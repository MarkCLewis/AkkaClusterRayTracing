package tester

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// Not intended for students to edit.
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import scala.collection.Seq

object Debug {
  var regexDebugLevel = 0
  var playerDebugLevel = 0
  var roomDebugLevel = 0
  var monitoredRooms = Seq[String]()

  def regexDebugPrint(level: Int, s: String) {
    if (level <= regexDebugLevel) println(s)
  }

  def playerDebugPrint(level: Int, s: String) {
    if (level <= playerDebugLevel) println(s)
  }

  def roomDebugPrint(level: Int, room: String, s: String) {
    if (level <= roomDebugLevel && monitoredRooms.contains(room)) println(s)
  }
}
