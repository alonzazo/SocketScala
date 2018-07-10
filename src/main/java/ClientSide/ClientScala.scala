package ClientSide


import java.io.{OutputStream, RandomAccessFile}
import java.net.{InetAddress, Socket}
import java.util
import java.util.Scanner

import Utils.{GeneradorArchivo, TablaTiempos}

class ClientScala {

  private var client = new Socket()
  private var reader: Scanner = _
  private var writer: OutputStream =_
  private var running: Boolean = true

  def run(): Unit = {
    try {
      val hostAddress = scala.io.StdIn.readLine()
      client = new Socket(InetAddress.getByName(hostAddress), 9999)
      reader = new Scanner(client.getInputStream())
      writer = client.getOutputStream()

      var textIn = reader.nextLine()
      println(s"MENSAJE DEL SERVIDOR:\n$textIn")

      while (running){
        println("Escriba su mensaje:")
        val keyboard = new Scanner(System.in)
        val textOut = keyboard.nextLine()

        val textParts = textOut.split(" ")

        textParts(0) match {
          case "TRANSMITIR" => {


            //Analizamos el path de la entrada
            if (textParts.length == 2){
              //Creación de archivos
              for (i <- 1 to textParts(1).toInt)
                GeneradorArchivo.getInstance().generateRandomSizeFile();
              println(s"Tamaño medio generado fue de ${GeneradorArchivo.getInstance().getMeanSize()} KB\n")
            }else if (textParts.length  > 2){
              for (i <- 1 to textParts(1).toInt)
                GeneradorArchivo.getInstance().generateFileOfSpecificSize(textParts(2).toInt)
            }

            if (textParts.length > 1){
              //Inicializamos el registro de tiempos
              val registro = new TablaTiempos()
              //Vemos si está en el generador de archivos
              val listFiles: util.LinkedList[javafx.util.Pair[Integer, String]] = GeneradorArchivo.getInstance().getFilesList()
              listFiles.forEach( tiempo => {
                //Iniciamos el timer
                registro.startTimer()
                //Inicia la transmisión
                transmitir(tiempo.getValue(), tiempo.getKey())
                //Detenemos el tiempo y registramos
                registro.stopTimer()
                registro.registerNewTime(tiempo.getValue(),tiempo.getKey())
              })
              //Exportamos el registro de tiempos
              registro.exportToCSV("registro1")
            } else {
              println("Parámetros inválidos")
            }

          }
          case "SALIR" => shutdown()
          case _ => {
            write(textOut)
            println("MENSAJE ENVIADO:\n\t" + textOut)

            textIn = reader.nextLine()
            println("MENSAJE DEL SERVIDOR:\n" + textIn)
          }
        }
      }


    }catch {
      case e:Exception => {
        System.err.println("ERROR: " + e.getMessage())
        client.close()
      }
    }

  }

  def transmitir(path: String, size: Int): Boolean = {
    /*implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()*/

    write("TRANSMITIR " + path + " " + size)
    println(s"Intentando transmitir el archivo $path...")

    reader.nextLine()
    println(s"Transmitiendo archivo $path -------------------------------TOTAL SIZE: $size KB")
    var currentKB = 0
    val defaultBlockSize = 1024
    val byteBuffer = new Array[Byte](defaultBlockSize)
    val randomAccessFile = new RandomAccessFile(path, "r")
    val numberOfChunks = randomAccessFile.length().toInt/defaultBlockSize
    for (i <- 1 to numberOfChunks) {
      randomAccessFile.readFully(byteBuffer)

      writer.write(byteBuffer ++ Array[Byte]('\n'.toByte))

      reader.nextLine()
      currentKB += 1
      if (currentKB % (size / 50) == 0) print("█")
    }
    randomAccessFile.close

    write("TERMINAR")

    reader.nextLine()
    println(s"\n$path ha sido transmitido correctamente\n")
    return true
  }

  def write(message: String):Unit = {
    writer.write((message + "\n").getBytes())
  }

  def shutdown(): Unit = {
    running = false
    client.close()
    println(client.getInetAddress().getHostAddress() + " closed the connection")
    GeneradorArchivo.getInstance().deleteAllFilesGenerated()
  }
}
