import kmedianas2D._

object pruebas {
  def main(args: Array[String]): Unit = {
    val puntos = Seq(new Punto(1, 2), new Punto(2, 3), new Punto(1, 1))

    val medianas = Seq(new Punto(1, 2), new Punto(2, 2))

    val cps = clasificarSeq(puntos,medianas)

    val cpp = clasificarPar(2)(puntos,medianas)

    val as = actualizarSeq(cps,medianas)
    val ap = actualizarPar(cpp,medianas)
    
    val hcs = hayConvergenciaSeq(0.5,medianas,as)
    val hcp = hayConvergenciaPar(0.5,medianas,ap)

    print(cps,cpp,as,ap)
  }
}

