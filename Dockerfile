# syntax=docker/dockerfile:1
# Railway 从 monorepo 根目录部署时使用此 Dockerfile

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY NeuLLMDev/pom.xml .
COPY NeuLLMDev/src src
RUN mvn -DskipTests -B package

FROM eclipse-temurin:17-jre-jammy
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
