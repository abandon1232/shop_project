package com.example.exception;

import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages="com.example.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    //Handle general exceptions consistently with @ExceptionHandler.
    @ExceptionHandler(Exception.class)
    @ResponseBody // Return a JSON response.
    public Result error(HttpServletRequest request, Exception e){
        log.error("Unhandled request error", e);
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody // Return a JSON response.
    public Result customError(HttpServletRequest request, CustomException e){
        return Result.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class})
    @ResponseBody
    public Result validationError(Exception e) {
        return Result.error(ResultCodeEnum.PARAM_ERROR);
    }
}
