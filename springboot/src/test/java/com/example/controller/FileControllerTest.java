package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.exception.CustomException;
import com.example.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileStorageService fileStorageService;

    private FileController controller;

    @BeforeEach
    void setUp() {
        controller = new FileController(fileStorageService);
        ReflectionTestUtils.setField(controller, "ip", "localhost");
        ReflectionTestUtils.setField(controller, "port", "9090");
    }

    @Test
    void uploadBuildsUrlOnlyAfterStorageSucceeds() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1});
        when(fileStorageService.storeImage(file)).thenReturn("generated.png");

        Result result = controller.upload(file);

        assertEquals(ResultCodeEnum.SUCCESS.code, result.getCode());
        assertEquals("http://localhost:9090/files/generated.png", result.getData());
    }

    @Test
    void uploadPropagatesStorageFailure() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[]{1});
        when(fileStorageService.storeImage(file))
                .thenThrow(new CustomException(ResultCodeEnum.SYSTEM_ERROR));

        CustomException error = assertThrows(CustomException.class,
                () -> controller.upload(file));

        assertEquals(ResultCodeEnum.SYSTEM_ERROR.code, error.getCode());
    }

    @Test
    void deleteReturnsNormalResult() {
        Result result = controller.delFile("generated.png");

        assertEquals(ResultCodeEnum.SUCCESS.code, result.getCode());
        verify(fileStorageService).delete("generated.png");
    }
}
