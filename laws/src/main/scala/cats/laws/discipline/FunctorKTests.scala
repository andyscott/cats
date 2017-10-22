package cats
package laws
package discipline

import org.scalacheck.{Arbitrary, Prop}
import Prop._

import org.typelevel.discipline.Laws

trait FunctorKTests[A[_[_]]] extends Laws {
  def laws: FunctorKLaws[A]

  def functorK[F[_], G[_], H[_]](f: F ~> G, g: G ~> H)(implicit
    ArbAF: Arbitrary[A[F]],
    EqAF: Eq[A[F]],
    EqAH: Eq[A[H]]
  ): RuleSet = {
    new DefaultRuleSet(
      name = "functorK",
      parent = None,
      "covariant identity" -> forAll(laws.covariantIdentity[F] _),
      "covariant composition" -> forAll(laws.covariantComposition[F, G, H](f, g) _))
  }
}

object FunctorKTests {
  def apply[A[_[_]]: FunctorK]: FunctorKTests[A] =
    new FunctorKTests[A] { def laws: FunctorKLaws[A] = FunctorKLaws[A] }
}
