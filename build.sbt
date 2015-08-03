organization := "com.foursquare"

name := "exceptionator"

version := "3.0-beta2"

scalaVersion := "2.10.5"

scalacOptions ++= Seq("-deprecation", "-feature", "-Xfatal-warnings")

resolvers ++= Seq(
  "repo.codahale.com" at "http://repo.codahale.com",
  "twitter repo" at "http://maven.twttr.com",
  "jboss 3rdparty" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases"
)

libraryDependencies ++= Seq(
  "com.cloudphysics"        %% "jerkson"              % "0.6.3" withSources(),
  "com.foursquare"          %% "rogue-field"          % "2.2.1" intransitive(),
  "com.foursquare"          %% "rogue-core"           % "3.0.0-beta13.1" intransitive(),
  "com.foursquare"          %% "rogue-lift"           % "3.0.0-beta13.1" intransitive(),
  "com.foursquare"          %% "rogue-index"          % "3.0.0-beta13.1" intransitive(),
  "net.liftweb"             %% "lift-mongodb-record"  % "2.5.1-fs-a",
  "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.2",
  "com.novocode"            %  "junit-interface"      % "0.7"       % "test",
  "com.twitter"             %% "finagle-http"         % "6.16.0",
  "com.twitter"             %% "finagle-core"         % "6.16.0",
  "com.twitter"             %% "finagle-ostrich4"     % "6.16.0",
  "com.twitter"             %% "ostrich"              % "9.1.3",
  "com.typesafe"            %  "config"               % "1.0.0",
  "org.scalaj"              %% "scalaj-collection"    % "1.5",
  "org.joda"                %  "joda-convert"         % "1.2",
  "org.mongodb"             %  "mongo-java-driver"    % "2.13.2",
  "org.scala-tools.testing" %% "specs"                % "1.6.11-fs"     % "test",
  "joda-time"               %  "joda-time"            % "2.2_2013h",
  "javax.mail"              %  "mail"                 % "1.4.4",
  "junit"                   %  "junit"                % "4.10"     % "test"
)

testFrameworks += new TestFramework("com.novocode.junit.JUnitFrameworkNoMarker")

ivyXML := (
<dependencies>
  <exclude module="jms"/>
  <exclude module="jmxtools"/>
  <exclude module="jmxri"/>
</dependencies>)
