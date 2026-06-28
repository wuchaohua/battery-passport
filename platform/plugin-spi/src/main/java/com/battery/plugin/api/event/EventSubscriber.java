 package com.battery.plugin.api.event;

 public interface EventSubscriber {

     String subscriberId();

     boolean supports(String eventType);

     void onEvent(BatteryEvent event);
 }
