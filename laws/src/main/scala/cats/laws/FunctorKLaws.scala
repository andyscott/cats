package cats
package laws

import cats.arrow.FunctionK
import cats.syntax.functork._

/**
  * Laws that must be obeyed by any `FunctorK`.
  */
trait FunctorKLaws[A[_[_]]] {
  implicit def A: FunctorK[A]

  def covariantIdentity[F[_]](af: A[F]): IsEq[A[F]] =
    af.mapK(FunctionK.id) <-> af

  def covariantComposition[F[_], G[_], H[_]](f: F ~> G, g: G ~> H)(af: A[F]): IsEq[A[H]] =
    af.mapK(f).mapK(g) <-> af.mapK(f andThen g)
}

object FunctorKLaws {
  def apply[A[_[_]]](implicit ev: FunctorK[A]): FunctorKLaws[A] =
    new FunctorKLaws[A] { def A: FunctorK[A] = ev }
}
