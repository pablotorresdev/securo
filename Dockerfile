# Etapa 1: Construir el .jar
FROM gradle:8.2.1-jdk17 AS builder

# Copiamos todo el proyecto
COPY . /home/app
WORKDIR /home/app

RUN chmod +x gradlew

# Build del proyecto
RUN ./gradlew clean bootJar -x test

# Etapa 2: Correr la app
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiamos el .jar desde la etapa anterior
COPY --from=builder /home/app/build/libs/*.jar app.jar

# Exponemos el puerto
EXPOSE 8080

# Variables de entorno para DB
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
ENV SPRING_DATASOURCE_USERNAME=${PGUSER}
ENV SPRING_DATASOURCE_PASSWORD=${PGPASSWORD}

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
