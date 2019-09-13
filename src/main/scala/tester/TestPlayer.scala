package tester

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Not intended for students to edit.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.PrintStream

import akka.actor.Actor

object TestPlayer {
  case object Disconnect
  case object GetDropTest

  //def apply(n: String, i: BufferedReader, o: PrintStream, c: IOConfig): TestPlayer = {
  def apply(n: String, i: BufferedReader, o: BufferedWriter, c: IOConfig): TestPlayer = {
    new TestPlayer(n, i, o, c)
  }
}

class TestPlayer private (name: String,
    private val in: BufferedReader,
    //private val out: PrintStream,
    private val out: BufferedWriter,
    val config: IOConfig) extends Actor {

  protected var gs = Player.GameState("", Nil, Nil, Nil, Nil)
  private var commandCount = 0
  connect()
  self ! TestPlayer.GetDropTest

  def receive() = {
    case TestPlayer.Disconnect => {
      config.exitCommand().runCommand(out, in, config, gs)
      context.stop(self)
      SimpleTest.system.terminate()
    }
    case TestPlayer.GetDropTest => {
      getDropTest() match {
        case Left(command) => println("Test failed on "+command)
        case Right(b) => println("Test passed with "+b)
      }
      self ! TestPlayer.Disconnect
    }
    case _ =>
  }

  private def connect() {
    //out.println(name)
    Command.readToMatch(in, config.roomOutput) match {
      case Left(message) => println(message)
      case Right(m) =>
        val name = config.roomName.parseSingle(m)
        val exits = config.exits.parseSeq(m)
        val items = config.items.parseSeq(m)
        val occupants = config.occupants.map(_.parseSeq(m)).getOrElse(Seq.empty)
        gs = gs.copy(roomName = name, players = occupants, roomItems = items, exits = exits)
    }
  }

  private def getDropTest(): Either[Command, Boolean] = {
    /*
     * Procedure:
     * 
     * 1. Run around until an item is found.
     * 2. Grab item.
     * 3. Check item is in inventory and no longer in room.
     * 4. Drop item.
     * 5. Check item is in room and no longer in inventory.
     * 
     */

    println("Running get/drop procedure.")
    val lookCommand = config.commands.filter(_.name == "look")(0)
    val invCommand = config.commands.filter(_.name == "inventory")(0)
    val getCommand = config.commands.filter(_.name == "get")(0)
    val dropCommand = config.commands.filter(_.name == "drop")(0)
    
    lookCommand.runCommand(out, in, config, gs) match {
      case Left(message) => {
        println(message)
        return Left(lookCommand)
      }
      case Right(state) => gs = state
    }
    out.flush()
    
    lookCommand.runCommand(out, in, config, gs) match {
      case Left(message) => {
        println(message)
        return Left(lookCommand)
      }
      case Right(state) => gs = state
    }
    out.flush()

    // 1. Run around until an item is found.
    while (gs.roomItems.isEmpty) {
      if (commandCount > 1000) {
        return Left(lookCommand)
      } else {
        val command = config.randomValidMovement(gs)
        command.runCommand(out, in, config, gs) match {
          case Left(message) => return Left(command)
          case Right(state) => gs = state
        }
        commandCount += 1
      }
    }
    out.flush()

    val oldInv = gs.inventory
    val oldRoomItems = gs.roomItems
   
    // 2. Grab item.
    getCommand.runCommand(out, in, config, gs) match {
      case Left(message) => {
        println(message)
        return Left(getCommand)
      }
      case Right(state) => gs = state
    }
    out.flush()

    // 3. Check item is in inventory and no longer in room.
    lookCommand.runCommand(out, in, config, gs) match {
      case Left(message) => {
        println(message)
        return Left(lookCommand)
      }
      case Right(state) => gs = state
    }
    out.flush()
    invCommand.runCommand(out, in, config, gs) match {
      case Left(message) => {
        println(message)
        return Left(invCommand)
      }
      case Right(state) => gs = state
    }
    out.flush()
    val getInv = gs.inventory
    val getRoomItems = gs.roomItems
    //if (oldRoomItems.filterNot(getRoomItems.contains(_)) == getInv.filterNot(oldInv.contains(_))) {
    val item = getInv.filterNot(oldInv.contains(_))(0)

    println("item: " + item)
    println("old inv: " + oldInv.mkString("; "))
    println("new inv: " + getInv.mkString("; "))
    println("old room: " + oldRoomItems.mkString("; "))
    println("new room: " + getRoomItems.mkString("; "))

    if(!getRoomItems.contains(item)) {

      // 4. Drop item.
      dropCommand.runCommand(out, in, config, gs) match {
        case Left(message) => {
          println(message)
          return Left(dropCommand)
        }
        case Right(state) => gs = state
      }
      out.flush()

      // 5. Check item is in room and no longer in inventory.
      lookCommand.runCommand(out, in, config, gs) match {
        case Left(message) => {
          println(message)
          return Left(lookCommand)
        }
        case Right(state) => gs = state
      }
      out.flush()
      invCommand.runCommand(out, in, config, gs) match {
        case Left(message) => {
          println(message)
          return Left(invCommand)
        }
        case Right(state) => gs = state
      }
      out.flush()
      val dropInv = gs.inventory
      val dropRoomItems = gs.roomItems
      if (dropRoomItems.filterNot(getRoomItems.contains(_)) == getInv.filterNot(dropInv.contains(_))) {
        return Right(true)
      } else {
        return Left(dropCommand)
      }

    } else {
      return Left(getCommand)
    }
  }

}
