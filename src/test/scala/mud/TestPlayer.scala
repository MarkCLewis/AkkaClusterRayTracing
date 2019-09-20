/*package mud

import org.junit._
import org.junit.Assert._

class TestPlayer {
  private var player: Player = null
  private var sword: Item = null
  
  @Before def getRoom(): Unit = {
    sword = Item("sword", "A long pointy thing to slice and dice.")
    player = new Player(Room.rooms(0), List(sword))
  }
  
  @Test def testGetFromInventory: Unit = {
    assertEquals(None, player.getFromInventory("Homework"))
    val gottenSword = player.getFromInventory("sword").get
    assertEquals(sword, gottenSword)
    assertEquals(None, player.getFromInventory("sword"))
  }
  
  @Test def testAddToInventory: Unit = {
    assertEquals(None, player.getFromInventory("Homework"))
    val item = Item("Homework", "Fun stuff you do for Dr. Lewis.")
    player.addToInventory(item)
    assertEquals(item, player.getFromInventory("Homework"))
  }
}*/