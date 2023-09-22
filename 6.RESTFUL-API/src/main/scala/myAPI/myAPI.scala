package myAPI

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

object myAPI extends App {
    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext = system.executionContext

    val route =
    path("hello") {
        get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,"<h1>Say hello to akka-http</h1>"))
        }
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    StdIn.readLine()

    bindingFuture
        .flatMap(server => server.unbind())
        .onComplete(_ => system.terminate())
}