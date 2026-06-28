 package com.battery.plugin.api;

 import com.battery.plugin.api.dto.BatteryData;

 public interface BatteryDataTransformer {

     String transformerId();

     boolean supports(String sourceType);

     BatteryData transform(Object rawData);
 }
