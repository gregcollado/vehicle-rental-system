# Etapa 1: build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests


# Etapa 2: run
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/vehicle-rental-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]