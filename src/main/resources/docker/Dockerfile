FROM ubuntu:16.04

MAINTAINER Christopher Johnson <christopher_hanna.johnson@uni-leipzig.de>
LABEL description = "Provides a Camel Kafka File Serializer"

ENV DYNAMO_VERSION 0.1.1-SNAPSHOT
ENV DYNAMO_RUNTIME /opt

ENV JVM_BASE /usr/lib/jvm
ENV JDK_JAVA_OPTIONS="-Xms250m -Xmx1024m"
ENV JDK_SPEC 10
ENV JDK_VERSION 10
ENV JAVA_HOME ${JVM_BASE}/jdk-${JDK_VERSION}
ENV JDK_ARCHIVE openjdk-${JDK_VERSION}_linux-x64_bin.tar.gz
ENV PATH ${JAVA_HOME}/bin:$PATH

RUN apt-get update && \
    apt-get -y install \
    wget

RUN mkdir ${JVM_BASE} && \
    wget -O ${JVM_BASE}/${JDK_ARCHIVE} https://download.java.net/java/GA/jdk${JDK_SPEC}/${JDK_VERSION}/binaries/${JDK_ARCHIVE} && \
    cd ${JVM_BASE} && \
    tar -xzf ${JDK_ARCHIVE} && \
    java --version && \
    rm -rf /var/lib/apt/lists/* && apt-get purge -y

WORKDIR ${DYNAMO_RUNTIME}
COPY *.tar .
RUN tar -xvf *.tar && \
mv dynamo-${DYNAMO_VERSION} dynamo && \
chmod +x ${DYNAMO_RUNTIME}/dynamo/bin/dynamo
COPY entrypoint.sh .
#COPY trellis.crt .
#RUN /usr/lib/jvm/jdk-10/bin/keytool -cacerts -storepass changeit -noprompt -import -alias trellis -file trellis.crt
RUN chmod +x entrypoint.sh
ENTRYPOINT [ "./entrypoint.sh" ]