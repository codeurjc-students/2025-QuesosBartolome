# -------- ETAPA 1: construir FRONTEND (Angular) --------
FROM node:22 AS frontend-build
WORKDIR /app
COPY frontend/ .
RUN npm install
RUN npm run build -- --configuration=production --base-href=/

# -------- ETAPA 2: construir BACKEND (Spring Boot) --------
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build  
WORKDIR /app
COPY backend/ .
COPY --from=frontend-build /app/dist/frontend/browser/ ./src/main/resources/static/
RUN mvn clean package -DskipTests

# -------- ETAPA 3: imagen final --------
FROM eclipse-temurin:21-jdk  
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
COPY backend/src/main/resources/keystore.jks /app/keystore.jks

EXPOSE 443
ENTRYPOINT ["java", "-jar", "app.jar", "--server.ssl.key-store=/app/keystore.jks"]