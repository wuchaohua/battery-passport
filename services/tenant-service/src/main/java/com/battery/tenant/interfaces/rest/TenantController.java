 package com.battery.tenant.interfaces.rest;

 import com.battery.common.PageResult;
 import com.battery.common.Result;
 import com.battery.tenant.application.service.TenantService;
 import com.battery.tenant.domain.model.Tenant;
 import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
 import org.springframework.web.bind.annotation.*;

 @RestController
 @RequestMapping("/api/v1/tenants")
 public class TenantController {

     private final TenantService service;
     public TenantController(TenantService service) { this.service = service; }

     @GetMapping
     public Result<PageResult<Tenant>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
         Page<Tenant> p = service.list(page, size);
         return Result.ok(new PageResult<>(p.getRecords(), p.getTotal(), page, size));
     }

     @GetMapping("/{id}")
     public Result<Tenant> get(@PathVariable Long id) { return Result.ok(service.getById(id)); }

     @PostMapping
     public Result<Void> create(@RequestBody Tenant tenant) { service.create(tenant); return Result.ok(); }

     @PutMapping("/{id}")
     public Result<Void> update(@PathVariable Long id, @RequestBody Tenant tenant) { service.update(id, tenant); return Result.ok(); }

     @DeleteMapping("/{id}")
     public Result<Void> delete(@PathVariable Long id) { service.delete(id); return Result.ok(); }
 }
