/*
 * Client for powerline-shell server
 * Compiled native client run much faster then interpreted or vm code
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define BUFSIZE 2048
#define PORT 18888
#define HOST "localhost"
#define PWD "PWD"

/*
 * error - wrapper for perror
 */
void error(char *msg) {
  perror(msg);
  exit(0);
}

int main(int argc, char **argv) {
  int sockfd, portno, n;
  struct sockaddr_in serveraddr;
  struct hostent *server;
  char buf[BUFSIZE];

  /* socket: create the socket */
  sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if (sockfd < 0)
    error("ERROR opening socket");

  /* gethostbyname: get the server's DNS entry */
  server = gethostbyname(HOST);
  if (server == NULL) {
    fprintf(stderr,"ERROR, no such host as %s\n", HOST);
    exit(0);
  }

  /* build the server's Internet address */
  bzero((char *) &serveraddr, sizeof(serveraddr));
  serveraddr.sin_family = AF_INET;
  bcopy((char *)server->h_addr,
	(char *)&serveraddr.sin_addr.s_addr, server->h_length);
  serveraddr.sin_port = htons(PORT);

  /* connect: create a connection with the server */
  if (connect(sockfd, &serveraddr, sizeof(serveraddr)) < 0)
    error("ERROR connecting");

  /* get current pwd */
  char* pwd = getenv(PWD);
  int len = strlen(pwd);
  char* msg = (char*)malloc((len+1)*sizeof(char));
  strcpy(msg, pwd);
  msg[len] = '\n';

  /* write: send the message line to the server */
  n = write(sockfd, msg, strlen(msg));
  free(msg);
  if (n < 0)
    error("ERROR writing to socket");

  /* read: print the server's reply */
  bzero(buf, BUFSIZE);
  n = read(sockfd, buf, BUFSIZE);
  if (n < 0)
    error("ERROR reading from socket");
  printf("%s", buf);
  close(sockfd);
  return 0;
}