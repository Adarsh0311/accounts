## Start with a base image containing Java runtime
#FROM eclipse-temurin:17-jdk-jammy
#
#
## MAINTAINER instruction is deprecated in favor of using label
## MAINTAINER eazybytes.com
##Information around who maintains the image
#LABEL "org.opencontainers.image.authors"="eazybytes.com"
#
## The application's jar fileHERE, we are copying the jar file from the target folder to the root of the image
#COPY target/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar
#
## When ever someone is trying to generate a container from this image, please execute so and so command
#ENTRYPOINT ["java","-jar","accounts-0.0.1-SNAPSHOT.jar"]


# ── STAGE 1: BUILD ─────────────────────────────────────────────────────
# Use the official Maven + Java 17 image to compile the project
# This image has Maven and JDK 17 already installed
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code to the container
# If pom.xml hasn't changed, Maven won't re-download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source code
COPY src ./src


# Build the JAR inside the container
RUN mvn clean package -DskipTests


# ── STAGE 2: RUNTIME ────────────────────────────────────────────────────
# Use a small JRE-only image — no Maven, no JDK, just what we need to RUN
# This makes the final image much smaller (~200MB vs ~600MB)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/accounts-0.0.1-SNAPSHOT.jar app.jar

# Tell Docker the app listens on port 8080
EXPOSE 8080

# Run the JAR file when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]

#Two stages (build + runtime) are used because the build stage needs Maven and JDK to compile — that is heavy.
#The runtime stage only needs JRE to run the JAR — much lighter. Final image ships only what is needed to run, not to build.
#This is called a multi-stage build and is important to mention in interviews.