# Run "mvn package" on the maven project in "parent" directory before building the dockerfile
FROM openjdk:latest
# we are expecting the following arguments from env-file at runtime 
#(can be tested with "docker run --env-file=local_env_file -it <tag>")
ARG REQUEST_QUEUE_URL
ARG RESPONSE_QUEUE_URL
ARG COLLATERAL_QUEUE_URL
# required libraries
RUN apt update && apt install libpcap0.8 && apt-get install xvfb -y
# VICE 3.3 pre-built binaries
COPY docker/usr /usr
# VICE configuration file
COPY docker/vicerc /root/.config/vice/vicerc
WORKDIR /usr/src/breakout
COPY Breakout/breakout.prg .
# Pre-built maven packages
# This is a shaded jar with all AWS libraries and also breakout:main jar
COPY parent/aws/target/aws-0.0.1-SNAPSHOT.jar .
ENV VICE_COMMAND=/usr/local/bin/x64
ENV PRG_LOCATION=/usr/src/breakout/breakout.prg
ENV REQUEST_QUEUE_URL=${REQUEST_QUEUE_URL}
ENV RESPONSE_QUEUE_URL=${RESPONSE_QUEUE_URL}
ENV COLLATERAL_QUEUE_URL=${COLLATERAL_QUEUE_URL}
ENV AWS_REGION=${AWS_REGION}
ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
ENV DISPLAY=:1.0
CMD (Xvfb :1 -screen 0 1024x768x16 &> xvfb.log &) && java -Xms100m -Xmx100m -cp ".:aws-0.0.1-SNAPSHOT.jar" karski.breakout.RequestProcessor karski.breakout.sqs.SQSRequestReceiver karski.breakout.sqs.SQSResponseSender