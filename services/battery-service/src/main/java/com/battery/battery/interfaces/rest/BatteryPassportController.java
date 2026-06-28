 package com.battery.battery.interfaces.rest;

 import com.battery.battery.application.dto.PassportCreateRequest;
 import com.battery.battery.application.service.BatteryPassportService;
 import com.battery.battery.domain.model.BatteryPassport;
 import com.battery.common.PageResult;
 import com.battery.common.Result;
 import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
 import jakarta.validation.Valid;
 import org.springframework.web.bind.annotation.*;

 import java.util.List;

 @RestController
 @RequestMapping("/api/v1/passports")
 public class BatteryPassportController {

     private final BatteryPassportService service;

     public BatteryPassportController(BatteryPassportService service) { this.service = service; }

     @GetMapping
     public Result<PageResult<BatteryPassport>> list(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "20") int size,
             @RequestParam(required = false) String status,
             @RequestParam(required = false) String manufacturer) {
         Page<BatteryPassport> p = service.list(page, size, status, manufacturer);
         return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), page, size));
     }

     @GetMapping("/{passportId}")
     public Result<BatteryPassport> get(@PathVariable String passportId) {
         return Result.ok(service.getByPassportId(passportId));
     }

     @PostMapping
     public Result<String> create(@Valid @RequestBody PassportCreateRequest request) {
         BatteryPassport passport = new BatteryPassport();
         passport.setSerialNumber(request.getSerialNumber());
         passport.setProductModel(request.getProductModel());
         passport.setManufacturer(request.getManufacturer());
         passport.setProductionDate(request.getProductionDate());
         passport.setRatedCapacity(request.getRatedCapacity());
         passport.setRatedVoltage(request.getRatedVoltage());
         passport.setInitialSoh(request.getInitialSoh());
         passport.setChemistryType(request.getChemistryType());
         passport.setWeight(request.getWeight());
         passport.setRawJson(request.getExtraJson());
         String id = service.create(passport);
         return Result.ok(id);
     }

     @PutMapping("/{passportId}")
     public Result<Void> update(@PathVariable String passportId, @RequestBody PassportCreateRequest request) {
         BatteryPassport passport = new BatteryPassport();
         passport.setSerialNumber(request.getSerialNumber());
         passport.setProductModel(request.getProductModel());
         passport.setRatedCapacity(request.getRatedCapacity());
         passport.setStatus(request.getStatus());
         service.update(passportId, passport);
         return Result.ok();
     }

     @DeleteMapping("/{passportId}")
     public Result<Void> delete(@PathVariable String passportId) {
         service.delete(passportId);
         return Result.ok();
     }

     @GetMapping("/search")
     public Result<List<BatteryPassport>> search(@RequestParam String serialNumber) {
         return Result.ok(service.searchBySerialNumber(serialNumber));
     }
 }
