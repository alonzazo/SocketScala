package Utils


import java.io._
import java.util

class TablaTiempos {
  private var tiempos: util.LinkedList[Tiempo] = new util.LinkedList[Tiempo]()
  private var currenTime: Long = 0

  def exportToCSV(fileName: String): String = {
    var result = ""

    //Componemos el string
    result += "nombreArchivo,tamano,duracion\n"
    tiempos.forEach(tiempo =>{
      result += s"${tiempo.getPathFile},${tiempo.getSize},${tiempo.getDuration}\n"
    })
    //Escribimos el archivo
    val writer = new PrintWriter(s"$fileName.csv")
    writer.print(result)
    writer.close()

    return result
  }

  def startTimer(): Long = {
    currenTime = System.nanoTime()
    return currenTime
  }

  def stopTimer(): Long = {
    currenTime = System.nanoTime() - currenTime
    return currenTime
  }

  def registerNewTime(pathFile: String, size: Int): Unit = {
    tiempos.add(new Tiempo(pathFile,size,currenTime))
  }

  class Tiempo(private var pathFile: String,private var size: Int, private var duration: Long) {
    def getPathFile = pathFile
    def getSize = size
    def getDuration = duration
  }
}
