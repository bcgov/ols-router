FROM jenkins/core/there

USER 0
COPY ./contrib/jenkins/configuration $JENKINS_REF_HOME

RUN set -x && \
    chgrp -R 0 $JENKINS_REF_HOME && \
    chmod -R 644 $JENKINS_REF_HOME && \
    chmod -R g+rwX $JENKINS_REF_HOME

RUN pushd /usr/local/bin && \
    curl -s -O https://dl.min.io/client/mc/release/linux-amd64/mc && \
    chmod +x mc && \
    popd

USER 1001
