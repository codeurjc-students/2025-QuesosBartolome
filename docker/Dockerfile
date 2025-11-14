# -------- ETAPA 1: construir FRONTEND (Angular) --------
FROM node:22 AS frontend-build
WORKDIR /app

# copiamos solo archivos necesarios para instalar dependencias
COPY frontend/package*.json ./
COPY frontend/angular.json ./
COPY frontend/tsconfig*.json ./
COPY frontend/src ./src

RUN npm install
RUN npm run build -- --configuration=production --base-href=/

# -------- ETAPA 2: construir BACKEND (Spring Boot) --------
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app

# copiamos pom.xml primero para usar cache
COPY backend/pom.xml ./
RUN mvn dependency:go-offline

# copiamos el código completo
COPY backend/src ./src

# Copiamos el frontend ya compilado al static del backend
COPY --from=frontend-build /app/dist/frontend/browser/ ./src/main/resources/static/

RUN mvn clean package -DskipTests

# -------- ETAPA 3: imagen final --------
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar
COPY backend/src/main/resources/keystore.jks /app/keystore.jks

EXPOSE 443
ENTRYPOINT ["java", "-jar", "app.jar", "--server.ssl.key-store=/app/keystore.jks"]
