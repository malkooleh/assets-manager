FROM eclipse-temurin:21-jdk-alpine as base

# set the image’s working directory
WORKDIR /app

# install dependencies into the image
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# clean up the file
RUN sed -i 's/\r$//' mvnw
# run with the SH path
RUN /bin/sh mvnw dependency:resolve

# add source code into the image
COPY src ./src

FROM base as build
RUN ./mvnw package

FROM eclipse-temurin:21-jdk-alpine as production
#EXPOSE 8080
COPY --from=build /app/target/gateway-service-*.jar /gateway-service.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/gateway-service.jar"]
