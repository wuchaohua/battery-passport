 package com.battery.common;

 public class BatteryException extends RuntimeException {

     private final int code;

     public BatteryException(int code, String message) {
         super(message);
         this.code = code;
     }

     public BatteryException(String message) { this(500, message); }

     public int getCode() { return code; }
 }
