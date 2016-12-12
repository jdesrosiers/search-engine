import org.scalatest._
import org.scalatest.Matchers._
import search.Evaluation._

class EvaluationSpec extends FunSpec {
  describe("log2") {
    it("log2(0) == 0") {
      log2(0) shouldEqual Double.NegativeInfinity
    }

    it("log2(1) == 0") {
      log2(1) shouldEqual (0.0 +- 0.001)
    }

    it("log2(2) == 1") {
      log2(2) shouldEqual (1.0 +- 0.001)
    }

    it("log2(3) == 1.58496") {
      log2(3) shouldEqual (1.58496 +- 0.001)
    }
  }

  describe("discountedGain") {
    it("discountedGain(relevance = 3, rank = 1) == 3") {
      discountedGain(3, 1) shouldEqual (3.0 +- 0.001)
    }

    it("discountedGain(relevance = 2, rank = 2) == 3") {
      discountedGain(2, 2) shouldEqual (2.0 +- 0.001)
    }

    it("discountedGain(relevance = 3, rank = 3) == 1.893") {
      discountedGain(3, 3) shouldEqual (1.893 +- 0.001)
    }
  }

  describe("discountedCumulativeGain") {
    it("map discountedGain") {
      val relevance = List[Double](3, 2, 3, 0, 0, 1, 2, 2, 3, 0)
      val expected = List[Double](3, 2, 1.89, 0, 0, 0.39, 0.71, 0.67, 0.95, 0).map(_ +- 0.01)

      val result = relevance.zipWithIndex.map {
        case (relevance, index) => discountedGain(relevance, index + 1)
      }

      (result zip expected).foreach {
        case (result, expected) => result shouldEqual expected
      }
    }

    it("discountedCumulativeGain() == List()") {
      dcg(Nil) shouldEqual Nil
    }

    it("discountedCumulativeGain(List(3)) == List(3)") {
      dcg(List(3)) shouldEqual List(3)
    }

    it("discountedCumulativeGain(List(3, 2)) == List(3, 5)") {
      dcg(List(3, 2)) shouldEqual List(3, 5)
    }

    it("discountedCumulativeGain(List(3, 2, 3)) == List(3, 5, 6.89)") {
      val relevance = List[Double](3, 2, 3)
      val expected = List[Double](3, 5, 6.89).map(_ +- 0.01)

      dcg(relevance) zip expected foreach {
        case (result, expected) => result shouldEqual expected
      }
    }

    it("discountedCumulativeGain(example from slides)") {
      val relevance = List[Double](3, 2, 3, 0, 0, 1, 2, 2, 3, 0)
      val expected = List[Double](3, 5, 6.89, 6.89, 6.89, 7.28, 7.99, 8.66, 9.61, 9.61).map(_ +- 0.01)

      dcg(relevance) zip expected foreach {
        case (result, expected) => result shouldEqual expected
      }
    }
  }

  describe("normalizedDiscountedCumulativeGain") {
    it("normalizedDiscountedCumulativeGain(example from slides)") {
      val relevance = List[Double](3, 2, 3, 0, 0, 1, 2, 2, 3, 0)
      val expected = List[Double](1, 0.83, 0.87, 0.78, 0.71, 0.69, 0.73, 0.8, 0.88, 0.88).map(_ +- 0.01)

      ndcg(relevance) zip expected foreach {
        case (result, expected) => result shouldEqual expected
      }
    }
  }

}
