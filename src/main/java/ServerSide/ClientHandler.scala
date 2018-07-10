package ServerSide

import java.io.OutputStream
import java.net.Socket
import java.util.Scanner

import Utils.GeneradorArchivo

class ClientHandler(val client: Socket) {


  private val reader: Scanner = new Scanner(client.getInputStream())
  private val writer: OutputStream = client.getOutputStream()
  private var running: Boolean = false
  private var tempSize: Int = 0

  def run(): Unit = {
    running = true

    // Welcome message
    write("Welcome to the server!" +
      "To Exit, write: 'EXIT'.")

    var writingFile: Boolean = false
    while (running) {
      try {
        val text = reader.nextLine()
        if (text == "EXIT"){
          shutdown()
        }

        var words: Array[String]  = text.split(" ")
        if (!GeneradorArchivo.getInstance().isWriting){
          words(0) match {
            case "TRANSMITIR" => {
              try {
                if (words(1).trim() != "" && words(2).trim() != ""){
                  write(s"Iniciando recepción de archivo: ${words(1)}-----------------------------------${words(2)} KB")
                  println(s"Iniciando recepción de archivo: ${words(1)}-----------------------------------${words(2)} KB")

                  GeneradorArchivo.getInstance().createFile("D" + words(1))
                  tempSize = words(2).toInt

                }else {
                  write("ERROR Nombre del archivo inválido")
                  println("Error en el nombre del archivo")
                }
              }
              catch {
                case e: IndexOutOfBoundsException => {
                  write("ERROR Nombre del archivo inválido")
                  println("Error en el nombre del archivo")
                }
              }
            }
            case _ => {
              println("MENSAJE RECIBIDO:\n\t" + text)
              write("\tEsto es una respuesta a su mensaje")
            }
          }
        } else {
          words(0) match {
            case "TERMINAR" => {
              println(s"\n${GeneradorArchivo.getInstance().getTempFilePath} ha sido recibido correctamente\n")
              GeneradorArchivo.getInstance().closeFile()
              write("\tServidor ha cerrado el archivo")

            }
            case _ => {
              write(s"\tServidor ha escrito ${text.length/1024} KB")
              GeneradorArchivo.getInstance().writeInFile(text)
              if (GeneradorArchivo.getInstance().getTempSize % (tempSize / 50) == 0) print("█")
            }
          }
        }


      } catch {
        // TODO: Implement exception handling
        case e: Exception => shutdown()
      }
    }
  }

  private def write(message: String): Unit = {
    writer.write((message + '\n').getBytes())
  }

  private def shutdown(): Unit = {
    running = false
    client.close()
    println(s"${client.getInetAddress().getHostAddress()} closed the connection")
    GeneradorArchivo.getInstance().deleteAllFilesGenerated()
  }
}
