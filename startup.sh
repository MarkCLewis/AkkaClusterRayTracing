#!/bin/bash

nohup ssh janus00.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus00 0' &
nohup ssh janus01.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus01 1' &
nohup ssh janus02.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus02 2' &
nohup ssh janus03.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus03 3' &
nohup ssh janus04.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus04 4' &
nohup ssh janus05.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus05 5' &
nohup ssh janus06.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus06 6' &
nohup ssh janus07.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus07 7' &
nohup ssh janus08.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus08 8' &
nohup ssh janus09.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus09 9' &
nohup ssh janus10.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus10 10' &
nohup ssh janus11.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus11 11' &
nohup ssh janus12.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus12 12' &
nohup ssh janus13.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus13 13' &
nohup ssh janus14.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus14 14' &
nohup ssh janus15.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus15 15' &
nohup ssh janus16.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus16 16' &
nohup ssh janus17.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus17 17' &
nohup ssh janus18.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus18 18' &
nohup ssh janus19.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus19 19' &
nohup ssh janus20.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus20 20' &
nohup ssh janus21.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus21 21' &
nohup ssh janus22.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus22 22' &
nohup ssh janus23.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.Main backend janus23 23' &