 package com.battery.battery.application.dto;

 import jakarta.validation.constraints.NotBlank;
 import jakarta.validation.constraints.NotNull;
 import java.time.LocalDate;

 public class PassportCreateRequest {

     @NotBlank(message = "序列号不能为空")
     private String serialNumber;

     @NotBlank(message = "产品型号不能为空")
     private String productModel;

     @NotBlank(message = "制造商不能为空")
     private String manufacturer;

     private LocalDate productionDate;
     private Double ratedCapacity;
     private Double ratedVoltage;
     private Double initialSoh;
     private String chemistryType;
     private Double weight;
     private String extraJson;
     private String status;

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
     public String getExtraJson() { return extraJson; }
     public void setExtraJson(String extraJson) { this.extraJson = extraJson; }
     public String getStatus() { return status; }
     public void setStatus(String status) { this.status = status; }
 }
