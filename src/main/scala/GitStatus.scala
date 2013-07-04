import java.io.File
import org.eclipse.jgit.api.Git

/**
 * Checks git repo status (clean, branch)
 */
case class GitStatus(path: String)
  extends CVSStatus {

  val git = try {
    path match {
      case path if path == "/" => None  // TODO bug in JGit ???
      case path => Some(Git.open(new File(path)))
    }
  } catch {
    case e: Exception => None
  }

  def exist() = git match {
    case Some(_) => true
    case None => false
  }

  def isClean() =
    git.get.status().call().isClean

  def currentBranch() =
    git.get.getRepository.getBranch
}