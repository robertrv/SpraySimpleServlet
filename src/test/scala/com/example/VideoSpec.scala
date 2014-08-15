package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._


class VideoSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  
  "VideoService" should {

    "return an empty list when videos are empty" in {
      Get("/video") ~> myRoute ~> check {
        responseAs[String] === "[]"
      }
    }

    "adds a video and then gets it" in {
      import VideoProtocol._
      Post("/video", Video(title = "Some title", duration = 33l)) ~> myRoute ~> check {
        Get("/video") ~> myRoute ~> check {
          responseAs[String] must contain(""""title": "Some title"""")
        }
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(myRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
