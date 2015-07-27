name := """TritonNote-Lambda-Report2Catches"""

version := "0.1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "com.amazonaws" % "aws-java-sdk" % "1.10.6"
)

assemblyExcludedJars in assembly := { 
  val cp = (fullClasspath in assembly).value
  cp filter {_.data.getName startsWith "aws-"}
}
