 FROM registry.battery.com/base/jdk17-minimal:latest
 WORKDIR /app
 RUN useradd -m -u 1000 battery
 USER battery
 COPY target/*.jar app.jar
 ENTRYPOINT ["java", "-jar", "app.jar"]
