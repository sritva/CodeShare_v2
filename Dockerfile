FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
RUN apk add --no-cache nodejs npm maven
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src/ src/
COPY frontend/ frontend/
RUN mvn clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/CodeShare-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
