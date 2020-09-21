name := "AkkaRayTracing"
version := "1.0"
scalaVersion := "2.12.10"

// To whomever did this, set your JAVA_HOME enviroment variable on your system.
//javaHome := Some(file("D:\\graalvm-ce-19.2.1\\jre\\"))

run / fork := true
mainClass in (Compile, packageBin) := Some("acrt.remoting.RemotingMain")

mainClass in (Compile, run) := Some("acrt.remoting.RemotingMain")

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.6.9",
	"com.typesafe.akka" %% "akka-testkit" % "2.6.9" % Test,
	"org.scala-lang.modules" %% "scala-xml" % "1.2.0",
	"com.novocode" % "junit-interface" % "0.11" % Test,
	"org.scalactic" %% "scalactic" % "3.0.8",
	"org.scalatest" %% "scalatest" % "3.0.8" % "test",
	"org.scala-lang.modules" %% "scala-swing" % "2.1.1",
	"com.typesafe.akka" %% "akka-remote" % "2.6.9",
	"io.netty" % "netty" % "3.10.6.Final"
	//"com.typesafe.akka" %% "akka-serialization-jackson" % "2.5.24"
)

