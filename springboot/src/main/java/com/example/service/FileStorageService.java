package com.example.service;

import com.example.common.enums.ResultCodeEnum;
import com.example.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "jpg", "png", "gif");

    private final Path uploadRoot;

    public FileStorageService(@Value("${filePath:./files/}") String filePath) {
        this.uploadRoot = Path.of(filePath).toAbsolutePath().normalize();
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_IMAGE_SIZE) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }

        String extension = detectImageExtension(file);
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = resolve(storedName);
        try {
            Files.createDirectories(uploadRoot);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
            return storedName;
        } catch (IOException e) {
            log.error("Could not store uploaded image", e);
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }

    public Resource load(String storedName) {
        Path path = resolve(storedName);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        return new FileSystemResource(path);
    }

    public void delete(String storedName) {
        Path path = resolve(storedName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Could not delete stored image {}", storedName, e);
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }

    private String detectImageExtension(MultipartFile file) {
        try (InputStream input = file.getInputStream();
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            if (imageInput == null) {
                throw new CustomException(ResultCodeEnum.PARAM_ERROR);
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new CustomException(ResultCodeEnum.PARAM_ERROR);
            }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!ALLOWED_FORMATS.contains(format)) {
                    throw new CustomException(ResultCodeEnum.PARAM_ERROR);
                }
                reader.setInput(imageInput, true, true);
                if (reader.read(0) == null) {
                    throw new CustomException(ResultCodeEnum.PARAM_ERROR);
                }
                return "jpg".equals(format) ? "jpeg" : format;
            } finally {
                reader.dispose();
            }
        } catch (CustomException e) {
            throw e;
        } catch (IOException e) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    private Path resolve(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        Path resolved = uploadRoot.resolve(storedName).normalize();
        if (!resolved.startsWith(uploadRoot) || !uploadRoot.equals(resolved.getParent())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        return resolved;
    }
}
