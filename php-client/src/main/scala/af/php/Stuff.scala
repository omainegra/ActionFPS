package af.php

import com.scalawilliam.sfc.{FastCGIConnectionConfig, FastCGIHandlerConfig, FastCGIRequest}

/**
  * Created by William on 27/12/2015.
  */
object Stuff {

  val sampleRequest = FastCGIRequest(
    remoteUser = None,
    headers = List.empty,
    authType = Option.empty,
    queryString = None,
    contextPath = "",
    servletPath = "/test.php",
    requestURI = "/uri/",
    serverName = "ScalaTest",
    protocol = "HTTP/1.1",
    remoteAddr = "127.0.0.1",
    serverPort = 1234,
    method = "GET",
    data = None,
    realPath = something => {
      scala.util.Properties.userDir + "/php-client/src/main/resources/root" + something
    }
  )
  def buildFcgi = FastCGIHandlerConfig(
    connectionConfig = FastCGIConnectionConfig.SingleConnection(
      address = "127.0.0.1:7772"
    ),
    startExecutable = Option(s"C:/php/php-cgi.exe -b 127.0.0.1:7772")
  ).build
}
