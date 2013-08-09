import sbt._

object Plugins extends Build {
  lazy val root = Project("root", file(".")) dependsOn(
    uri("git://github.com/jorgeortiz85/sbt-assembly.git")
  )
}
