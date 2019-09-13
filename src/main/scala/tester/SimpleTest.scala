package tester

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Not intended for students to edit.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import scala.sys.process._
import java.io.OutputStream
import java.io.InputStream
import java.io.BufferedReader
import java.io.PrintStream
import java.io.InputStreamReader
import akka.actor.ActorSystem
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import akka.actor.Props

object SimpleTest extends App {
  val process = java.lang.Runtime.getRuntime.exec("sbt run".split(" "))
  val is = new BufferedReader(new InputStreamReader(process.getInputStream))
  val os = new BufferedWriter(new OutputStreamWriter(process.getOutputStream))
  
  val system = ActorSystem("SimpleTest")
  val ioConfig = IOConfig("config.xml")
  val player = system.actorOf(Props(TestPlayer("test_player",is,os,ioConfig)),"test_player")
}