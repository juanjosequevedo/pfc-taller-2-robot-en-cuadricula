package taller

object App {
  def main(args: Array[String]): Unit = {
    val r = new RobotCuadricula()
    println(r.ejecutar(r.inicial, Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))))
    println(r.ejecutar(r.inicial, Secuencia(List(Avanzar(2), Girar(Derecha), Avanzar(4)))))
    println(r.girar(Norte, Derecha))
    println(r.profundidad(Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))))
  }
}
