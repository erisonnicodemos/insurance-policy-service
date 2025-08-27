FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /workspace/app

# Copia o arquivo pom.xml e baixa as dependências para cache
COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# Copia o código fonte e compila
COPY src src
RUN mvn package -DskipTests

# Imagem final
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria diretório para logs
RUN mkdir -p /app/logs

# Copia o JAR da etapa de build
COPY --from=build /workspace/app/target/*.jar app.jar

# Expõe a porta da aplicação
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

