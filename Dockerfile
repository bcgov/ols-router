FROM maven:3-jdk-11-slim AS build-env
ADD . /app
WORKDIR /app
RUN mvn clean package

FROM gcr.io/distroless/base
COPY --from=build-env /app/ols-*/target/ols-*.war /
RUN chmod o+r $(ls /ols-*.war)
CMD ["tail", "-f", "/dev/null"]
