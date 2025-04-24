# Usa una imagen base de OpenJDK
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado por Spring Boot al contenedor
COPY build/libs/conitrack-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto donde corre la aplicación Spring Boot
EXPOSE 8080

# Configuración de conexión a la base de datos
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-db:5432/conitrack
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=root
# ENV SPRING_DATASOURCE_URL=jdbc:postgresql://conitrack_user:xykmzdgGu9BITrWsOff2FFSkDmv5Fbey@dpg-d05c33euk2gs73cb0520-a/conitrack
# ENV SPRING_DATASOURCE_USERNAME=conitrack_user
# ENV SPRING_DATASOURCE_PASSWORD=xykmzdgGu9BITrWsOff2FFSkDmv5Fbey

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
