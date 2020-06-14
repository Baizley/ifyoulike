FROM maven:3.6-jdk-14 AS builder

WORKDIR build/

COPY pom.xml ./
COPY ./src ./src

RUN ["mvn", "clean", "package", "spring-boot:repackage"]

RUN ls

FROM openjdk:14-alpine

WORKDIR app/

COPY --from=builder /build/target/*.jar ./app.jar

CMD ["java", "--enable-preview", "-jar", "app.jar"]