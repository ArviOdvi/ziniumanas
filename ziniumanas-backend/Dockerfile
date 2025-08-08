# ziniumanas-backend/Dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .

# Vietoj main/pom.xml – paleidžiam visą backend modulį (api + main)
RUN chmod +x ./mvnw \
 && ./mvnw -f ziniumanas-backend/pom.xml clean package -DskipTests -T1C

FROM eclipse-temurin:21-jdk
WORKDIR /app
# paimame .jar iš MAIN modulio
COPY --from=builder /app/ziniumanas-backend/main/target/*.jar app.jar
# Įkeliam sertifikatus į Java truststore
COPY certs /app/certs
RUN for cert in /app/certs/*.crt; do \
      keytool -importcert -noprompt -trustcacerts \
      -alias $(basename $cert .crt) \
      -file $cert \
      -keystore $JAVA_HOME/lib/security/cacerts \
      -storepass changeit; \
    done
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]