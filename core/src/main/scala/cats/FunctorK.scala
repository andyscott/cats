package cats

import simulacrum.typeclass

/**
  * FunctorK.
  *
  * A covariant functor in the category of functors.
  *
  * Must obey the laws defined in cats.laws.FunctorKLaws.
  */
@typeclass
trait FunctorK[A[_[_]]] {

  def mapK[F[_], G[_]](af: A[F])(f: F ~> G): A[G]

}
