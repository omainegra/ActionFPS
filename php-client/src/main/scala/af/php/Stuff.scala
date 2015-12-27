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

  def buildFcgi(port: Int, start: Boolean = true) = {
    val addr = s"127.0.0.1:$port"
    FastCGIHandlerConfig(
      connectionConfig = FastCGIConnectionConfig.SingleConnection(
        address = addr
      ),
      startExecutable = if (start) Option {
        if (scala.util.Properties.isWin)
          s"C:/php/php-cgi.exe -b $addr"
        else s"php-cgi -b $addr"
      } else None
    ).build
  }
}
