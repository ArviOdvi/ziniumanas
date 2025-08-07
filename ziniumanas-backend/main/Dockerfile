FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

WORKDIR /app/main

RUN chmod +x ../mvnw && ../mvnw dependency:go-offline
RUN ../mvnw clean package -DskipTests

# Final image
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app/main/target/ziniumanas-backend-main-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]