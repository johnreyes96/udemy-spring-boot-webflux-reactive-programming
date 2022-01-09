# Projects reactives made with Spring Boot 2 and Spring WebFlux
This repository is about udemy curse Programación Reactiva con Spring Boot 2 y Spring WebFlux based with reactor project.

## Tecnologies and tools
* Java 17 [download here](https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
* Spring Boot 2
* Spring WebFlux
* Maven
* MongoDB (NoSQL) [download here](https://www.mongodb.com/try/download/community)
* Spring Tools IDE (Eclipse) [download here](https://spring.io/tools)
* Robo 3T (Optional) [download here](https://robomongo.org/)

## Installation
To install and run this project follow the steps below:

Add settings in the system environment variables

PATH:

```
C:\Program Files\Java\jdk-17.0.1\bin
```

JAVA_HOME:

```
C:\Program Files\Java\jdk-17.0.1
```

Open cmd and run the following commands to verify the jdk installation:
```
java -version
```
&
```
javac -version
```

Install Spring Tools IDE (4.xx.x)

Install MongoDB compass 1.xx.x or top

PATH:

```
C:\Program Files\MongoDB\Server\5.0\bin
```

Open cmd and run the following commands to verify the MongoDB installation:

```
mongo -version
```

### Optional

Install Robo 3T and add MongoDB connections (also in MongoDB compass):

```
localhost:27017
```

## How to clone
* ```git clone repository```
* In Spring Tools > Import Projects from File System or Archive > Directory... > Select All > Finish
* Right click on any project > Maven > Update Project... > Select All > OK
* Run projects

### Notes
In the spring-boot-webflux-client project, to add new server instances that are located on the Eureka server, the following step must be carried out:

* Run As > Run Configurations... > Arguments > VM arguments: -Dserver.port=XXXX
