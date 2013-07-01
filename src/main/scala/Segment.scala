/**
 *
 */
abstract class Segment {
  val content: String
  val contentFg: Int
  val contentBg: Int

  def draw(next: Option[Segment]): String
}

import PowerlineServer._

case class NormalSegment(content: String,
                         contentFg: Int,
                         contentBg: Int,
                         sep: String,
                         sepFg: Int)
  extends Segment {

  override def draw(next: Option[Segment]) = {
    List(fgcolor(contentFg), bgcolor(contentBg), content,
      fgcolor(sepFg), bgcolor(contentBg), sep) mkString ""
  }
}

case class LastSegment(content: String,
                       contentFg: Int,
                       contentBg: Int)
  extends Segment {

  override def draw(next: Option[Segment]) = {
    val sepBgColorStr = next match {
      case Some(seg) => bgcolor(seg.contentBg)
      case None => RESET
    }
    List(fgcolor(contentFg), bgcolor(contentBg), content,
      fgcolor(contentBg), sepBgColorStr, filledSeparator) mkString ""
  }
}