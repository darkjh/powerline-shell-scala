// Project settings
organization := "me.juhanlol"

name := "powerline-shell-scala"

version := "0.1.0-SNAPSHOT"

// Dependencies
// JGit for interacting with Git repo
resolvers += "jgit-repository" at "http://download.eclipse.org/jgit/maven"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "3.0.0.201306101825-r"
