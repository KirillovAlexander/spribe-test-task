# Use the official OpenJDK 21 JDK image as the base image
FROM eclipse-temurin:21-jdk AS build

# Set the working directory inside the container
WORKDIR /exchangerate

# Copy the Gradle wrapper and build configuration files first to leverage Docker cache
COPY gradlew gradlew.bat /exchangerate/
COPY gradle /exchangerate/gradle
COPY build.gradle settings.gradle /exchangerate/

# Download the dependencies (to leverage Docker layer caching)
RUN ./gradlew dependencies --no-daemon

# Copy the application source code
COPY src /exchangerate/src

# Build the application
RUN ./gradlew clean bootJar --no-daemon

# Second stage - run the application in a lightweight JRE container
FROM eclipse-temurin:21-jre

# Set the working directory for the runtime container
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /exchangerate/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]