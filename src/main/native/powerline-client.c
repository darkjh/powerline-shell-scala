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

char* prepare_msg(const char *env_var) {
  int len = strlen(env_var);
  char* msg = (char*)malloc((len+2)*sizeof(char));
  strcpy(msg, env_var);
  msg[len] = '\n';    // add '\n' since server side use readLine()
  msg[len+1] = '\0';  // now terminate the string with '\0'

  return msg;
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

  /* get current pwd and return code */
  char* pwd_msg = prepare_msg(getenv(PWD));
  char* ret_msg = prepare_msg(argv[1]);

  /* write: send the messages to the server */
  if (write(sockfd, pwd_msg, strlen(pwd_msg)) < 0)
    error("ERROR writing to powerline server");
  if (write(sockfd, ret_msg, strlen(ret_msg)) < 0)
    error("ERROR writing to powerline server");

  free(pwd_msg);
  free(ret_msg);

  /* read: print the server's reply */
  bzero(buf, BUFSIZE);
  if (read(sockfd, buf, BUFSIZE) < 0)
    error("ERROR reading from powerline server");
  printf("%s", buf);
  close(sockfd);
  return 0;
}