package myAPI

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import scala.io.StdIn

import scala.concurrent.ExecutionContext
import scala.collection.mutable.ArrayBuffer
import io.circe.syntax._
import akka.util.ByteString


object myAPI extends App {
    implicit val system = ActorSystem(Behaviors.empty, "my-lowlevel-system")
    implicit val executionContext: ExecutionContext = system.executionContext

    val taskList = ArrayBuffer[String]()

    val requestHandler: HttpRequest => HttpResponse = {
        
        case HttpRequest(GET, Uri.Path("/task"), _, _, _) => {
            val json = taskList.asJson.noSpaces
            HttpResponse(StatusCodes.OK, entity = HttpEntity(
                ContentTypes.`application/json`,
                json
            )) 
        }
               
        case HttpRequest(PUT, uri, _, _, _) if uri.path.toString == "/task" => {
            val queryParams = uri.query()

            val task: String = queryParams.getOrElse("task", "")

            task match { 
                case "" => HttpResponse(400)
                case task: String => 
                    taskList += task
                    HttpResponse(StatusCodes.OK, entity = HttpEntity(
                        ContentTypes.`application/json`,
                        taskList.asJson.noSpaces
                    ))

            }   
        }

        case HttpRequest(PATCH, uri, _, _, _) if uri.path.toString == "/task" => {
            val queryParams = uri.query()

            val task: String = queryParams.getOrElse("task", "")
            val newTask: String = queryParams.getOrElse("newTask", "")

            taskList.contains(task) match { 
                case false => HttpResponse(400)
                case true => 
                    taskList(taskList.indexOf(task)) = newTask

                    HttpResponse(StatusCodes.OK, entity = HttpEntity(
                        ContentTypes.`application/json`,
                        taskList.asJson.noSpaces
                    ))

            }   
        }

        case HttpRequest(DELETE, uri, _, _, _) if uri.path.toString == "/task" => {
            val queryParams = uri.query()

            val task: String = queryParams.getOrElse("task", "")

            taskList.contains(task) match { 
                case false => HttpResponse(400)
                case true => 
                    taskList -= task

                    HttpResponse(StatusCodes.OK, entity = HttpEntity(
                        ContentTypes.`application/json`,
                        taskList.asJson.noSpaces
                    ))

            }   
        }

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