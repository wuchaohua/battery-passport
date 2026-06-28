 package com.battery.plugin.api.dto;

 import java.util.Map;

 public class NotificationContext {

     private String notificationType;
     private String title;
     private String content;
     private String recipient;
     private Map<String, Object> metadata;

     public String getNotificationType() { return notificationType; }
     public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
     public String getTitle() { return title; }
     public void setTitle(String title) { this.title = title; }
     public String getContent() { return content; }
     public void setContent(String content) { this.content = content; }
     public String getRecipient() { return recipient; }
     public void setRecipient(String recipient) { this.recipient = recipient; }
     public Map<String, Object> getMetadata() { return metadata; }
     public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
 }
