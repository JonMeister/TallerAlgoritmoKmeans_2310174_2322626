import java.util.concurrent.ForkJoinTask

package object kmedianas2D{
  import scala.annotation.tailrec
  import scala.collection.immutable.{Map,Seq}
  import scala.collection.parallel.CollectionConverters._
  import scala.util.Random
  import common._

  // Definiciones comunes para las dos versiones (secuencial y paralela)

  class Punto(val x: Double, val y: Double) {
    private def cuadrado(v: Double): Double = v * v

    def distanciaAlCuadrado(that: Punto): Double =
      cuadrado(that.x - x) + cuadrado(that.y - y)

    private def round(v: Double): Double = (v * 100).toInt / 100.0

    override def toString: String = s"(${round(x)}, ${round(y)})"
  }

  def generarPuntos(k: Int, num: Int): Seq[Punto] = {
    val randx = new Random
    val randy = new Random
    (0 until num).map { i =>
      val x = ((i + 1) % k) * 1.0 / k + randx.nextDouble() * 0.5
      val y = ((i + 5) % k) * 1.0 / k + randy.nextDouble() * 0.5
      new Punto(x, y)
    }
  }

  def inicializarMedianas(k: Int, puntos: Seq[Punto]): Seq[Punto] = {
    val rand = new Random
    (0 until k).map(_ => puntos(rand.nextInt(puntos.length)))
  }

  // Clasificar puntos
  def hallarPuntoMasCercano(p: Punto, medianas: Seq[Punto]): Punto = {
    assert(medianas.nonEmpty)
    medianas
      .map(pto => (pto, p.distanciaAlCuadrado(pto)))
      .sortWith((a, b) => a._2 < b._2)
      .head._1
  }

  // Versiones secuenciales

  def calculePromedioSeq(medianaVieja: Punto, puntos: Seq[Punto]): Punto = {
    if (puntos.isEmpty) medianaVieja
    else {
      new Punto(
        puntos.map(p => p.x).sum / puntos.length,
        puntos.map(p => p.y).sum / puntos.length
      )
    }
  }

  def clasificarSeq(puntos: Seq[Punto], medianas: Seq[Punto]): Map[Punto, Seq[Punto]] = {
    puntos.groupBy(p => hallarPuntoMasCercano(p, medianas))
  }

  def actualizarSeq(clasif: Map[Punto, Seq[Punto]], medianasViejas: Seq[Punto]): Seq[Punto] = {
    for {
      mediana <- medianasViejas
      if clasif.contains(mediana)
      puntosAsignados = clasif(mediana)
    } yield calculePromedioSeq(mediana, puntosAsignados)
  }


  @tailrec
  def hayConvergenciaSeq(eta: Double, medianasViejas: Seq[Punto], medianasNuevas: Seq[Punto]): Boolean = {
    if (medianasViejas.isEmpty && medianasNuevas.isEmpty) true
    else if (medianasViejas.isEmpty || medianasNuevas.isEmpty) false
    else {
      val distancia = math.sqrt(medianasViejas.head.distanciaAlCuadrado(medianasNuevas.head))
      if (distancia > eta) false
      else hayConvergenciaSeq(eta, medianasViejas.tail, medianasNuevas.tail)
    }
  }

  @tailrec
  final def kMedianasSeq(puntos: Seq[Punto], medianas: Seq[Punto], eta: Double): Seq[Punto] = {
    // Clasificar los puntos según la mediana más cercana
    val clasificacion = clasificarSeq(puntos, medianas)

    // Calcular las nuevas medianas basadas en los puntos clasificados
    val nuevasMedianas = actualizarSeq(clasificacion, medianas)

    // Verificar si las medianas han convergido
    if (hayConvergenciaSeq(eta, medianas, nuevasMedianas)) nuevasMedianas
    else kMedianasSeq(puntos, nuevasMedianas, eta)
  }

  // Versiones paralelas

  def calculePromedioPar(medianaVieja : Punto , puntos : Seq[Punto] ) : Punto = {
    if (puntos.isEmpty) medianaVieja
    else {
      val puntosPar = puntos.par
      new Punto ( puntosPar.map(p=>p.x).sum / puntos.length, puntosPar.map(p=>p.y).sum / puntos.length)
    }
  }

  def clasificarPar(umb: Int)(puntos : Seq[Punto], medianas : Seq[Punto]) : Map[Punto, Seq[Punto]] = {

    if(umb == 0){
      clasificarSeq(puntos,medianas)
    }else {
      val middle : Int = (puntos.length + 1) / 2
      val (part1,part2) = parallel(clasificarPar(umb-1)(puntos.slice(0,middle),medianas)
        ,clasificarPar(umb-1)(puntos.slice(middle,puntos.length),medianas))
      part1 ++ part2.map{case (key,value) => key -> (part1.getOrElse(key,List()) ++ value) }
    }
  }

  def actualizarPar(clasif: Map[Punto, Seq[Punto]], medianasViejas: Seq[Punto]): Seq[Punto] = {
    val retorno = for {
      mv <- medianasViejas
      puntosAsignados = clasif.getOrElse(mv, Seq())
    } yield calculePromedioPar(mv, puntosAsignados)

    retorno
  }


  def hayConvergenciaPar(eta : Double, medianasViejas : Seq[Punto], medianasNuevas : Seq[Punto]) : Boolean = {

    def EsConvergenteElPunto(PuntoNuevo: Punto, PuntoViejo : Punto) : Boolean = {
      if (PuntoNuevo.distanciaAlCuadrado(PuntoViejo) > eta) false else true
    }

    // Versión usando task, esto hace que para cada punto de cada secuencia de medianas genere una
    // linea de procesamiento, luego comprueba con el forall si todos los resultados dieron true
    // esto es equivalente a que todas los puntos convergen, luego el resultado es que las medianas
    // convergen, sino divergen
    def taskVersion : Boolean = {

      val forks : IndexedSeq[ForkJoinTask[Boolean]] = (for{
        i <- medianasNuevas.indices
      } yield task(EsConvergenteElPunto(medianasNuevas(i),medianasViejas(i))))

      val valueOfJoins = forks.forall(x => x.join())

      valueOfJoins
    }

    taskVersion

  }

  @tailrec
  final def kMedianasPar(puntos: Seq[Punto], medianas: Seq[Punto], eta: Double): Seq[Punto] = {

    // Modificar valor del umbral en clasificarPar -> preguntar al profesor sobre esto
    val clasificacion: Map[Punto, Seq[Punto]] = clasificarPar(2)(puntos, medianas)

    val medianasNuevas: Seq[Punto] = actualizarPar(clasificacion, medianas)

    if (hayConvergenciaPar(eta, medianas, medianasNuevas)) {
      medianasNuevas
    } else {
      kMedianasPar(puntos, medianasNuevas, eta)
    }

  }

}