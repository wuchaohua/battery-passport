 package com.battery.plugin.api.event;

 import java.time.LocalDateTime;
 import java.util.Map;

 public class BatteryEvent {

     private String eventId;
     private String eventType;
     private String passportId;
     private String serialNumber;
     private LocalDateTime occurredAt;
     private Map<String, Object> payload;

     public String getEventId() { return eventId; }
     public void setEventId(String eventId) { this.eventId = eventId; }

     public String getEventType() { return eventType; }
     public void setEventType(String eventType) { this.eventType = eventType; }

     public String getPassportId() { return passportId; }
     public void setPassportId(String passportId) { this.passportId = passportId; }

     public String getSerialNumber() { return serialNumber; }
     public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

     public LocalDateTime getOccurredAt() { return occurredAt; }
     public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

     public Map<String, Object> getPayload() { return payload; }
     public void setPayload(Map<String, Object> payload) { this.payload = payload; }

     public static final String TYPE_PASSPORT_CREATED = "PASSPORT_CREATED";
     public static final String TYPE_PASSPORT_UPDATED = "PASSPORT_UPDATED";
     public static final String TYPE_REPORT_GENERATED = "REPORT_GENERATED";
     public static final String TYPE_QUALITY_ALERT = "QUALITY_ALERT";
     public static final String TYPE_TRACE_UPDATED = "TRACE_UPDATED";
 }
