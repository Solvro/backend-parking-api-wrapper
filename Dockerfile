FROM docker.io/library/maven:3.9.9-eclipse-temurin-21 AS build
COPY . /build/
WORKDIR /build
RUN mvn -B clean package --file pom.xml

FROM docker.io/library/openjdk:21
WORKDIR /app
COPY --from=build /build/target/parking-*-SNAPSHOT.jar parking.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "parking.jar"]
