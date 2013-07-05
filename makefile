NATIVE=src/main/native/
CLIENT_SRC=powerline-client.c
TARGET=target/

all: client server

client: $(NATIVE)$(CLIENT_SRC)
	gcc $(NATIVE)$(CLIENT_SRC) -o $(TARGET)powerline-client

server:
	sbt assembly