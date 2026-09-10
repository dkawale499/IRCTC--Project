FROM eclipse-temurin:25-jre
EXPOSE 8081
ADD target/irctc.jar irctc.jar
ENTRYPOINT ["java","-jar","irctc.jar"]