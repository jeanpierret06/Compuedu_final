# ==========================================
# ETAPA 1: Compilación del proyecto Java (JDK 21)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Directorio de trabajo para la compilación
WORKDIR /app

# Copiar el archivo de configuración de dependencias de Maven
COPY pom.xml .

# Descargar las dependencias para aprovechar la caché de capas de Docker
RUN mvn dependency:go-offline -B

# Copiar el código fuente del backend de Java
COPY src ./src

# Compilar el proyecto y empaquetar el archivo .jar omitiendo las pruebas unitarias
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Entorno de ejecución ligero (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el archivo .jar generado en la etapa anterior a la imagen final
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto estándar que configurará Render de forma dinámica
EXPOSE 8080

# Comando para arrancar la aplicación de Spring Boot en producción
CMD ["java", "-jar", "app.jar"]