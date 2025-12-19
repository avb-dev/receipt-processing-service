FROM amazoncorretto:21-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

COPY libs/mytax-client-0.1.1.jar /tmp/mytax-client-0.1.1.jar

RUN --mount=type=cache,target=/root/.m2 mvn install:install-file \
  -Dfile=/tmp/mytax-client-0.1.1.jar \
  -DgroupId=ru.loolzaaa.nalog.mytax \
  -DartifactId=mytax-client \
  -Dversion=0.1.1 \
  -Dpackaging=jar

COPY pom.xml ./pom.xml
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

