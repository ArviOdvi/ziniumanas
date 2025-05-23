FROM openjdk:21-jdk-slim

# Įdiegiame Python 3.10 ir priklausomybes
RUN apt-get update && apt-get install -y \
    python3.10 \
    python3.10-dev \
    python3-pip \
    maven \
    && ln -sf python3.10 /usr/bin/python3 \
    && ln -sf pip3 /usr/bin/pip

# Įdiegiame PyTorch ir transformers
RUN pip3 install torch==2.1.0 transformers==4.44.2 --no-cache-dir

# Nustatome darbo katalogą
WORKDIR /app

# Kopijuojame sertifikatus
COPY certs/kurjeris.lt.crt /tmp/kurjeris.lt.crt
COPY certs/_.lrt.lt.crt /tmp/_.lrt.lt.crt
COPY certs/ve.lt.crt /tmp/ve.lt.crt

# Įkeliame sertifikatus į JVM truststore
RUN keytool -importcert \
    -noprompt \
    -trustcacerts \
    -alias kurjeris-lt \
    -file /tmp/kurjeris.lt.crt \
    -keystore /usr/local/openjdk-21/lib/security/cacerts \
    -storepass changeit
RUN keytool -importcert \
    -noprompt \
    -trustcacerts \
    -alias lrt-lt \
    -file /tmp/_.lrt.lt.crt \
    -keystore /usr/local/openjdk-21/lib/security/cacerts \
    -storepass changeit
RUN keytool -importcert \
    -noprompt \
    -trustcacerts \
    -alias ve-lt \
    -file /tmp/ve.lt.crt \
    -keystore /usr/local/openjdk-21/lib/security/cacerts \
    -storepass changeit

# Kopijuojame projekto failus
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/Ziniumanas-0.0.1-SNAPSHOT.jar"]