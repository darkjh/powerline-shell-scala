import java.io.{PrintStream, IOException}
import java.net.{SocketException, ServerSocket}
import scala.io.BufferedSource

/**
 *
 */
object PowerlineServer extends App {
  try {
    val server = new ServerSocket(18888)
    while (true) {
      val socket = server.accept()
      val in = new BufferedSource(socket.getInputStream).getLines()
      val out = new PrintStream(socket.getOutputStream)

      while (in.hasNext) {
        val msg = in.next()
        out.print(msg)
        // out.print('\0')
        println("Received: "+msg)
      }
      out.close()
      socket.close()
    }
  } catch {
    case e: SocketException =>
    case e: IOException =>
      e.printStackTrace()
  }
}
