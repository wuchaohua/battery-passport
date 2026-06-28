 package com.battery.common;

 import com.fasterxml.jackson.annotation.JsonInclude;
 import java.io.Serializable;

 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class Result<T> implements Serializable {

     private int code;
     private String message;
     private T data;

     public static <T> Result<T> ok(T data) {
         Result<T> r = new Result<>();
         r.code = 200;
         r.message = "success";
         r.data = data;
         return r;
     }

     public static <T> Result<T> ok() { return ok(null); }

     public static <T> Result<T> fail(int code, String message) {
         Result<T> r = new Result<>();
         r.code = code;
         r.message = message;
         return r;
     }

     public static <T> Result<T> fail(String message) { return fail(500, message); }

     public int getCode() { return code; }
     public void setCode(int code) { this.code = code; }
     public String getMessage() { return message; }
     public void setMessage(String message) { this.message = message; }
     public T getData() { return data; }
     public void setData(T data) { this.data = data; }
 }
