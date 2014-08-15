package com.example

import java.util.concurrent.atomic.AtomicLong

import akka.actor.Actor
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.marshalling._
import spray.json._
import spray.routing._

case class Video(var id: Option[Long] = None,
                 title: String,
                 duration: Long,
                 location: Option[String] = None,
                 subject:  Option[String] = None,
                 contentType: Option[String] = Some("video/mpeg"),
                 dataUrl: Option[String] = None)

object VideoProtocol extends DefaultJsonProtocol {
  implicit val marshaller = jsonFormat7(Video)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService with spray.httpx.SprayJsonSupport {

  import VideoProtocol._

  var videos: List[Video] = List()
  val counter: AtomicLong = new AtomicLong(0)

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Just playing with spray.io to keep learning for <a href="https://class.coursera.org/mobilecloud-001/">Coursera</a></h1>
                <h2>You can see the list of videos <a href="/video">here</a></h2>
              </body>
            </html>
          }
        }
      }
    } ~
    path("video") {
      get {
        complete {
          videos
        }
      } ~ post {
        entity(as[Video]) { video =>
          complete {
            val id = counter.incrementAndGet()
            video.id = Some(id)
            videos = video :: videos
            video.id.toString
          }
        }
      }
    }
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}
