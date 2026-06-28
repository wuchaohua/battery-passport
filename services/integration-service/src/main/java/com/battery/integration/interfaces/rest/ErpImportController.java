 package com.battery.integration.interfaces.rest;

 import com.battery.common.Result;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.*;

 import java.util.Map;

 @RestController
 @RequestMapping("/api/v1/integration")
 public class ErpImportController {

     private static final Logger log = LoggerFactory.getLogger(ErpImportController.class);

     @PostMapping("/erp/import")
     public Result<Map<String, Object>> importFromErp(@RequestBody Map<String, Object> erpData,
                                                       @RequestParam String sourceType) {
         log.info("ERP数据导入请求: sourceType={}", sourceType);
         // 这里会通过 SPI 调用 BatteryDataTransformer 转换数据
         // 然后调用 battery-service 的 Feign 接口创建护照
         return Result.ok(Map.of("imported", 0, "failed", 0));
     }

     @GetMapping("/erp/sources")
     public Result<Map<String, String>> listSupportedSources() {
         return Result.ok(Map.of(
             "tuobang_erp_v2", "托邦股份 ERP V2",
             "tuobang_erp_v3", "托邦股份 ERP V3",
             "trina_mes", "天合储能 MES系统"
         ));
     }

     @PostMapping("/sso/config")
     public Result<Void> saveSsoConfig(@RequestBody Map<String, Object> config) {
         log.info("保存SSO配置: {}", config);
         return Result.ok();
     }
 }
