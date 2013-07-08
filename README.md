powerline-shell-scala
=====================

A re-implementation of powerline-shell in Scala

![powerline-shell-scala](http://imgur.com/9SVFfUw.png)

I was always using the powerling-shell created by [milkbikis](https://github.com/milkbikis) and I like it very much. But since it's a python script, sometimes it reponses slowly, maybe there's a way to make it fast but I decided to re-implement it using Scala, in a different structure.

The original python script is executed every time the shell prompt is evaluated. Since I implement it here using scala, it can't fit into the same structure because running a scala program means starting a java VM first, so it's not suitable for lots of short lived execution. For this reason, powerline-shell-scala is done in a client-server structure. There's a native client written in C, which sends all information needed over a socket to a local scala server, run as a system daemon. The result? I get a new shell prompt instantly.

## Features ##

It has all the basic features of powerline-shell: nice path segments rendering, git branches, error return code etc. It also adapts the prompt length according to the actual console window width: you have always 1/3 the place to type your commands.

## Installation ##

You need to have gcc, scala and sbt to compile. Just type

`make all`

to compile. The compiled client is `target/powerline-client`, and the server jar is `target/scala-{version}/powerline-shell-scala-assembly-0.1.0-SNAPSHOT.jar`.

To use it, add this function in you `.bashrc` and then override the prompt command:

```sh
function _update_ps1() {
    export PS1="$(PATH/TO/powerline-shell-scala/target/powerline-client $?)"
}
export PROMPT_COMMAND="_update_ps1 && other_stuff_that_you_had_before"
```

Dont foget to run the server daemon at startup, you can put the command in `.xinitrc`:

```sh
java -Xmx50m -cp PATH/TO/powerline-shell-scala/target/scala-{version}/powerline-shell-scala-assembly-0.1.0-SNAPSHOT.jar PowerlineServer
```

which will launch the server with maximum 50Mb of memory at startup.

## Fonts ##

As the original one, powerline uses some special symbols that maybe not included in your font. Consult [this post](https://github.com/Lokaltog/vim-powerline/wiki/Patched-fonts) for patching your favorite font.

## Other implementations ##

- [original vim version](https://github.com/Lokaltog/powerline)
- [milkbikis' port to shell](https://github.com/milkbikis/powerline-shell)
- [a javascript version](https://github.com/ceejbot/powerline-js)

## TODO ##

- supports for other shells
- supports for other CVS
