FROM findepi/graalvm
RUN apt-get update
RUN apt-get install -y vim bash curl wget gnupg apt-transport-https apt-utils lsb-release

RUN wget http://dist.neo4j.org/neo4j-enterprise-3.4.5-unix.tar.gz
RUN tar zxvf neo4j-enterprise-3.4.5-unix.tar.gz
RUN mv neo4j-enterprise-3.4.5 /var/lib/neo4j

ENV PATH /var/lib/neo4j/bin:$PATH

VOLUME /data

RUN addgroup neo4j && adduser neo4j --ingroup neo4j --system --shell /bin/bash --disabled-password

COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN ln -s /var/lib/neo4j/plugins /plugins
RUN ln -s /var/lib/neo4j/logs /logs
RUN ln -s /var/lib/neo4j/import /import
RUN ln -s /var/lib/neo4j/metrics /metrics
RUN ln -s /var/lib/neo4j/conf /conf

EXPOSE 7474 7473 7687

ENTRYPOINT ["/bin/bash", "/docker-entrypoint.sh"]
CMD ["neo4j"]

