resolvers += "Akka library repository".at("https://repo.akka.io/maven")

val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.5.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.6"
)