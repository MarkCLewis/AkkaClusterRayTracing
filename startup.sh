#!/bin/bash

nohup ssh pandora01.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora02.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora03.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora04.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora05.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora06.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora07.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
nohup ssh pandora08.cs.trinity.edu 'java -cp /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.remoting.StartupRemote' &
