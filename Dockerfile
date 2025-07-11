FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN --mount=type=cache,target=/root/.m2 \
    java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /application
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
EXPOSE 8080
