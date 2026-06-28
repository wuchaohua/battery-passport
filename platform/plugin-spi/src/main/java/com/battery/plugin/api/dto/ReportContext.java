 package com.battery.plugin.api.dto;

 import java.util.Map;

 public class ReportContext {

     private String reportType;
     private String templateName;
     private String language;
     private Map<String, Object> extraParams;

     public String getReportType() { return reportType; }
     public void setReportType(String reportType) { this.reportType = reportType; }
     public String getTemplateName() { return templateName; }
     public void setTemplateName(String templateName) { this.templateName = templateName; }
     public String getLanguage() { return language; }
     public void setLanguage(String language) { this.language = language; }
     public Map<String, Object> getExtraParams() { return extraParams; }
     public void setExtraParams(Map<String, Object> extraParams) { this.extraParams = extraParams; }
 }
