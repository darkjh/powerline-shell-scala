import java.io.{InputStreamReader, BufferedReader, PrintStream, IOException}
import java.net.{SocketException, ServerSocket}
import scala.io.BufferedSource

/**
 * Powerline server that do all the job as a daemon
 */
object PowerlineServer extends App {

  val segments: List[String] = List()

  // Main
  try {
    val server = new ServerSocket(18888)
    while (true) {
      val socket = server.accept()
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = new PrintStream(socket.getOutputStream)

      val msg = in.readLine()
      out.print(msg)
      println("Received: "+msg)

      out.close()
      socket.close()
    }
  } catch {
    case e: SocketException =>
    case e: IOException =>
      e.printStackTrace()
  }
}
