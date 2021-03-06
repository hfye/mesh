FROM    openjdk:10-slim

ENV ESVERSION 6.1.2

EXPOSE 8080

RUN adduser --disabled-password --disabled-login --uid 1000 --home /mesh mesh
USER mesh
WORKDIR /mesh
ADD ./target/mesh-demo*jar /mesh/meshdemo.jar

USER root
RUN mkdir /data   && chown mesh: /data -R   && ln -s /data /mesh/data
RUN mkdir /config && chown mesh: /config -R && ln -s /config /mesh/config
RUN mkdir /plugins && chown mesh: /plugins -R && ln -s /plugins /mesh/plugins

ADD https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$ESVERSION.zip /es.zip

WORKDIR /
RUN apt-get update && apt-get install -y unzip \
    && unzip /es.zip && apt-get remove -y unzip \
    && apt-get clean \
    && mv elasticsearch* elasticsearch \
    && rm /es.zip \
    && ln -s /elasticsearch /mesh/elasticsearch \
    && mkdir -p /elasticsearch/data \
    && chown mesh: /elasticsearch -R

USER mesh
WORKDIR /mesh
VOLUME /data
VOLUME /config
VOLUME /plugins
VOLUME /elasticsearch/data
VOLUME /elasticsearch/config

CMD [ "java", "-jar" , "meshdemo.jar" ]
