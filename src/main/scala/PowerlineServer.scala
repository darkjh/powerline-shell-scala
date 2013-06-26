import java.io.{IOException, DataInputStream, ObjectInputStream, DataOutputStream}
import java.net.{SocketException, ServerSocket}

/**
 *
 */
object PowerlineServer extends App {
  try {
    val socket = new ServerSocket(18888).accept()
    val out = new DataOutputStream(socket.getOutputStream)
    val in = new ObjectInputStream(
      new DataInputStream(socket.getInputStream))

    while (true) {
      val input = in.readObject
      out.writeChars("Hello Man: "+input.asInstanceOf[String])
    }
    out.close()
    socket.close()
  } catch {
    case e: SocketException =>
    case e: IOException =>
      e.printStackTrace()
  }
}
