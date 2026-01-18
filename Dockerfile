FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
COPY target/oda-files-service-0.0.3.jar /app

CMD ["java","--add-opens","java.base/java.time=ALL-UNNAMED","-jar","oda-files-service-0.0.3.jar"]
