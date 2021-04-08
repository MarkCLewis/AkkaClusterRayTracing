#!/bin/bash

nohup ssh janus00.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus00 25251 0' &
nohup ssh janus01.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus01 25251 1' &
nohup ssh janus02.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus02 25251 2' &
nohup ssh janus03.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus03 25251 3' &
nohup ssh janus04.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus04 25251 4' &
nohup ssh janus05.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus05 25251 5' &
nohup ssh janus06.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus06 25251 6' &
nohup ssh janus07.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus07 25251 7' &
nohup ssh janus08.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus08 25251 8' &
nohup ssh janus09.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus09 25251 9' &
nohup ssh janus10.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus10 25251 10' &
nohup ssh janus11.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus11 25251 11' &
nohup ssh janus12.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus12 25251 12' &
nohup ssh janus13.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus13 25251 13' &
nohup ssh janus14.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus14 25251 14' &
nohup ssh janus15.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus15 25251 15' &
nohup ssh janus16.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus16 25251 16' &
nohup ssh janus17.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus17 25251 17' &
nohup ssh janus18.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus18 25251 18' &
nohup ssh janus19.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus19 25251 19' &
nohup ssh janus20.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus20 25251 20' &
nohup ssh janus21.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus21 25251 21' &
nohup ssh janus22.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus22 25251 22' &
nohup ssh janus23.cs.trinity.edu 'java -cp -Xmx12g /users/khardee/AkkaClusterRayTracing/target/scala-2.12/AkkaRayTracing-assembly-1.0.jar acrt.cluster.untyped.RaytracingMain backend janus23 25251 23' &
