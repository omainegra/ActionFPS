package com.actionfps.ladder.connecting

import java.io.File
import java.net.URI

/**
  * Created by me on 09/05/2016.
  */

sealed trait ConnectionReader

object ConnectionReader {
  def unapply(input: String): Option[ConnectionReader] = {
    val uri = new URI(input)
    if ("ssh" == uri.getScheme) {
      RemoteSshPath.unapply(input)
    } else if ("file" == uri.getScheme) {
      Option(LocalReader(new File(uri)))
    } else None
  }
}

sealed trait RemoteSshAction {
  def command: String
}

object RemoteSshAction {

  //cat $(ls -r --sort=time *28763*|head -n -1) && tail $(ls -r --sort=time *2016*27863*|tail -n 1)

  case class Execute(command: String) extends RemoteSshAction

  case class Tail(file: String) extends RemoteSshAction {
    override def command: String = s"tail -f '${file}'"
  }

  case class TailStart(file: String) extends RemoteSshAction {
    override def command: String = s"tail -n +0 -f '${file}'"
  }

}

case class RemoteSshPath(hostname: String,
                         username: String) extends ConnectionReader {
  def sshTarget = s"$username@$hostname"
}

object RemoteSshPath {
  def unapply(input: String): Option[RemoteSshPath] = {
    val uri = new URI(input)
    if ("ssh" == uri.getScheme) {
      for {
        u <- Option(uri.getUserInfo)
        host <- Option(uri.getHost)
        path <- Option(uri.getPath)
      }
        yield {
          RemoteSshPath(hostname = host, username = u)
        }
    } else None
  }
}

case class LocalReader(file: File) extends ConnectionReader

