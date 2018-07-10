package ServerSide


import java.net.ServerSocket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServerScala {

  def startServer(): Unit = {

    val server = new ServerSocket(9999)
    println(s"Server is running on port ${server.getLocalPort}")

    while (true) {

      val client = server.accept()
      println(s"Client connected: ${client.getInetAddress().getHostAddress()}")

      val connection = Future {
        new ClientHandler(client).run()
        1
      }
    }
  }
}
