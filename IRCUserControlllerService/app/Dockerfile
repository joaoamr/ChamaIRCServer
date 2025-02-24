FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY /build/libs/app.jar ./
COPY /deploy/run.sh ./

RUN apt-get update ; apt-get -y install dos2unix
RUN dos2unix run.sh 
RUN chmod +x run.sh

EXPOSE 6667

ENTRYPOINT ["bash", "-c", "./run.sh"]