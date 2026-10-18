package taller

import scala.annotation.tailrec

/**
 * Taller 2 — órdenes para un robot en una cuadrícula infinita.
 *
 * El tipo `Orden` es recursivo: `Repetir` y `Secuencia` contienen otras
 * órdenes. Toda función que lo recorra lo es también, con un caso por cada
 * alternativa del tipo.
 *
 * Tal como está, el proyecto compila y las pruebas quedan en rojo.
 */
class RobotCuadricula {

  /** El robot empieza en el origen mirando al norte. */
  val inicial = Estado(0, 0, Norte)

  // Punto 1 -------------------------------------------------------------------

  /** La dirección que queda tras girar noventa grados hacia el sentido dado. */
  def girar(d: Direccion, s: Sentido): Direccion = (d, s) match {
  case (Norte, Derecha) => Este
  case (Este, Derecha) => Sur
  case (Sur, Derecha) => Oeste
  case (Oeste, Derecha) => Norte
  case (Norte, Izquierda) => Oeste
  case (Oeste, Izquierda) => Sur
  case (Sur, Izquierda) => Este
  case (Este, Izquierda) => Norte
  }
  // Punto 2 -------------------------------------------------------------------

  /** El estado que resulta de aplicarle la orden al estado dado. */
  def ejecutar(e: Estado, o: Orden): Estado = o match {
    case  Avanzar(n) =>
      e.mira match {
        case Norte => Estado(e.x, e.y + n, e.mira)
        case Este => Estado(e.x + n, e.y, e.mira)
        case Sur => Estado(e.x, e.y - n, e.mira)
        case Oeste => Estado(e.x - n, e.y, e.mira)
      }

    case Girar(sentido) => Estado(e.x, e.y, girar(e.mira, sentido))

    case Repetir(veces, _) if veces <= 0 => e

    case Repetir(veces, orden) => ejecutar(ejecutar(e, orden), Repetir(veces - 1, orden))

    case Secuencia(ordenes) =>
      @tailrec
      def aux(estado: Estado, pendientes: List[Orden]): Estado =
        pendientes match {
          case Nil => estado
          case cabeza :: cola => aux(ejecutar(estado, cabeza), cola)
        }

      aux(e, ordenes)
  }

  // Punto 3 -------------------------------------------------------------------

  /** Cuántos Avanzar y Girar llega a ejecutar la orden, contando repeticiones. */
  def cuantasBasicas(o: Orden): Int = o match {
    case Avanzar(_) => 1

    case Girar(_) => 1

    case Repetir(veces, _) if veces <= 0 => 0

    case Repetir(veces, orden) => veces * cuantasBasicas(orden)

    case Secuencia(Nil) => 0

    case Secuencia(cabeza :: cola) => cuantasBasicas(cabeza) + cuantasBasicas(Secuencia(cola))
  }

  /** Niveles de anidamiento: una básica tiene 1, una compuesta uno más que la más honda. */
  def profundidad(o: Orden): Int = o match {
    case Avanzar(_) => 1

    case Girar(_) => 1

    case Repetir(_, orden) => 1 + profundidad(orden)

    case Secuencia(Nil) => 1

    case Secuencia(cabeza :: cola) =>
      val profundidadCabeza = profundidad(cabeza) + 1
      val profundidadCola = profundidad(Secuencia(cola))

      if(profundidadCabeza < profundidadCola) {
        profundidadCola
      }
      else {
        profundidadCabeza
      }
  }

  // Punto 4 -------------------------------------------------------------------

  /** La secuencia de las dos órdenes. */
  def componer(o1: Orden, o2: Orden): Orden =
    Secuencia(List(o1,o2))

  /** Currificada: fijados los pasos y el giro, queda una función de las veces. */
  def rutina(pasos: Int)(giro: Sentido)(veces: Int): Orden =
    Repetir(veces, componer(Avanzar(pasos), Girar(giro)))

  /** La misma orden ejecutada sobre cada uno de los estados. */
  def aplicarA(estados: List[Estado], o: Orden): List[Estado] = estados match {
    case Nil => Nil

    case cabeza :: cola => ejecutar(cabeza, o) :: aplicarA(cola, o)
  }

  /** Reemplaza cada orden básica por f(orden), respetando la estructura de las compuestas. */
  def transformar(o: Orden, f: Orden => Orden): Orden = o match {
    case Avanzar(_) => f(o)

    case Girar(_) => f(o)

    case Repetir(veces, orden) => Repetir(veces, transformar(orden, f))

    case Secuencia(Nil) => Secuencia(Nil)

    case Secuencia(ordenes) => Secuencia(ordenes.map(orden => transformar(orden, f)))

  }

  // Punto 5 -------------------------------------------------------------------

  /** Una orden equivalente más corta, con las reglas del enunciado. */
  def simplificar(o: Orden): Orden = ???
}
