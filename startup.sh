#!/bin/bash

nohup ssh pandora01.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora01 0' &
nohup ssh pandora02.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora02 1' &
nohup ssh pandora03.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora03 2' &
nohup ssh pandora04.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora04 3' &
nohup ssh pandora05.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora05 4' &
nohup ssh pandora06.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora06 5' &
nohup ssh pandora07.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora07 6' &
nohup ssh pandora08.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend pandora08 7' &
