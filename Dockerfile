FROM maven:3-jdk-11
MAINTAINER leo.lou@gov.bc.ca

RUN apt-get update && apt-get install -y git \
 && wget /root/.m2/settings.xml https://raw.githubusercontent.com/bcgov/ols-router/tools/settings.xml \
 && git clone -b ${BRANCH} ${REPO} /SRC \
 && cd /SRC && mvn clean package -Pk8s \
 && mv /SRC/*/target/*.war / \
 && ls /*.war
 
CMD ["tail", "-f", "/dev/null"]
