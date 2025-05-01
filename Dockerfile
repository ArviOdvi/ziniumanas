FROM openjdk:21-jdk-slim

# Nustatome darbo katalogą
WORKDIR /app

# Įdiegiame reikalingus įrankius
RUN apt-get update && apt-get install -y maven

# Nukopijuojame sertifikatą į konteinerį
COPY certs/kurjeris.lt.crt /tmp/kurjeris.lt.crt

# Įkeliame sertifikatą į JVM truststore
RUN keytool -importcert \
  -noprompt \
  -trustcacerts \
  -alias kurjeris-lt \
  -file /tmp/kurjeris.lt.crt \
  -keystore /usr/local/openjdk-21/lib/security/cacerts \
  -storepass changeit

# Kopijuojame projekto failus
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/Ziniumanas-0.0.1-SNAPSHOT.jar"]