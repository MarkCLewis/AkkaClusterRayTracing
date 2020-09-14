package acrt.remoting

import akka.actor.Actor

class TokActor extends Actor {
  val selection = context.actorSelection("akka.tcp://TestSystem@131.194.161.51:2552/user/TikActor")
  def receive: Actor.Receive = {
    case "tik" => {
      Thread.sleep(5000)
      selection ! "tok"
      println("tok")
    }
    case m => println("Got message " + m)
  }
}
