 # ===== 多阶段构建 =====
 FROM registry.battery.com/base/jdk17-minimal:latest AS builder
 WORKDIR /build
 COPY target/*.jar app.jar
 RUN java -Djarmode=layertools -jar app.jar extract

 FROM registry.battery.com/base/jdk17-minimal:latest
 WORKDIR /app
 RUN useradd -m -u 1000 battery
 USER battery

 COPY --from=builder /build/dependencies/ ./
 COPY --from=builder /build/spring-boot-loader/ ./
 COPY --from=builder /build/snapshot-dependencies/ ./
 COPY --from=builder /build/application/ ./

 ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -Xms512m -Xmx2g -Djava.security.egd=file:/dev/./urandom"
 ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
 HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8084/actuator/health || exit 1
