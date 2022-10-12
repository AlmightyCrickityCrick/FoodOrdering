# FoodOrdering
The following project is part of the laboratory number 2 for Network Programming Course. The objective of the laboratory work is to simulate a food ordering environment within Docker Containers using Threads.

To run this project it is recommended to rebuild the FatJar using the command.

```
./gradlew :buildFatJar  
```    

And then use the commands below to create the image and run the docker container. This container can be run before kitchen-container, but must always precede any dining-containers or client-containers.

```
docker network create lina-restaurant network
docker build -t foodordering .     
docker run --name food-ordering --network lina-restaurant-network  -p 8088:8088 foodordering
```
