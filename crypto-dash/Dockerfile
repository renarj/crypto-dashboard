FROM adoptopenjdk/openjdk11:latest
VOLUME /tmp

RUN apt-get update && apt-get install -y netcat

RUN echo ${BSH_SCRIPT}
RUN echo ${JAR_FILE}

ARG BSH_SCRIPT
ADD ${BSH_SCRIPT} start.sh
RUN chmod +x start.sh

ARG JAR_FILE
ADD ${JAR_FILE} crypto-dash.jar

RUN bash -c 'touch /crypto-dash.jar'

CMD ["/start.sh"]