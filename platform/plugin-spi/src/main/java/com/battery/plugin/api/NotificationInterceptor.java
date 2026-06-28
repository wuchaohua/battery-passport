 package com.battery.plugin.api;

 import com.battery.plugin.api.dto.NotificationContext;

 public interface NotificationInterceptor {

     String interceptorId();

     boolean shouldSend(NotificationContext ctx);

     NotificationContext transform(NotificationContext ctx);
 }
