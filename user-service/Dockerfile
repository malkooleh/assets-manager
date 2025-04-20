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

# to run the tests, we just need to run the 'docker build --target test .' command
FROM base as test
RUN ["./mvnw", "test"]

FROM base as development
# with debug
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.profiles=...", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000'"]

FROM base as build
RUN ./mvnw package

FROM eclipse-temurin:21-jdk-alpine as production
#EXPOSE 8080
COPY --from=build /app/target/user-service-*.jar /user-service.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/user-service.jar"]
