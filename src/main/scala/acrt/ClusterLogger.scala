package acrt

//Credit to johanandren
/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.typesafe.com>
 */

import akka.actor.{Actor, ActorLogging, Props}


object ClusterLogger {
  def props(messageTransformer: Option[PartialFunction[Any, String]] = None) =
    Props(new ClusterLogger(messageTransformer))

}

class ClusterLogger(messageTransformer: Option[PartialFunction[Any, String]]) extends Actor with ActorLogging {
  override def receive: Receive = {
    case message =>
      val logEntry = messageTransformer.flatMap(transformer =>
        transformer.lift(message)
      ).getOrElse(s"Saw message: [$message]")

      log.info(logEntry)
  }
}
