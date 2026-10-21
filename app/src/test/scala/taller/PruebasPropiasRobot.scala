package taller

import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PruebasPropiasRobot extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  // Punto 1 -------------------------------------------------------------------

  test("girar: desde el este dos giros a la izquierda dan Oeste") {
    assert(girar(girar(Este, Izquierda),Izquierda) == Oeste) }

  test("girar: desde el sur dos giros a la derecha dan Norte") {
    assert(girar(girar(Sur, Derecha),Derecha) == Norte) }

  test("girar: tres giros a la derecha desde Norte dan Oeste") {
    val tres = List(Derecha, Derecha, Derecha)
      .foldLeft(Norte: Direccion)((d, s) => girar(d, s))
    assert(tres == Oeste)}

  test("girar: alternar sentidos conserva la dirección inicial") {
    val alternar = List(Derecha, Izquierda, Izquierda, Derecha)
      .foldLeft(Este: Direccion)((d, s) => girar(d, s))
    assert(alternar == Este)}

  test("girar: cuatro giros a la izquierda desde Oeste conservan Oeste") {
    val cuatro = List(Izquierda, Izquierda, Izquierda, Izquierda)
      .foldLeft(Oeste: Direccion)((d, s) => girar(d, s))
    assert(cuatro == Oeste)}

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar: avanzar y retroceder en el mismo sentido se compensan") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(7), Avanzar(-4)))) ==
      Estado(0, 3, Norte))
  }

  test("ejecutar: una secuencia de dirección durante el recorrido") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(3), Girar(Izquierda), Avanzar(2), Girar(Derecha), Avanzar(1)))) ==
      Estado(-2, 4, Norte))
  }


  test("ejecutar: repetir una secuencia actualiza posición y orientación") {
    val recorrido = Repetir(2,Secuencia(List(Avanzar(3), Girar(Derecha))))
    assert(ejecutar(Estado(1,1, Norte),recorrido) == Estado(4, 4, Sur))
  }

  test("ejecutar: avanzar negativamente hacia el sur aumenta Y") {
    assert(ejecutar(Estado(-2, 4, Sur), Avanzar(-3)) == Estado(-2,7,Sur))
  }

  test("ejecutar: una secuencia anidada conserva el orden de ejecucción") {
    assert(ejecutar(inicial, Secuencia(List(Avanzar(2),
      Secuencia(List(Girar(Izquierda), Avanzar(3)))))) == Estado(-3,2,Oeste))
  }

  test("ejecutar: avanzar al sur y luego al oeste cambia ambas coordenadas") {
    assert(ejecutar(Estado(8, 3, Sur),
      Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(2)))) ==
      Estado(6, 0, Oeste))
  }

  test("ejecutar: una repetición negativa deja intacto el estado") {
    assert(ejecutar(Estado(-3, 5, Este), Repetir(-2, Avanzar(4))) == Estado(-3, 5, Este))
  }

  // Punto 3 -------------------------------------------------------------------

  test("cuantasBasicas: cuenta las órdenes de una secuencia anidada") {
    assert(cuantasBasicas(Secuencia(List(Avanzar(2),
      Secuencia(List(Girar(Derecha),Avanzar(3)))))) == 3)
  }

  test("cuantasBasicas: multiplica las órdenes de una secuencia repetida") {
    assert(cuantasBasicas(Repetir(3,Secuencia(List(Avanzar(1),
      Girar(Izquierda), Avanzar(2))))) == 9)
  }

  test("profundidad: una repetición de una orden básica tiene nivel dos") {
    assert(profundidad(Repetir(3, Girar(Derecha))) == 2)
  }

  test("profundidad: una secuencia dentro de otra aumenta el nivel") {
    assert(profundidad(Secuencia(List(Avanzar(1),Secuencia(List(
      Girar(Izquierda),Avanzar(2)))))) == 3)
  }

  test("profundidad: las repeticiones anidadas cuentan cada nivel") {
    assert(profundidad(Repetir(2,
      Repetir(3,Avanzar(1)))) == 3)
  }

  // Punto 4 -------------------------------------------------------------------

  test("componer: una orden compuesta conserva las dos órdenes") {
    assert(componer(Girar(Izquierda),Avanzar(4)) ==
      Secuencia(List(Girar(Izquierda),Avanzar(4))))
  }

  test("componer: la composición respeta el orden de ejecucción") {
    assert(ejecutar(Estado(2,-1,Norte), componer(Avanzar(3), Girar(Derecha))) ==
      ejecutar(ejecutar(Estado(2,-1,Norte), Avanzar(3)), Girar(Derecha)))
  }

  test("rutina: construye tres avances con giro a la izquierda") {
    assert(rutina(2)(Izquierda)(3) ==
      Repetir(3,Secuencia(List(Avanzar(2),Girar(Izquierda)))))
  }

  test("aplicarA: ejecuta una misma orden sobre estados diferentes") {
    assert(aplicarA(List( Estado(1, 2, Norte),Estado(4, -1, Oeste), Estado(-2, 3, Sur)),
      Avanzar(5)) ==
      List(Estado(1, 7, Norte), Estado(-1, -1, Oeste), Estado(-2, -2, Sur)))
  }

  test("transformar: cuadruplica los avances dentro de una repetición y conserva los giros") {
    val orden = transformar(
      Repetir(2, Secuencia(List(Avanzar(2), Girar(Izquierda), Avanzar(3)))),
      {
        case Avanzar(n) => Avanzar(4 * n)
        case otra => otra
      }
    )
    assert(orden == Repetir(2, Secuencia(List(
      Avanzar(8), Girar(Izquierda), Avanzar(12)
    ))))
  }

  test("transformar: reemplaza los giros sin alterar los avances") {
    val orden = transformar(Secuencia(List(Girar(Izquierda),
      Avanzar(3),Girar(Derecha))), {
      case Girar(_) => Avanzar(1)
      case otra => otra
    })
    assert(orden == Secuencia(List(Avanzar(1),Avanzar(3),Avanzar(1))))
  }

  // Punto 5 -------------------------------------------------------------------

  test("simplificar: combina varios avances consecutivos") {
    assert(simplificar(Secuencia(List(Avanzar(2), Avanzar(7), Avanzar(-3)))) == Avanzar(6))
  }

  test("simplificar: reduce una secuencia anidada de un solo elemento") {
    assert(simplificar(Secuencia(List(
      Secuencia(List(Girar(Izquierda)))
    ))) == Girar(Izquierda))
  }

  test("simplificar: elimina avances que se cancelan antes de un giro") {
    assert(simplificar(Secuencia(List(
      Avanzar(4), Avanzar(-4), Girar(Izquierda)
    ))) == Girar(Izquierda))
  }

  test("simplificar: aplicar dos veces produce el mismo resultado") {
    val orden = Secuencia(List(
      Repetir(1, Secuencia(List(Avanzar(2), Avanzar(3)))),
      Avanzar(0),
      Girar(Izquierda)
    ))
    assert(simplificar(simplificar(orden)) == simplificar(orden))
  }

  test("simplificar: conserva el resultado al ejecutar una orden compuesta") {
    val orden = Repetir(3, Secuencia(List(
      Avanzar(2),
      Avanzar(3),
      Girar(Derecha),
      Avanzar(0)
    )))
    val estados = List(
      inicial,
      Estado(3, -2, Este),
      Estado(-4, 5, Sur)
    )

    assert(estados.forall(e =>
      ejecutar(e, orden) == ejecutar(e, simplificar(orden))))
  }
}