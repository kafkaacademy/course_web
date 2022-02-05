
extra and optional Kafka Manager from yahoo:
(see kafka_manager on internet)

  kafka_manager:
    image: hlebalbau/kafka-manager:stable
    container_name: kafka_manager    
    ports:
      - "9000:9000"
    environment:
      ZK_HOSTS: "zookeeper:2181"
      APPLICATION_SECRET: "random-secret"  
