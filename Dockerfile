FROM docker.io/library/maven:3.9.9-eclipse-temurin-21 AS build
COPY .git /build/.git
WORKDIR /build
RUN git reset --hard
RUN mvn -B package --file pom.xml

FROM docker.io/library/openjdk:21
WORKDIR /app
COPY --from=build /build/target/parking-*-SNAPSHOT.jar parking.jar
EXPOSE 8080
CMD ["java", "-jar", "parking.jar"]
