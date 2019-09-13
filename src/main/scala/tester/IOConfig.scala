package tester

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Not intended for students to edit.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import scala.collection.Seq
import scala.util.matching.Regex
import scala.xml.XML

case class IOConfig(
    numCommandsToGive: Int,
    commands: Seq[Command],
    roomOutput: Regex,
    inventoryOutput: Regex,
    roomName: IOElement,
    occupants: Option[IOElement],
    exits: IOElement,
    items: IOElement,
    invItems: IOElement,
    testProcs: Map[String, Boolean]) {

  private def validArgs(com: Command, state: Player.GameState): Boolean = {
    com.args.forall(_.isValidForState(state))
  }

  // TODO This is just randomValidMovement + exit?
  def randomValidCommand(state: Player.GameState): Command = {
    val com = commands(util.Random.nextInt(commands.length))
    if ((com.isMovement && !state.exits.contains(com.name)) || !validArgs(com, state)) {
      randomValidCommand(state)
    } else com
  }

  def randomValidMovement(state: Player.GameState): Command = {
    val moves = commands.filter(_.isMovement)
    val com = moves(util.Random.nextInt(moves.length))
    if (com.isTerminator || !state.exits.contains(com.name) || !validArgs(com, state)) {
      randomValidMovement(state)
    } else com
  }

  def exitCommand(): Command = {
    commands.find(_.isTerminator).get
  }
}

case class IOElement(groupNumber: Int, separator: Option[String], pattern: Regex) {
  def parseSingle(m: Regex.Match): String = {
    val found = m.group(groupNumber)
    Debug.regexDebugPrint(1, "Looking for output group " + groupNumber + " using pattern " + pattern + ", found \"" + found + "\".")
    found
  }
  def parseSeq(m: Regex.Match): Seq[String] = {
    if (separator.isEmpty) throw new UnsupportedOperationException("No separator for element with parseSeq.")
    val str = m.group(groupNumber)
    Debug.regexDebugPrint(2, s"str = $str")
//    println(s"str = $str")
    if (str == null) Seq.empty else {
      val found = str.split(separator.get).map { part =>
        Debug.regexDebugPrint(2, s"Match $part against $pattern")
//        println(s"Match $part against $pattern")
        pattern.findFirstMatchIn(part) match {
          case None => throw new IllegalArgumentException("Part in parseSeq didn't match pattern.")
          case Some(m) => m.group(1)
        }
      }
      Debug.regexDebugPrint(1, "Looking for output group " + groupNumber + " using pattern " + pattern + ", found \"" + found.mkString("\", \"") + "\".")
      found
    }
  }
}

object IOConfig {
  def apply(configFile: String): IOConfig = {
    val xml = XML.loadFile(configFile)
    val numCommandsToGive = (xml \ "numCommandsToGive").text.toInt
    val commands = (xml \ "commands" \ "command").flatMap { n =>
      val enabled = (n \ "@enabled").text
      if (enabled == "true") {
        val name = (n \ "@name").text
        val output = (n \ "@output").text
        val movement = (n \ "@movement").text == "true"
        output match {
          case "room" => Seq(RoomParsing(name, (n \ "argument").map(CommandArgument.apply), movement))
          case "inventory" => Seq(InvParsing(name, (n \ "argument").map(CommandArgument.apply)))
          case "unparsed" => Seq(Unparsed(name, (n \ "argument").map(CommandArgument.apply), false))
          case "terminate" => Seq(Unparsed(name, (n \ "argument").map(CommandArgument.apply), true))
          case _ => Nil
        }
      } else {
        Nil
      }
    }
    //println(commands)
    val roomOutput = (xml \ "output" \ "roomOutput").text.trim
    val inventoryOutput = (xml \ "output" \ "inventoryOutput").text.trim
    val roomName = parseElement(xml \ "output" \ "roomName")
    val occupants = (xml \ "output" \ "occupants").headOption.map(parseElement)
    val exits = parseElement(xml \ "output" \ "exits")
    val items = parseElement(xml \ "output" \ "items")
    val invItems = parseElement(xml \ "output" \ "invItems")
    val testProcs = Array("getDrop").map { proc =>
      (proc -> ((((xml \ "tests") \ proc) \ "@enabled").text.trim == "true"))
    }.toMap

    Debug.regexDebugLevel = (((xml \ "debug") \ "regexDebug") \ "@level").text.trim.toInt
    Debug.playerDebugLevel = (((xml \ "debug") \ "playerDebug") \ "@level").text.trim.toInt
    Debug.roomDebugLevel = (((xml \ "debug") \ "roomDebug") \ "@level").text.trim.toInt
    Debug.monitoredRooms = ((((xml \ "debug") \ "roomDebug") \ "monitoredRooms") \ "monitoredRoom").map(_.text.trim)

    try {
      new IOConfig(numCommandsToGive, commands, roomOutput.r, inventoryOutput.r, roomName, occupants, exits, items, invItems, testProcs)
    } catch {
      case e: java.util.regex.PatternSyntaxException => {
        println("Illegal Regex syntax:\n " + e.getDescription + "\n" + e.getPattern)
        sys.exit(1)
      }
    }
  }

  def parseElement(n: xml.NodeSeq): IOElement = {
    val group = (n \ "@group").text.trim.toInt
    val separator = (n \ "@separator").headOption.map(_.text)
    val text = n.text
    val pattern = if (text.isEmpty) "(.*)" else text
    IOElement(group, separator, pattern.r)
  }

}
