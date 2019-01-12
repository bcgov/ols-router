FROM busybox:latest
MAINTAINER leo.lou@gov.bc.ca

RUN wget -O ${APP}.war http://${ARTIFACTORY_URL}
CMD ["tail", "-f", "/dev/null"]

