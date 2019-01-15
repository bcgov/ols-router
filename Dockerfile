FROM maven:3-jdk-11
MAINTAINER leo.lou@gov.bc.ca

RUN apt-get update && apt-get install -y git \
 && mkdir /SRC && git clone -b master https://github.com/cmhodgson/ols-router.git . \
 && mvn clean package -Pk8s 
 
CMD ["tail", "-f", "/dev/null"]
