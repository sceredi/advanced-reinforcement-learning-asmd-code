ThisBuild / version := "0.1.0-SNAPSHOT"


// Properties for the Python interpreter
initialize ~= { _ =>
  val result = java.lang.Runtime.getRuntime.exec(Array("python", "--version"))
  result.waitFor()
  val line = scala.io.Source.fromInputStream(result.getInputStream).getLines().next()
  val version = line.split(" ")(1)
  val majorAndMinor = version.split('.').take(2).mkString(".")
  System.setProperty("scalapy.python.programname", "env/bin/python")
  System.setProperty("scalapy.python.library", s"python$majorAndMinor")
}

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "3.3.3",
    name := "multi-agent-system-modelling",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "dev.scalapy" %% "scalapy-core" % "0.5.3",
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % "test",
    ),
  )
