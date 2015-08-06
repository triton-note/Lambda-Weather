name := "TritonNote-Lambda-Moon"

version := "1.0.2"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "org.fathens" %% "astronomy" % "1.1.3"
)

assemblyExcludedJars in assembly := { 
  val cp = (fullClasspath in assembly).value
  cp filter { _.data.getName.startsWith("aws-lambda") }
}
