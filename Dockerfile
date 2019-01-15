FROM maven:3-jdk-11
MAINTAINER leo.lou@gov.bc.ca

RUN apt-get update && apt-get install -y git \
 && git clone -b ${BRANCH} ${REPO} /SRC \
 && cd /SRC && mvn clean package -Pk8s 
 
CMD ["tail", "-f", "/dev/null"]
