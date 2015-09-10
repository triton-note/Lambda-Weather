name := "TritonNote-Lambda-Weather"

version := "1.0.3"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.0",
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0"
)

assemblyExcludedJars in assembly := { 
  val cp = (fullClasspath in assembly).value
  cp filter { _.data.getName.startsWith("aws-lambda") }
}
