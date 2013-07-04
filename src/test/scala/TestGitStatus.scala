import org.scalatest.FunSuite

/**
 */
class TestGitStatus extends FunSuite {
  test("git status called in non-repo folder") {
    assert(GitStatus("/").exist() == false)
  }

  test("git status called in repo folder") {
    assert(GitStatus(".").exist() == true)
  }
}
