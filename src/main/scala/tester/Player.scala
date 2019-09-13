package tester

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Not intended for students to edit.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

object Player {
  case class GameState(roomName: String, val inventory: Seq[String], val players: Seq[String], val roomItems: Seq[String], val exits: Seq[String])

  case object Connect
  case object Disconnect
  case object KillPlayer
}
