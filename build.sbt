organization := "com.foursquare"

name := "exceptionator"

version := "2.0-beta16"

scalaVersion := "2.9.1"

scalacOptions += "-deprecation"

resolvers ++= Seq(
  "repo.codahale.com" at "http://repo.codahale.com",
  "twitter repo" at "http://maven.twttr.com",
  "jboss 3rdparty" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases"
)

libraryDependencies ++= Seq(
  "com.codahale"            %% "jerkson"              % "0.4.2"      withSources(),
  "com.foursquare"          %% "rogue-field"          % "2.0.0-RC1" intransitive(),
  "com.foursquare"          %% "rogue-core"           % "2.0.0-RC1" intransitive(),
  "com.foursquare"          %% "rogue-lift"           % "2.0.0-RC1" intransitive(),
  "net.liftweb"             %% "lift-mongodb-record"  % "2.4-M5",
  "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.2",
  "com.novocode"            %  "junit-interface"      % "0.6"       % "test",
  "com.twitter"             %  "finagle-http"         % "5.3.23",
  "com.twitter"             %  "finagle-core"         % "5.3.23",
  "com.twitter"             %  "finagle-ostrich4"     % "5.3.23",
  "com.twitter"             %  "ostrich"              % "8.2.3",
  "com.typesafe"            %  "config"               % "0.4.1",
  "org.scalaj"              %% "scalaj-collection"    % "1.2",
  "org.joda"                %  "joda-convert"         % "1.1",
  "org.mongodb"             %  "mongo-java-driver"    % "2.9.3",
  "org.scala-tools.testing" %% "specs"                % "1.6.9"     % "test",
  "joda-time"               %  "joda-time"            % "2.0",
  "javax.mail"              %  "mail"                 % "1.4.2",
  "junit"                   %  "junit"                % "4.8.2"     % "test"
)

testFrameworks += new TestFramework("com.novocode.junit.JUnitFrameworkNoMarker")

ivyXML := (
<dependencies>
  <exclude module="jms"/>
  <exclude module="jmxtools"/>
  <exclude module="jmxri"/>
</dependencies>)
