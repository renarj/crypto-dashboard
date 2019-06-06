FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp

RUN apt-get update && apt-get install -y netcat

RUN apt-get install -y kafkacat

RUN echo ${BSH_SCRIPT}
RUN echo ${JAR_FILE}

ARG BSH_SCRIPT
ADD ${BSH_SCRIPT} start.sh
RUN chmod +x start.sh

ARG JAR_FILE
ADD ${JAR_FILE} kraken-producer.jar

RUN bash -c 'touch /kraken-producer.jar'

CMD ["/start.sh"]