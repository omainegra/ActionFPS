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

case class RemoteSshPath(hostname: String,
                         username: String,
                         path: String) extends ConnectionReader {
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
          RemoteSshPath(hostname = host, path = path, username = u)
        }
    } else None
  }
}

case class LocalReader(file: File) extends ConnectionReader

