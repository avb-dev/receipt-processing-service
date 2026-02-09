FROM amazoncorretto:21-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml ./pom.xml
COPY src ./src
COPY checkstyle.xml ./checkstyle.xml
COPY suppressions.xml ./suppressions.xml

RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

