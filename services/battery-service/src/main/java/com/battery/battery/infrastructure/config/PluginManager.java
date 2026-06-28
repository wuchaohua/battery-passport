 package com.battery.battery.infrastructure.config;

 import com.battery.battery.domain.model.BatteryPassport;
 import com.battery.common.BatteryException;
 import com.battery.plugin.api.BatteryDataValidator;
 import com.battery.plugin.api.BatteryDataTransformer;
 import com.battery.plugin.api.dto.BatteryData;
 import com.battery.plugin.api.dto.ValidateResult;
 import jakarta.annotation.PostConstruct;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;

 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.*;
 import java.util.stream.Collectors;

 @Component
 public class PluginManager {

     private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

     private final List<BatteryDataValidator> validators = new ArrayList<>();

     @Value("${battery.plugin.scan-path:/opt/battery/plugins/}")
     private String pluginScanPath;

     @PostConstruct
     public void init() {
         loadPlugins();
     }

     public void loadPlugins() {
         validators.clear();
         Path dir = Paths.get(pluginScanPath);
         File pluginDir = dir.toFile();
         if (!pluginDir.exists() || !pluginDir.isDirectory()) {
             log.info("插件目录不存在: {}, 跳过插件加载", pluginScanPath);
             return;
         }

         File[] jars = pluginDir.listFiles((d, name) -> name.endsWith(".jar"));
         if (jars == null) return;

         for (File jar : jars) {
             try {
                 URLClassLoader classLoader = new URLClassLoader(
                         new URL[]{jar.toURI().toURL()},
                         Thread.currentThread().getContextClassLoader());
                 ServiceLoader<BatteryDataValidator> loader =
                         ServiceLoader.load(BatteryDataValidator.class, classLoader);
                 loader.forEach(validator -> {
                     validators.add(validator);
                     log.info("插件加载成功: {} -> {}", jar.getName(), validator.validatorId());
                 });
             } catch (Exception e) {
                 log.error("插件加载失败: {}", jar.getName(), e);
             }
         }

         validators.sort(Comparator.comparingInt(BatteryDataValidator::order));
         log.info("插件加载完毕，共 {} 个校验器", validators.size());
     }

     public void validate(BatteryPassport passport) {
         BatteryData data = toPluginData(passport);
         for (BatteryDataValidator validator : validators) {
             if (!validator.enabled()) continue;
             ValidateResult result = validator.validate(data);
             if (!result.isPassed()) {
                 String errors = String.join("; ", result.getErrors());
                 log.warn("校验器 {} 未通过: {}", validator.validatorId(), errors);
                 throw new BatteryException(422, "数据校验未通过: " + errors);
             }
         }
     }

     private BatteryData toPluginData(BatteryPassport p) {
         BatteryData data = new BatteryData();
         data.setPassportId(p.getPassportId());
         data.setSerialNumber(p.getSerialNumber());
         data.setProductModel(p.getProductModel());
         data.setManufacturer(p.getManufacturer());
         data.setProductionDate(p.getProductionDate());
         data.setRatedCapacity(p.getRatedCapacity());
         data.setRatedVoltage(p.getRatedVoltage());
         data.setInitialSoh(p.getInitialSoh());
         data.setChemistryType(p.getChemistryType());
         data.setWeight(p.getWeight());
         data.setRawJson(p.getRawJson());
         return data;
     }
 }
