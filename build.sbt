organization := "com.foursquare"

name := "exceptionator"

version := "2.0-beta21"

scalaVersion := "2.10.3"

scalacOptions += "-deprecation"

resolvers ++= Seq(
  "repo.codahale.com" at "http://repo.codahale.com",
  "twitter repo" at "http://maven.twttr.com",
  "jboss 3rdparty" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases"
)

libraryDependencies ++= Seq(
  "com.cloudphysics"        %% "jerkson"              % "0.6.3" withSources(),
  "com.foursquare"          %% "rogue-field"          % "2.2.1" intransitive(),
  "com.foursquare"          %% "rogue-core"           % "3.0.0-beta4" intransitive(),
  "com.foursquare"          %% "rogue-lift"           % "3.0.0-beta4" intransitive(),
  "com.foursquare"          %% "rogue-index"          % "3.0.0-beta4" intransitive(),
  "net.liftweb"             %% "lift-mongodb-record"  % "2.5.1",
  "com.googlecode.concurrentlinkedhashmap" % "concurrentlinkedhashmap-lru" % "1.2",
  "com.novocode"            %  "junit-interface"      % "0.6"       % "test",
  "com.twitter"             %%  "finagle-http"         % "6.6.2",
  "com.twitter"             %% "finagle-core"         % "6.6.2",
  "com.twitter"             %% "finagle-ostrich4"     % "6.6.2",
  "com.twitter"             %% "ostrich"              % "9.1.3",
  "com.typesafe"            %  "config"               % "0.4.1",
  "org.scalaj"              %% "scalaj-collection"    % "1.5",
  "org.joda"                %  "joda-convert"         % "1.1",
  "org.mongodb"             %  "mongo-java-driver"    % "2.11.3",
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
