# [Chama! Irc Server] User Controller Service
----------------------

A service responsible to control IRC sockets for clients connections.

## Depends on

- JDK 17 + Gradle wrapper

- Apache Kafka

## Functionalities:

- Nick set

- Private message

- MOTD

- Client\Server connection check Ping

## System properties

- **kafka.producer.bootstrap.servers:** Kafka address and port for the producers. Default: localhost:9092 
- **kafka.consumer.bootstrap.servers:** Kafka address and port for the consumers. Default: localhost:9092
- **kafka.admin.bootstrap.servers:** Kafka address and port for administration. Default: localhost:9092
- **irc.server.port:** IRC socket server port. Default: 6667
- **irc.server.hostname:** IRC Server hostname. Default: chama.irc.server
- **irc.server.nick.fobidden.characters:** Forbidden characters that a nick may have. Default: @;,:!+#
- **irc.server.nick.max.len:** Maximum length of a nick. Any nick bigger than this will be truncated. Default: 20

## Test with docker

To test a complete environment, run the compose.yaml model. However, if you just want to up the docker for development and local debugging, run the compose-only-broker.yaml.

To set up a MOTD message, post the messages in to the topic *server-motd*. Check out the compose file for the example.

The tests were made with mIRC and xChat. To reproduce the test, just choose an IRC client and connect to some of the instances from the compose (*localhost:6667* or *localhost:6668*). You might connect more than one client to any server and input */msg TargetNick message*. The message must be send to the client with *TargetNick*

You are also able to change your nick after the register. Once you do that, the server notify all the network client that the user changed its nick. However, only mIRC commir the exchange. For instance, if you have a private chat active with user *chama*, and this user set its nick to *chama-2*, mIRC renames the chat window title to the new nick.





