package myAPI

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import scala.io.StdIn

import scala.concurrent.ExecutionContext


object myAPI extends App {
    implicit val system = ActorSystem(Behaviors.empty, "my-lowlevel-system")
    implicit val executionContext: ExecutionContext = system.executionContext


    val requestHandler: HttpRequest => HttpResponse = {
        case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
            HttpResponse(entity = HttpEntity(
                ContentTypes.`text/html(UFT-8)`,
                "<html><body>Hello world!</body></html>"
            ))
        
        case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
        HttpResponse(entity = "PONG!")

        case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
        sys.error("BOOM!")

        case r: HttpRequest =>
            r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            HttpResponse(404, entity = "Unknown resource!")
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bindSync(requestHandler)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine()

    bindingFuture
        .flatMap(server => server.unbind())
        .onComplete(_ => system.terminate())
}