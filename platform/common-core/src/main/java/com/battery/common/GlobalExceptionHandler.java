 package com.battery.common;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.bind.annotation.RestControllerAdvice;

 @RestControllerAdvice
 public class GlobalExceptionHandler {

     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

     @ExceptionHandler(BatteryException.class)
     @ResponseStatus(HttpStatus.OK)
     public Result<Void> handleBatteryException(BatteryException e) {
         log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
         return Result.fail(e.getCode(), e.getMessage());
     }

     @ExceptionHandler(IllegalArgumentException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
         return Result.fail(400, e.getMessage());
     }

     @ExceptionHandler(Exception.class)
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     public Result<Void> handleException(Exception e) {
         log.error("系统异常", e);
         return Result.fail(500, "系统内部错误");
     }
 }
