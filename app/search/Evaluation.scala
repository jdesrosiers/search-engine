package search

object Evaluation {

  def log2(value: Double): Double = scala.math.log(value) / scala.math.log(2)

  def discountedGain(relevance: Double, rank: Int): Double = rank match {
    case 1 => relevance
    case _ => relevance / log2(rank)
  }
  def dg = (discountedGain _)

  def discountedCumulativeGain(relevance: List[Double]): List[Double] = {
    var sum = 0.0
    relevance.zip(Stream from 1).foldLeft(List[Double]()) {
      case (acc, (rel, rank)) =>
        sum += discountedGain(rel, rank)
        acc :+ sum
    }
  }
  def dcg = (discountedCumulativeGain _)

  def normalizedDiscountedCumulativeGain(relevance: List[Double]): List[Double] = {
    val actuals = discountedCumulativeGain(relevance)
    val ideals = discountedCumulativeGain(relevance.sortWith(_ > _))
    (actuals zip ideals).map { case (actual, ideal) => actual / ideal }
  }
  def ndcg = (normalizedDiscountedCumulativeGain _)

  def fMeasure(relevant: List[String], retrieved: List[String]) = {
    val precision =
      if (retrieved.size == 0) 0.0
      else (relevant intersect retrieved).size / (0.0 + retrieved.size)
    val recall = (relevant intersect retrieved).size / 10.0//(0.0 + relevant.size)

    if (precision + recall == 0.0)
      0.0
    else
      (precision * recall) / (precision + recall)
  }

}
