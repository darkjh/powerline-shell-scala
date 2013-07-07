import AssemblyKeys._

// Project settings
organization := "me.juhanlol"

name := "powerline-shell-scala"

version := "0.1.0-SNAPSHOT"

// sbt-assembly plugin settings
assemblySettings

// Dependencies
// JGit for interacting with Git repo
resolvers += "jgit-repository" at "http://download.eclipse.org/jgit/maven"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "3.0.0.201306101825-r"

// ScalaTest
libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

// Akuma
libraryDependencies += "org.kohsuke" % "akuma" % "1.9"