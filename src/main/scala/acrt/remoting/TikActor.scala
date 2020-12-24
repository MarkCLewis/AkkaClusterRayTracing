package acrt.remoting

import akka.actor._
import akka.remote.RemoteScope

class TikActor extends Actor {
  //val selection = context.actorSelection("akka://TestSystem@127.0.0.1:2553/user/TokActor")
  
  private val nodes = "0 1 2 3 4 5 6 7 8".split(" ").map("131.194.71.13" + _.trim)

  val playerManagers = nodes.map { ip =>
    println("make address" + ip)
      val address = Address("akka", "TestSystem", ip, 5150)
    println("address " + ip)
      context.system.actorOf(Props(new TokActor).withDeploy(Deploy(scope = RemoteScope(address))), s"TokActor_$ip")
  }
  
  def receive: Actor.Receive = {
    case "start" => {
      playerManagers.foreach{pm => pm ! "tik"; println(s"sent tik to $pm")}
    }
    case "tok" => {
      Thread.sleep(5000)
      sender ! "tik"
      println("tik")
    }
    case m => println("Got message " + m)
  }
}
