package com.example.controller;

import com.example.common.enums.ResultCodeEnum;
import com.example.exception.GlobalExceptionHandler;
import com.example.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class CartValidationTest {
    @Mock
    private CartService cartService;
    @InjectMocks
    private CartController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addRejectsQuantityAboveNinetyNine() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .contentType(APPLICATION_JSON)
                        .content("{\"goodsId\":7,\"quantity\":100}"))
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));

        verifyNoInteractions(cartService);
    }

    @Test
    void updateRejectsZeroQuantity() throws Exception {
        mockMvc.perform(put("/cart/items/14")
                        .contentType(APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));

        verifyNoInteractions(cartService);
    }
}
