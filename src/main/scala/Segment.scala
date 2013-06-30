/**
 *
 */
abstract class Segment {
  val content: String
  val contentFg: Int
  val contentBg: Int

  def draw(): String
}

import PowerlineServer._

case class NormalSegment(content: String,
                         contentFg: Int,
                         contentBg: Int,
                         sep: String,
                         sepFg: Int)
  extends Segment {

  def draw() = {
    List(fgcolor(contentFg), bgcolor(contentBg), content,
      fgcolor(sepFg), bgcolor(contentBg), sep) mkString ""
  }
}

case class LastSegment(content: String,
                       contentFg: Int,
                       contentBg: Int,
                       next: Option[Segment])
  extends Segment {

  val sepBgColorStr = next match {
    case Some(seg) => bgcolor(seg.contentBg)
    case None => RESET
  }

  def draw() = {
    List(fgcolor(contentFg), bgcolor(contentBg), content,
      fgcolor(contentBg), sepBgColorStr, filledSeparator) mkString ""
  }
}