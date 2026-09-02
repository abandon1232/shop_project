package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tempDirectory;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService(tempDirectory.toString());
    }

    @Test
    void rejectsNonImageContentEvenWhenMimeTypeClaimsPng() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "attack.png", "image/png",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        CustomException error = assertThrows(CustomException.class,
                () -> service.storeImage(file));

        assertEquals(ResultCodeEnum.PARAM_ERROR.code, error.getCode());
    }

    @Test
    void rejectsFilesLargerThanFiveMiB() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]);

        CustomException error = assertThrows(CustomException.class,
                () -> service.storeImage(file));

        assertEquals(ResultCodeEnum.PARAM_ERROR.code, error.getCode());
    }

    @Test
    void storesDecodedImageUnderGeneratedName() throws Exception {
        MockMultipartFile file = validOnePixelPng("../../avatar.png");

        String storedName = service.storeImage(file);

        assertTrue(storedName.matches("[0-9a-f-]{36}\\.png"));
        assertTrue(Files.exists(tempDirectory.resolve(storedName)));
        assertNotNull(ImageIO.read(tempDirectory.resolve(storedName).toFile()));
    }

    @Test
    void refusesPathsOutsideTheUploadDirectory() {
        CustomException deleteError = assertThrows(CustomException.class,
                () -> service.delete("../application.yml"));
        CustomException loadError = assertThrows(CustomException.class,
                () -> service.load("../application.yml"));

        assertEquals(ResultCodeEnum.PARAM_ERROR.code, deleteError.getCode());
        assertEquals(ResultCodeEnum.PARAM_ERROR.code, loadError.getCode());
    }

    @Test
    void refusesTheUploadDirectoryItselfAsAFileName() {
        CustomException error = assertThrows(CustomException.class,
                () -> service.delete("."));

        assertEquals(ResultCodeEnum.PARAM_ERROR.code, error.getCode());
        assertTrue(Files.isDirectory(tempDirectory));
    }

    @Test
    void reportsWriteFailureInsteadOfReturningAName() throws Exception {
        Path blockedRoot = tempDirectory.resolve("not-a-directory");
        Files.writeString(blockedRoot, "blocked");
        FileStorageService blockedService = new FileStorageService(blockedRoot.toString());

        CustomException error = assertThrows(CustomException.class,
                () -> blockedService.storeImage(validOnePixelPng("avatar.png")));

        assertEquals(ResultCodeEnum.SYSTEM_ERROR.code, error.getCode());
    }

    private MockMultipartFile validOnePixelPng(String originalName) throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("file", originalName, "image/png", output.toByteArray());
    }
}
