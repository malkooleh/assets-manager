### Getting Started
1. Run [Consul Agent](https://developer.hashicorp.com/consul/downloads) on local machine
`$ consul agent -dev`.
Consul will be available on the default local 8500 port.
2. Then compile the application with Maven `mvn clean install` command and using `java -jar ...` command. Or just build it and run using IDE.

Another way, just use Docker Compose file.
Run the following command:`$ docker-compose up --build`.
We pass the `--build` flag so Docker will compile image and then starts the containers.

[//]: # (### Project requirements)

[//]: # (![img.png]&#40;requirements.png&#41;)