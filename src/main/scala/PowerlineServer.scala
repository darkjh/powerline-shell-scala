import com.sun.akuma.Daemon
import java.io.{InputStreamReader, BufferedReader, PrintStream, IOException}
import java.net.{SocketException, ServerSocket}

/**
 * Powerline server that do all the job as a daemon
 */
object PowerlineServer extends App {

  /** ANSI color definitions */
  object Color {
    val PATH_BG = 238  // dark grey
    val PATH_FG = 251  // light grey
    val CWD_FG = 254  // nearly-white grey
    val SEPARATOR_FG = 244

    val USER_FG = 27

    val REPO_CLEAN_BG = 35 // 148  // a light green color
    val REPO_CLEAN_FG = 0  // black
    val REPO_DIRTY_BG = 160 // 161  // pink/red
    val REPO_DIRTY_FG = 15  // white

    val CMD_PASSED_BG = 236
    val CMD_PASSED_FG = 15
    val CMD_FAILED_BG = 124 // 161
    val CMD_FAILED_FG = 15

    val SVN_CHANGES_BG = 148
    val SVN_CHANGES_FG = 22  // dark green

    val VIRTUAL_ENV_BG = 35  // a mid-tone green
    val VIRTUAL_ENV_FG = 22
  }

  // Separators
  val filledSeparator = "\u2B80"
  val thinSeparator = "\u2B81"
  val ellipsis = "\u2026"

  // Bash prompt color escape string
  val LSQESCRSQ = "\\[\\e%s\\]"
  val RESET = LSQESCRSQ format "[0m"
  val ROOT_INDICATOR = " \\$ "

  // Home dir
  val HOME = System.getenv("HOME")
  val USER = System.getenv("USER")


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

  def generateCwdSegments(path: String, maxLen: Int) = {
    val dirs = path match {
      case msg if msg startsWith(HOME) =>
        msg.replaceFirst(HOME, "~").split("/").toIndexedSeq
      case "/" =>
        IndexedSeq("/")
      case msg =>
        msg.substring(1).split("/").toIndexedSeq  // remove the leading "/"
    }

    var len = dirs.foldLeft(0)((l, d) => l + d.length + 3)
    val hasDrop = len > maxLen

    val (firsts, last) = (dirs.slice(0, dirs.length-1), dirs.last)

    val segments = {
      val shortened = firsts.dropWhile {
        dir: String =>
          val drop = len > maxLen
          len = len - dir.length - 3
          drop
      }
      shortened map {
        dir: String =>
          NormalSegment(" %s " format dir, Color.PATH_FG,
            Color.PATH_BG, thinSeparator, Color.SEPARATOR_FG)
      }
    }

    (if (hasDrop)
      IndexedSeq(NormalSegment(" %s " format ellipsis,
        Color.PATH_FG, Color.PATH_BG, thinSeparator, Color.SEPARATOR_FG))
    else
      IndexedSeq()) ++
      segments :+
      LastSegment(" %s " format last,
        Color.PATH_FG, Color.PATH_BG)
  }

  def generateRootIndicator(retCode: Int) = {
    val (fg, bg) = if (retCode != 0) {
      (Color.CMD_FAILED_FG, Color.CMD_FAILED_BG)
    } else {
      (Color.CMD_PASSED_FG, Color.CMD_PASSED_BG)
    }
    IndexedSeq(LastSegment(ROOT_INDICATOR, fg, bg))
  }

  def generateCVSSegment(path: String) = {
    val git = GitStatus(path)
    if (git.exist()) {
      val (fg, bg) = git.isClean() match {
        case true => (Color.REPO_CLEAN_FG, Color.REPO_CLEAN_BG)
        case false => (Color.REPO_DIRTY_FG, Color.REPO_DIRTY_BG)
      }
      IndexedSeq(LastSegment(" %s " format git.currentBranch(), fg, bg))
    } else {
      IndexedSeq()
    }
  }

  val userSegment = IndexedSeq(
    LastSegment(" %s " format USER, Color.REPO_DIRTY_FG, Color.USER_FG))

  def runPowerlineServer() {
    try {
      val server = new ServerSocket(18888)
      while (true) {
        val socket = server.accept()
        val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
        val out = new PrintStream(socket.getOutputStream)

        val pwd = in.readLine()
        val ret = try {
          in.readLine().toInt
        } catch {
          case e: Exception => 0
        }
        val winSize = in.readLine().toInt
        // calculate max length for PWD segments
        val maxLen = ((winSize + HOME.length - 3) * 0.4f).toInt

        println("Pwd: "+pwd + ", Ret: " + ret + ", Size: " + winSize)
        val output = draw(
          userSegment
            ++ generateCwdSegments(pwd, maxLen)
            ++ generateCVSSegment(pwd)
            ++ generateRootIndicator(ret))
        out.print(output)

        in.close()
        out.close()
        socket.close()
      }
    } catch {
      case e: SocketException =>
      case e: IOException =>
        e.printStackTrace()
    }
  }

  // Main
  // Daemonize the powerline server
  val daemon = new Daemon()
  if (daemon.isDaemonized) {
    daemon.init()
  } else {
    daemon.daemonize()
    System.exit(0)
  }
  // Run the server in background
  runPowerlineServer()
}