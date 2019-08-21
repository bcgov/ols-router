FROM maven:3-jdk-11-slim AS builder
ADD . /app
WORKDIR /app
RUN mvn versions:set -DnewVersion='1' -DgenerateBackupPoms=false package

FROM gcr.io/distroless/base
COPY --from=builder /app/ols-*/target/ols-*.war /
RUN ls -l /ols-*.war
CMD ["tail", "-f", "/dev/null"]
