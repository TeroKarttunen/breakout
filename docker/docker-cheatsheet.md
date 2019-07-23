# docker cheat sheet

### build docker container image and tag it
docker build --tag=requestprocessor .

### start a number of containers (you need to edit docker-compose.yml to change the image name to whatever you have tagged it)
### alternatively you can use docker-compose-aws.yml as template
docker stack deploy -c docker-compose.yml requestprocessors
### stop the containers
docker stack rm requestprocessors

### view information on what is going on
docker stats
docker stack ps requestprocessor
docker container ls
docker service ps requestprocessor

### run the image interactively (in command shell), for example for debugging purposes
docker run --env-file=local_env_file -it requestprocessor
docker run --env-file=local_env_file -it requestprocessor bash

### get login credentials from ECR
aws ecr get-login --no-include-email

### push the container image to Amazon Elastic Container Registry (ECR), you must login first (see previous command)
docker tag requestprocessor 766719651372.dkr.ecr.eu-north-1.amazonaws.com/breakout:0.1
docker push 766719651372.dkr.ecr.eu-north-1.amazonaws.com/breakout

