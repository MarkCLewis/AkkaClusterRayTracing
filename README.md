# Akka-Clustered Ray Tracing

This is a part of a large research project studying Akka-based clustered raytracing of large geometric scenes. It served as the basis for the papers listed below. Significant credit goes to Dr. Mark C. Lewis's SwiftVis2 library, whose Geometry primitives are used as the basis of creating the geometry, as well as whose raytracing libraries serve as the basis off which this was designed.

The current structure of the project mirrors the structure used by sbt. Inside the lib directory is .jar files of ScalaRingsCode and SwifVis2, specifically the Core, JVM, and Swing packages. These are used for the Geometry primitives pulled in by the project.

The current versions of these are:

    ScalaRingsCode 2.12 0.1.0
    SwiftVis2 2.12 0.1.0
  
## Current Functionality

This project currently supports local actorized raytracing, clustered actorized raytracing across a local network, multiple serializers and transport protocols, as well as photometric rendering locally and across a network.

For local raytracing, run:

    sbt runMain acrt.raytracing.untyped.Main

For local photometry, run:

    sbt runMain acrt.photometry.untyped.Main

In order to create a clustered raytrace, first edit src/main/scala/acrt/cluster/untyped/frontend/raytracing/FrontendNode.scala to tell it how many Backend nodes to expect, as well as add the ip and port number of all machines to src/main/scala/acrt/cluster/untyped/Main.scala, then, on each machine except the head node, run:

    sbt runMain acrt.cluster.untyped.Main backend ip port machinenumber

filling in the ip and port of the machine you are using, and using a unique machine number from 0 to numBackend for each. After all are created, run the following on the head node:

    sbt runMain acrt.cluster.untyped.Main frontend ip port

similarly filling in the ip and port of this machine.

There are several different config files that can be loaded in to support standard Java serialization, Jackson, and Kryo serializers. To change transport protocol between TCP and UDP, change the lines seen in acrt.cluster.untyped.Main.

## Future Functionality

This project is entirely dependent on Classic Cluster and untyped Actors from Akka. This is being phased out over time, and Akka has since introduced Akka Typed, which includes Typed Actors and the new Typed Cluster. As such, this should eventually support both types of Actors, typed and untyped, and though there are stubbed entries in each folder for a typed version, this typed version does not, as yet, exist and will likely be added at some point in the future.

## Papers
Clustered Visualisation of Large Scenes using Actors
  
A Study in Akka-based Distributed Raytracing of Large Scenes
  
Distributed Raytracing of Large Scenes using Actors

## Contributors

Kurt Hardee
  
Dr. Mark Lewis
  
Elizabeth Ruetschle
  
## Credit

https://github.com/MarkCLewis/SwiftVis2

https://akka.io/
