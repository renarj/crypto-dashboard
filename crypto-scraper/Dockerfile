FROM adoptopenjdk:latest
VOLUME /tmp

RUN apt-get update && apt-get install -y netcat

RUN apt-get install -y kafkacat

RUN echo ${BSH_SCRIPT}
RUN echo ${JAR_FILE}

ARG BSH_SCRIPT=src/main/resources/start.sh
ADD ${BSH_SCRIPT} start.sh
RUN chmod +x start.sh

ARG JAR_FILE=target/crypto-scraper.jar
ADD ${JAR_FILE} crypto-scraper.jar

RUN bash -c 'touch /crypto-scraper.jar'

CMD ["/start.sh"]