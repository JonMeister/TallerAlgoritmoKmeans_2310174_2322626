import java.util.concurrent.ForkJoinTask

package object kmedianas2D{

  import scala.annotation.tailrec
  import scala.collection._
  import scala.collection.parallel.CollectionConverters._
  //import parallel.CollectionConverters._
  import scala.util.Random
  import common.{task,parallel}

  case class Punto(val x: Double, val y: Double) {
    private def cuadrado(v: Double): Double = v * v

    def distanciaAlCuadrado(that: Punto): Double = cuadrado(that.x - x) + cuadrado(that.y - y)

    private def round(v: Double) : Double = (v * 100).toInt / 100.0

    override def toString: String = s"(${round(x)},${round(y)})"
  }

  def generarPuntos(k: Int, num: Int): Seq[Punto] = {

    val randx = new Random(1)
    val randy = new Random(2)
    (0 until num)
      .map({ i =>
        val x = ( ( i + 1) % k ) * 1.0 / k + randx.nextDouble( ) * 0.5
        val y = ( ( i + 5) % k ) * 1.0 / k + randy.nextDouble( ) * 0.5
        new Punto ( x , y )
      })
  }

  def inicializarMedianas ( k : Int , puntos : Seq [ Punto ] ) : Seq [ Punto ] ={
    val rand = new Random( 7 )
    (0 until k ).map( _ =>puntos (rand.nextInt( puntos.length )))
  }

  // Clasificar puntos

  def hallarPuntoMasCercano(p : Punto , medianas : Seq [ Punto ] ) : Punto ={
    assert ( medianas.nonEmpty )
    medianas.map( pto=>( pto , p.distanciaAlCuadrado(pto))).sortWith ((a,b)=>( a._2 < b._2 )).head._1
  }

  // Versiones secuenciales
  def calculePromedioSeq( medianaVieja : Punto , puntos : Seq[Punto] ) : Punto = {
    if ( puntos.isEmpty ) medianaVieja
    else {
      new Punto ( puntos.map(p=>p.x).sum / puntos.length, puntos.map(p=>p.y).sum / puntos.length )
    }
  }

  def calculePromedioPar(medianaVieja : Punto , puntos : Seq[Punto] ) : Punto = {
    if (puntos.isEmpty) medianaVieja
    else {
      val puntosPar = puntos.par
      new Punto ( puntosPar.map(p=>p.x).sum / puntos.length, puntosPar.map(p=>p.y).sum / puntos.length)
    }
  }

  def clasificarSeq(puntos : Seq[Punto], medianas : Seq[Punto]) : Map[Punto, Seq[Punto]] = {

    val matchPuntos = puntos.groupBy(x => hallarPuntoMasCercano(x,medianas))
    //print(matchPuntos)
    matchPuntos
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

  // Versión completa que dependiendo de la entrada ejecuta secuencial o paralela mente
  def clasificar(umb: Int)(puntos : Seq[Punto], medianas : Seq[Punto]) : Map[Punto, Seq[Punto]] = {

    if(puntos.length > umb)  clasificarPar(umb)(puntos, medianas) else clasificarSeq(puntos, medianas)

  }

   def actualizarSeq(clasif : Map[Punto, Seq[Punto]], medianasViejas : Seq[Punto]) : Seq[Punto] = {

     val retorno = for {

       mv <- medianasViejas

     } yield calculePromedioSeq(mv, clasif(mv))
     retorno
   }


  // Hay dos niveles de paralelismo aquí, lo cual puede llegar a ser más costoso temporalmente
  // que la versión secuencial si la cantidad de datos no es lo suficientemente grande,
  // Por lo que esto se deja estipulado, dado que no hay especificaciones sobre cuando usar la versión
  // paralela y cuando la secuencial


  def actualizarPar(clasif : Map[Punto, Seq[Punto]], medianasViejas : Seq[Punto]) : Seq[Punto] = {

    val retorno = for{

      mv <- medianasViejas

    } yield calculePromedioPar(mv,clasif(mv))

    retorno
  }

  // Esta lo dejo así, puesto que describe un proceso iterativo
  def hayConvergenciaSeq(eta : Double, medianasViejas : Seq[Punto], medianasNuevas : Seq[Punto]) : Boolean = {

    def iterative(pos : Int): Boolean = {
      if (medianasNuevas(pos).distanciaAlCuadrado(medianasViejas(pos)) > eta) {
        false
      } else {
        if (pos == medianasNuevas.length - 1){
          true
        } else {
          iterative(pos+1)
        }
      }
    }

    iterative(0)

    /**
     //Otra posible implementación es esta de aquí usando expresión for y recorriendo, sin embargo
    //esta opción siempre es O(n), puesto que recorre siempre hasta el último elemento de cada mediana,
    //lo cual no pasa para la iterative, puesto que en el mejor caso sera O(1), pues los primeros puntos
    //no cumpliran con la condición.

     val limit : Int = medianasNuevas.length

     val calculos = (for{
      i <- 0 until limit
      if (medianasNuevas(i).distanciaAlCuadrado(medianasViejas(i)) < eta)
    } yield (medianasNuevas(i),medianasViejas(i))).distinct

    print(calculos)

    if(calculos.length == limit) true
    else false
    **/

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
  final def kMedianasSeq(puntos : Seq[Punto], medianas : Seq[Punto], eta : Double) : Seq[Punto] = {

    val clasificacion : Map[Punto, Seq[Punto]] = clasificarSeq(puntos, medianas)

    val medianasNuevas : Seq[Punto] = actualizarSeq(clasificacion,medianas)

    if(hayConvergenciaSeq(eta,medianas,medianasNuevas)){
      medianasNuevas
    }else{
      kMedianasSeq(puntos, medianasNuevas, eta)
    }

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