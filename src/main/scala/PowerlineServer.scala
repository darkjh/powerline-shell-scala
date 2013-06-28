import java.io.{InputStreamReader, BufferedReader, PrintStream, IOException}
import java.net.{SocketException, ServerSocket}

/**
 * Powerline server that do all the job as a daemon
 */
object PowerlineServer extends App {

  /** ANSI color definitions */
  object Color {
    val PATH_BG = 237  // dark grey
    val PATH_FG = 250  // light grey
    val CWD_FG = 254  // nearly-white grey
    val SEPARATOR_FG = 244
    
    val REPO_CLEAN_BG = 148  // a light green color
    val REPO_CLEAN_FG = 0  // black
    val REPO_DIRTY_BG = 161  // pink/red
    val REPO_DIRTY_FG = 15  // white
    
    val CMD_PASSED_BG = 236
    val CMD_PASSED_FG = 15
    val CMD_FAILED_BG = 161
    val CMD_FAILED_FG = 15
    
    val SVN_CHANGES_BG = 148
    val SVN_CHANGES_FG = 22  // dark green
    
    val VIRTUAL_ENV_BG = 35  // a mid-tone green
    val VIRTUAL_ENV_FG = 22    
  }

  // TODO refactor in scala style
  case class Segment(content: String,
                     fg: Int,
                     bg: Int,
                     sep: String,
                     var fgSepColor: Int) {
    if (fgSepColor == null)
      fgSepColor = bg

    def draw(next: Segment) = {
      val bgSepColorStr = next match {
        case null => RESET  // reset bg color if it's the last segment
        case _ => bgcolor(next.bg)
      }

      List(fgcolor(fg), bgcolor(bg), content,
        bgSepColorStr, fgcolor(fgSepColor), sep) mkString ""
    }
  }

  val segments: List[Segment] = List()

  val separator = "\u2B80"
  val thinSeparator = "\u2B81"

  val LSQESCRSQ = "\\[\\e%s\\]"
  val RESET = LSQESCRSQ format "[0m"


  def color(prefix: String, code: Int) =
    LSQESCRSQ format ("[%s;5;%sm" format (prefix, code))

  def fgcolor(code: Int) =
    color("38", code)

  def bgcolor(code: Int) =
    color("48", code)

  def draw(segments: List[Segment]) = {
    val shifted = segments.tail
    val output = (for {
      (curr, next) <- segments zip shifted
    } yield curr.draw(next)) mkString ""

    val sb = new StringBuilder()
    output foreach {
      sb.append(_)
    }
    sb.append(segments.last.draw(null))
  }


  // TODO maxDepth
  def genCwdSegments(msg: String) = {
    val cwd = msg.substring(1)  // remove "/"
    val dirs = cwd.split("/").toList

    dirs map {
      dir: String =>
        Segment(" %s " format dir, Color.PATH_FG,
          Color.PATH_BG, thinSeparator, Color.SEPARATOR_FG)
    }
  }

  // Main
  try {
    val server = new ServerSocket(18888)
    while (true) {
      val socket = server.accept()
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = new PrintStream(socket.getOutputStream)

      val msg = in.readLine()
      println("Received: "+msg)
      out.print(draw(genCwdSegments(msg)))

      out.close()
      socket.close()
    }
  } catch {
    case e: SocketException =>
    case e: IOException =>
      e.printStackTrace()
  }
}
