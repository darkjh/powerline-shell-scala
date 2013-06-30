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

  // TODO 2 classes? Segment, LastSegment ?
  case class Segment(content: String,
                     contentFg: Int,
                     contentBg: Int,
                     sep: String,
                     sepFg: Option[Int]) {
    val sepFgColor = sepFg match {
      case Some(c) => c
      case None => contentBg
    }

    def draw(next: Option[Segment]) = {
      val sepBgColorStr = next match {
        // reset bg color if it's the last segment
        case None => RESET
        // separator's bg should be consistent with next segment's bg
        case Some(s) => bgcolor(s.contentBg)
      }

      val sepToUse = next match {
        // last segment
        case None => filledSeparator
        // segments in the middle
        case Some(_) => sep
      }

      List(fgcolor(contentFg), bgcolor(contentBg), content,
        sepBgColorStr, fgcolor(sepFgColor), sepToUse) mkString ""
    }
  }

  // Separators
  val filledSeparator = "\u2B80"
  val thinSeparator = "\u2B81"

  // Bash prompt color escape string
  val LSQESCRSQ = "\\[\\e%s\\]"
  val RESET = LSQESCRSQ format "[0m"
  val ROOT_INDICATOR = " \\$ "

  // Home dir
  val HOME = System.getenv("HOME")


  def color(prefix: String, code: Int) =
    LSQESCRSQ format ("[%s;5;%sm" format (prefix, code))

  def fgcolor(code: Int) =
    color("38", code)

  def bgcolor(code: Int) =
    color("48", code)

  def draw(segments: Seq[Segment]) = {
    val shifted = segments.tail
    val output = (for {
      (curr, next) <- segments zip shifted
    } yield curr.draw(Some(next))) mkString ""

    val sb = new StringBuilder()
    output foreach {
      sb.append(_)
    }
    sb.append(segments.last.draw(None))
    sb.append(RESET)
  }

  // TODO maxDepth
  def genCwdSegments(msg: String) = {
    val cwd =
      if (msg.startsWith(HOME))
        msg.replaceFirst(HOME, "~")
      else
        msg.substring(1)  // remove leading "/"
    val dirs = cwd.split("/").toIndexedSeq

    val (firsts, last) = (dirs.slice(0, dirs.length-1), dirs.last)

    val segments = firsts map {
      dir: String =>
        Segment(" %s " format dir, Color.PATH_FG,
          Color.PATH_BG, thinSeparator, Some(Color.SEPARATOR_FG))
    }
    segments :+ Segment(" %s " format last, Color.PATH_FG,
          Color.PATH_BG, filledSeparator, None)
  }

  def genRootIndicator() = {
    IndexedSeq(Segment(ROOT_INDICATOR, Color.CMD_PASSED_FG,
      Color.CMD_PASSED_BG, thinSeparator, None))
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
      val output = draw(genCwdSegments(msg) ++ genRootIndicator())
      // println(output)
      out.print(output)

      out.close()
      socket.close()
    }
  } catch {
    case e: SocketException =>
    case e: IOException =>
      e.printStackTrace()
  }
}