package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.controller.request.AuthRequest;
import com.example.controller.request.PasswordChangeRequest;
import com.example.exception.GlobalExceptionHandler;
import com.example.service.AdminService;
import com.example.service.BusinessService;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class WebControllerTest {

    @Mock
    private AdminService adminService;
    @Mock
    private BusinessService businessService;
    @Mock
    private UserService userService;

    @InjectMocks
    private WebController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginRejectsUnknownRole() {
        Result result = controller.login(new AuthRequest("alice", "secret123", "OWNER"));

        assertEquals(ResultCodeEnum.PARAM_ERROR.code, result.getCode());
        assertEquals("Invalid parameters", result.getMsg());
        verifyNoInteractions(adminService, businessService, userService);
    }

    @Test
    void rootEndpointUsesAnEnglishHealthMessage() {
        Result result = controller.hello();

        assertEquals("Success", result.getMsg());
        assertEquals("API is running", result.getData());
    }

    @Test
    void registrationRejectsAdministratorRole() {
        Result result = controller.register(new AuthRequest("alice", "secret123", "ADMIN"));

        assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, result.getCode());
        verifyNoInteractions(adminService, businessService, userService);
    }

    @Test
    void passwordChangeRejectsUnknownRole() {
        Result result = controller.updatePassword(
                new PasswordChangeRequest("alice", "old-secret", "new-secret", "OWNER"));

        assertEquals(ResultCodeEnum.PARAM_ERROR.code, result.getCode());
        verifyNoInteractions(adminService, businessService, userService);
    }

    @Test
    void loginRejectsBlankFieldsThroughBeanValidation() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\",\"role\":\"\"}"))
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));

        verifyNoInteractions(adminService, businessService, userService);
    }
}
