FROM maven:3-jdk-11-slim AS build-env
ADD . /app
WORKDIR /app
RUN mvn versions:set -DnewVersion='1' -DgenerateBackupPoms=false clean package

FROM gcr.io/distroless/base
COPY --from=build-env /app/ols-*/target/ols-*.war /
RUN ls -l /ols-*.war
CMD ["tail", "-f", "/dev/null"]
