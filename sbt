#!/bin/sh

LATEST=http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.0/sbt-launch.jar

if [ ! -d .sbtlib ]; then
  mkdir .sbtlib
fi

if [ ! -f .sbtlib/sbt-launcher.jar ]; then
  wget -O .sbtlib/sbt-launcher.jar $LATEST
fi

java \
-Duser.timezone=UTC \
-Djava.awt.headless=true \
-Dfile.encoding=UTF-8 \
-XX:MaxPermSize=256m \
-Xmx1g \
-noverify \
-jar .sbtlib/sbt-launcher.jar \
"$@"
