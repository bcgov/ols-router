FROM maven:3-jdk-11-slim AS builder
ADD . /app
WORKDIR /app
RUN mvn versions:set -DnewVersion='1' -DgenerateBackupPoms=false && mvn package

FROM busybox
RUN mkdir /app
COPY --from=builder /app/ols-*/target/ols-*.war /app/
RUN ls -la /app/
CMD ["tail", "-f", "/dev/null"]
