FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw package -DskipTests

EXPOSE 8080
CMD ["java", "-jar", "target/ziniumanas-backend-main-0.0.1-SNAPSHOT.jar"]