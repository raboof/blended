#!/bin/bash 

set -e 

export DOCKER_HOST=127.0.0.1
export DOCKER_PORT=2375 

source /home/blended/.nvm/nvm.sh
nvm use 4.2.6

node --version 

nohup sudo /usr/bin/dockerd -H 127.0.0.1:2375 &> /tmp/docker.out &

cd ~/project 

mvn clean install -P build,docker,itest -Ddocker.host=$DOCKER_HOST -Ddocker.port=$DOCKER_PORT | grep -v -i "download" | grep -v -i "CheckForNull" | grep -v -i "longer than 100 characters"
