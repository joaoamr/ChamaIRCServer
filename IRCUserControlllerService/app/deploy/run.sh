java \
-Dkafka.producer.bootstrap.servers=$CHAMA_KAFKA_PRODUCER_SERVER \
-Dkafka.consumer.bootstrap.servers=$CHAMA_KAFKA_CONSUMER_SERVER \
-Dkafka.admin.bootstrap.servers=$CHAMA_KAFKA_ADMIN_SERVER \
-Dirc.server.port=$CHAMA_IRC_SERVER_PORT \
-Dirc.server.hostname=$CHAMA_IRC_SERVER_HOSTNAME \
-jar app.jar 
