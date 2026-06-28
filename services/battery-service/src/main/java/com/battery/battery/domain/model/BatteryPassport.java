 package com.battery.battery.domain.model;

 import com.baomidou.mybatisplus.annotation.TableName;
 import com.battery.mybatis.BaseEntity;
 import java.time.LocalDate;

 @TableName("battery_passport")
 public class BatteryPassport extends BaseEntity {

     private String passportId;
     private String serialNumber;
     private String productModel;
     private String manufacturer;
     private LocalDate productionDate;
     private Double ratedCapacity;
     private Double ratedVoltage;
     private Double initialSoh;
     private String chemistryType;
     private Double weight;
     private String status;
     private String rawJson;

     public String getPassportId() { return passportId; }
     public void setPassportId(String passportId) { this.passportId = passportId; }
     public String getSerialNumber() { return serialNumber; }
     public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
     public String getProductModel() { return productModel; }
     public void setProductModel(String productModel) { this.productModel = productModel; }
     public String getManufacturer() { return manufacturer; }
     public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
     public LocalDate getProductionDate() { return productionDate; }
     public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
     public Double getRatedCapacity() { return ratedCapacity; }
     public void setRatedCapacity(Double ratedCapacity) { this.ratedCapacity = ratedCapacity; }
     public Double getRatedVoltage() { return ratedVoltage; }
     public void setRatedVoltage(Double ratedVoltage) { this.ratedVoltage = ratedVoltage; }
     public Double getInitialSoh() { return initialSoh; }
     public void setInitialSoh(Double initialSoh) { this.initialSoh = initialSoh; }
     public String getChemistryType() { return chemistryType; }
     public void setChemistryType(String chemistryType) { this.chemistryType = chemistryType; }
     public Double getWeight() { return weight; }
     public void setWeight(Double weight) { this.weight = weight; }
     public String getStatus() { return status; }
     public void setStatus(String status) { this.status = status; }
     public String getRawJson() { return rawJson; }
     public void setRawJson(String rawJson) { this.rawJson = rawJson; }
 }
