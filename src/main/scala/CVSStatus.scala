/**
 * All CVS status checking objects should implement
 * this interface
 */
trait CVSStatus {
  def exist(): Boolean
  def isClean(): Boolean
  def currentBranch(): String
}
