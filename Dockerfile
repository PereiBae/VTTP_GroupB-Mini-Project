FROM openjdk:23-jdk-oracle AS builder
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

ARG COMPILED_DIR=/compiledir

WORKDIR ${COMPILED_DIR}

COPY src src
COPY .mvn .mvn
COPY pom.xml .
COPY mvnw .

RUN chmod +x mvnw
RUN ./mvnw package -Dmaven.test.skip=true

ENV SERVER_PORT=8080
ENV SPRING_DATA_REDIS_HOST=localhost
ENV SPRING_DATA_REDIS_USERNAME=""
ENV SPRING_DATA_REDIS_PASSWORD=""
ENV SPRING_DATA_REDIS_PORT=6379
ENV SPRING_DATA_REDIS_DATABASE=0
ENV API_URL=""

EXPOSE ${SERVER_PORT}

#second stage
FROM openjdk:23-jdk-oracle
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"

ARG WORK_DIR=/app

WORKDIR ${WORK_DIR}

COPY --from=builder /compiledir/target/Miniproject-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080
ENV SPRING_DATA_REDIS_HOST=localhost
ENV SPRING_DATA_REDIS_USERNAME=""
ENV SPRING_DATA_REDIS_PASSWORD=""
ENV SPRING_DATA_REDIS_PORT=6379
ENV SPRING_DATA_REDIS_DATABASE=0
ENV API_URL=""

EXPOSE ${SERVER_PORT}

ENTRYPOINT java -jar app.jar