package com.example.controller;

import com.example.common.enums.ResultCodeEnum;
import com.example.exception.GlobalExceptionHandler;
import com.example.entity.Goods;
import com.example.service.GoodsService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class GoodsValidationTest {

    @Mock
    private GoodsService goodsService;

    @InjectMocks
    private GoodsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addRejectsBlankNameAndNegativeStock() throws Exception {
        mockMvc.perform(post("/goods/add")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"", "price":19.90, "count":-1, "typeId":2}
                                """))
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));

        verifyNoInteractions(goodsService);
    }

    @Test
    void addRejectsNegativePrice() throws Exception {
        mockMvc.perform(post("/goods/add")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Milk", "price":-0.01, "count":4, "typeId":2}
                                """))
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));

        verifyNoInteractions(goodsService);
    }

    @Test
    void selectPageLimitsPageSizeToOneHundred() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Method method = GoodsController.class.getMethod(
                "selectPage", Goods.class, Integer.class, Integer.class);

        int violations = validator.forExecutables()
                .validateParameters(controller, method, new Object[]{new Goods(), 1, 101})
                .size();

        assertEquals(1, violations);
        verifyNoInteractions(goodsService);
    }
}
