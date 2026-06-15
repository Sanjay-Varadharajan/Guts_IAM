#empty container of jdk
FROM eclipse-temurin:21-jdk

#creates a dir app and put code in that
WORKDIR /app

#copies my jar to the app jar
COPY target/Guts_IAM-0.0.1-SNAPSHOT.jar app.jar

#exposes 8080
EXPOSE 8080

#When container starts, Docker runs:java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]