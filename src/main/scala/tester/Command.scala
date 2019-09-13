package tester

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Not intended for students to edit.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.PrintStream

import scala.Left
import scala.Right
import scala.annotation.tailrec
import scala.collection.Seq
import scala.util.matching.Regex

object Command {
  //def sendCommand(out: PrintStream, name: String, args: Seq[CommandArgument], currentState: Player.GameState): Unit = {
  def sendCommand(out: BufferedWriter, name: String, args: Seq[CommandArgument], currentState: Player.GameState): Unit = {
    val com = (name + " " + args.map(_(currentState)).mkString(" ")).trim + "\n"
    println("Sending "+com)
    //out.println(com)
    out.write(com)
    out.flush()
    Thread.sleep(100)
  }
  def readToMatch(in: BufferedReader, regex: Regex): Either[String, Regex.Match] = {
    @tailrec
    def helper(input: String, cnt: Int): Either[String, Regex.Match] = {
//      println(s"$cnt $input")
      if (cnt > 10) Left("Couldn't match room output:\n" + input)
      else {
//        println("About to read.")
        val line = in.readLine()
        println("Read: "+line)
        if(input.nonEmpty || !line.contains("info")) {
          val input2 = input+"\n"+line
          val om = regex.findFirstMatchIn(input2)
          if (om.isEmpty) helper(input2, cnt + 1) else {
//            println(s"Matched at $cnt: $input2")
            Right(om.get)
          }
        } else helper(input, cnt)
      }
    }
    helper("", 0)
  }
}

sealed trait Command {
  val isTerminator: Boolean
  val isMovement: Boolean
  val name: String
  val args: Seq[CommandArgument]
  //def runCommand(out: PrintStream, in: BufferedReader, config: IOConfig, currentState: Player.GameState): Either[String, Player.GameState]
  def runCommand(out: BufferedWriter, in: BufferedReader, config: IOConfig, currentState: Player.GameState): Either[String, Player.GameState]
}

case class RoomParsing(val name: String, args: Seq[CommandArgument], isMovement: Boolean) extends Command {
  val isTerminator = false
  //def runCommand(out: PrintStream, in: BufferedReader, config: IOConfig,
  def runCommand(out: BufferedWriter, in: BufferedReader, config: IOConfig,
    currentState: Player.GameState): Either[String, Player.GameState] = {
    Command.sendCommand(out, name, args, currentState)
    Command.readToMatch(in, config.roomOutput) match {
      case Left(message) => Left(message)
      case Right(m) =>
        val name = config.roomName.parseSingle(m)
        val exits = config.exits.parseSeq(m)
        val items = config.items.parseSeq(m)
        val occupants = config.occupants.map(_.parseSeq(m)).getOrElse(Seq.empty)
        println(s"Got room:\n$name\n$exits\n$items\n$occupants\n")
        Right(currentState.copy(roomName = name, players = occupants, roomItems = items, exits = exits))
    }
  }
}

case class Unparsed(val name: String, args: Seq[CommandArgument], isTerminator: Boolean) extends Command {
  val isMovement = false
  //def runCommand(out: PrintStream, in: BufferedReader, config: IOConfig,
  def runCommand(out: BufferedWriter, in: BufferedReader, config: IOConfig,
    currentState: Player.GameState): Either[String, Player.GameState] = {
    Command.sendCommand(out, name, args, currentState)
    Right(currentState)
  }
}

case class InvParsing(val name: String, args: Seq[CommandArgument]) extends Command {
  val isTerminator = false
  val isMovement = false
  //def runCommand(out: PrintStream, in: BufferedReader, config: IOConfig,
  def runCommand(out: BufferedWriter, in: BufferedReader, config: IOConfig,
    currentState: Player.GameState): Either[String, Player.GameState] = {
    Command.sendCommand(out, name, args, currentState)
    Command.readToMatch(in, config.inventoryOutput) match {
      case Left(message) => Left(message)
      case Right(m) =>
//        println("Inv matched "+m)
        val invItems = config.invItems.parseSeq(m)
        Right(currentState.copy(inventory = invItems))
    }
  }
}
