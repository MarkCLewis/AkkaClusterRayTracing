package mud

import org.junit.Test
import org.junit.Assert._
import org.junit.Before

/**
 * Test code for the room. These tests should pass at the end of the first assignment.
 * Note that you might have to edit some of them to the details of your code and your map.
 */
class TestRoom {
  private var room: Room = null
  private var eastRoom: Room = null
  
  @Before def getRoom(): Unit = {
    room = Room.rooms(0)
    eastRoom = Room.rooms(1)
  }
  
  @Test def testDescription: Unit = {
    val properDescription = """First Room
This would be the description of the first room.
Exits: North, East
Items: sword, chair
"""
    assertEquals(properDescription, room.getDescription)
  }
  
  @Test def testGetExit: Unit = {
    assertEquals(None, room.getExit(0))
    assertEquals(eastRoom, room.getExit(2).get)
  }
  
  @Test def testGetItem: Unit = {
    assertEquals(None, room.getItem("Homework"))
    val sword = room.getItem("sword").get
    assertEquals(Item("sword", "A long pointy thing to slice and dice."), sword)
    assertEquals(None, room.getItem("sword"))
  }
  
  @Test def testDropItem: Unit = {
    assertEquals(None, room.getItem("Homework"))
    val item = Item("Homework", "Fun stuff you do for Dr. Lewis.")
    room.dropItem(item)
    assertEquals(item, room.getItem("Homework"))
  }
}