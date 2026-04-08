# Base image - Using Zeta's custom OpenJDK image from AWS ECR
FROM 813361731051.dkr.ecr.ap-south-1.amazonaws.com/zeta-openjdk:2.0.0

# Build arguments that can be passed at build time
# app: Name of the application
# version: Version of the application
# lastCommitHash: Git commit hash for traceability
# lastCommitAuthorEmail: Email of the last person who made changes
ARG app
ARG version
ARG lastCommitHash
ARG lastCommitAuthorEmail

# Setting environment variables for application metadata
# These variables store information about the artifact for monitoring and tracking
ENV ARTIFACT_NAME $app
ENV ARTIFACT_VERSION $version
ENV ARTIFACT_COMMIT_ABR $lastCommitHash
ENV ARTIFACT_COMMITTER $lastCommitAuthorEmail

# Setting application-specific environment variables
# APP: Application name passed from build arg
# JAR_PATH: Full path to the JAR file (DATA_PATH is defined in the base image)
ENV APP $app
ENV JAR_PATH "$DATA_PATH/$APP.jar"

# Creating log directory for the application and setting appropriate permissions
# LOGS_PATH and USERNAME are defined in the base image
RUN mkdir -p "$LOGS_PATH/$APP" && chown -R $USERNAME:$USERNAME "$LOGS_PATH/$APP"

# Copying the compiled JAR file from the build context into the container
# Sets ownership to the non-root user defined in the base image
COPY --chown=$USERNAME:$USERNAME ./target/$APP-$version.jar $JAR_PATH

# Switching to a non-root user for security best practices
# USERNAME is defined in the base image
USER $USERNAME:$USERNAME