package cats
package tests

import org.scalatest.prop.PropertyChecks
import org.scalacheck.Arbitrary

import cats.instances.all._

abstract class TraverseCheck[F[_]: Traverse](name: String)(implicit ArbFInt: Arbitrary[F[Int]]) extends CatsSuite with PropertyChecks {

  test(s"Traverse[$name].zipWithIndex") {
    forAll { (fa: F[Int]) =>
      fa.zipWithIndex.toList should === (fa.toList.zipWithIndex)
    }
  }

  test(s"Traverse[$name].mapWithIndex") {
    forAll { (fa: F[Int], fn: ((Int, Int)) => Int) =>
      fa.mapWithIndex((a, i) => fn((a, i))).toList should === (fa.toList.zipWithIndex.map(fn))
    }
  }

  test(s"Traverse[$name].traverseWithIndex") {
    forAll { (fa: F[Int], fn: ((Int, Int)) => (Int, Int)) =>
      val left = fa.traverseWithIndex((a, i) => fn((a, i))).map(_.toList)
      val (xs, values) = fa.toList.zipWithIndex.map(fn).unzip
      left should === ((xs.combineAll, values))
    }
  }

}

object TraverseCheck {
  // forces testing of the underlying implementation (avoids overridden methods)
  abstract class Underlying[F[_]: Traverse](name: String)(implicit ArbFInt: Arbitrary[F[Int]])
      extends TraverseCheck(s"$name (underlying)")(proxyTraverse[F], ArbFInt)

  // proxies a traverse instance so we can test default implementations
  // to achieve coverage using default datatype instances
  private def proxyTraverse[F[_]: Traverse]: Traverse[F] = new Traverse[F] {
    def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B =
      Traverse[F].foldLeft(fa, b)(f)
    def foldRight[A, B](fa: F[A], lb: cats.Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      Traverse[F].foldRight(fa, lb)(f)
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]] =
      Traverse[F].traverse(fa)(f)
  }
}

class TraverseListCheck   extends TraverseCheck[List]("List")
class TraverseStreamCheck extends TraverseCheck[Stream]("Stream")
class TraverseVectorCheck extends TraverseCheck[Vector]("Vector")

class TraverseListCheckUnderlying   extends TraverseCheck.Underlying[List]("List")
class TraverseStreamCheckUnderlying extends TraverseCheck.Underlying[Stream]("Stream")
class TraverseVectorCheckUnderlying extends TraverseCheck.Underlying[Vector]("Vector")

class TraverseTestsAdditional extends CatsSuite {

  def checkZipWithIndexedStackSafety[F[_]](fromRange: Range => F[Int])(implicit F: Traverse[F]): Unit = {
    F.zipWithIndex(fromRange(1 to 70000))
    ()
  }

  test("Traverse[List].zipWithIndex stack safety") {
    checkZipWithIndexedStackSafety[List](_.toList)
  }

  test("Traverse[Stream].zipWithIndex stack safety") {
    checkZipWithIndexedStackSafety[Stream](_.toStream)
  }

  test("Traverse[Vector].zipWithIndex stack safety") {
    checkZipWithIndexedStackSafety[Vector](_.toVector)
  }
}
