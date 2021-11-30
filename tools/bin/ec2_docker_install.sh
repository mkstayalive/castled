# !/usr/bin/env bash

if [ -x "$(command -v docker)" ]; then
    echo "docker already avaliable!"
    # command
else
    echo "Installing docker..."
	sudo yum update -y
	sudo yum install -y docker
	sudo service docker start
	sudo usermod -a -G docker $USER
	docker --version
fi

if [ -x "$(command -v docker-compose)" ]; then
    echo "docker-compose already avaliable!"
    # command
else
    echo "Installing docker-compose..."  
	sudo wget https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m) -O /usr/local/bin/docker-compose
	sudo chmod +x /usr/local/bin/docker-compose
	docker-compose --version
fi
