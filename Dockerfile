FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libasound2 \
        libgl1 \
        libgtk-3-0 \
        libnss3 \
        libx11-6 \
        libxcomposite1 \
        libxext6 \
        libxfixes3 \
        libxi6 \
        libxrandr2 \
        libxrender1 \
        libxtst6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/target/classes ./classes
COPY --from=build /workspace/target/dependency ./lib

ENV DB_URL=jdbc:mysql://db:3306/storemanager?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
ENV DB_USER=storemanager
ENV DB_PASSWORD=storemanager

CMD ["java", "-cp", "/app/classes:/app/lib/*", "com.manager.storemanager.StoreManager"]
