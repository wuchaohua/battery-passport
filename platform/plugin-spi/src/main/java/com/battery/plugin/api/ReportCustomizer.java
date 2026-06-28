 package com.battery.plugin.api;

 import com.battery.plugin.api.dto.BatteryData;
 import com.battery.plugin.api.dto.ReportContext;

 public interface ReportCustomizer {

     String customizerId();

     byte[] customize(byte[] standardReport, BatteryData data, ReportContext ctx);

     default String[] supportedReportTypes() { return new String[0]; }
 }
