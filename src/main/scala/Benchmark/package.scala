package object Benchmark {
  import scala.concurrent.{Future, Await}
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
  import kmedianas2D._
  import org.scalameter._
  import plotly._, element._, layout._

  def umbral(npuntos: Int): Int = {
    // Si npuntos= 2^n, entonces el umbral será 2^(n/2)
    math.pow(2, ((math.log(npuntos) / math.log(2)) / 2).toInt).toInt
  }

  def tiempoDe[T](body: => T) = {
    val timeA1 = config(
      KeyValue(Key.exec.minWarmupRuns -> 20),
      KeyValue(Key.exec.maxWarmupRuns -> 60),
      KeyValue(Key.verbose -> false)
    ) withWarmer(new Warmer.Default) measure (body)
    timeA1
  }

  def tiemposKmedianas(numPuntos:Int, k:Int, eta:Double) = {
    val puntos = generarPuntos(k, numPuntos)
    val medianas = inicializarMedianas(k, puntos)
    val tiempoSeq = tiempoDe(kMedianasSeq(puntos, medianas, eta))
    //    val tiempoPar = tiempoDe(kMedianasPar(puntos, medianas, eta))
    //    (tiempoSeq,tiempoPar, tiempoSeq.value / tiempoPar.value)
    (tiempoSeq)

  }

  def probarKmedianas(numPuntos:Int, k:Int, eta:Double) = {
    // Probar lo secuencial
    val puntosSeq = generarPuntos(k, numPuntos)
    val medianasSeq = inicializarMedianas(k, puntosSeq)
    val medianasSeqfin = kMedianasSeq(puntosSeq, medianasSeq, eta)
    val clasifFinalSeq = clasificarSeq(puntosSeq,medianasSeqfin)
    val tiempoSeq = tiempoDe(kMedianasSeq(puntosSeq, medianasSeq, eta))

    // Hacer gráfica de los resultados del proceso secuencial
    val trazosSeq = for {
      (p,pseq) <- clasifFinalSeq
      ejeXseq = for {
        pto <- pseq
      } yield pto.x
      ejeYseq = for {
        pto <- pseq
      } yield pto.y
    } yield Scatter(
      ejeXseq,
      ejeYseq
    ).withMode(ScatterMode(ScatterMode.Markers)).withName(s"Puntos: $p.x" ++ s"$p.y")

    val ejeXMedianasSeq = for {
      p <- medianasSeq
    } yield p.x

    val ejeYMedianasSeq = for {
      p <- medianasSeq
    } yield p.y

    val ejeXMedianasFinSeq = for {
      p <- medianasSeqfin
    } yield p.x

    val ejeYMedianasFinSeq = for {
      p <- medianasSeqfin
    } yield p.y

    val trazo2Seq= Scatter(
      ejeXMedianasSeq,
      ejeYMedianasSeq
    ).withMode(ScatterMode(ScatterMode.Markers)).withName("Medianas")

    val trazo3Seq= Scatter(
      ejeXMedianasFinSeq,
      ejeYMedianasFinSeq
    ).withMode(ScatterMode(ScatterMode.Markers)).withName("Medianas Finales")

    val dataSeq = trazo2Seq +:  (trazo3Seq +: trazosSeq.toSeq)

    val layoutSeq = Layout().withTitle("Plotting de puntos al azar y medianas iniciales y finales - Versión Secuencial")

    def plotGrafico1(dataSeq: Seq[Trace], layoutSeq: Layout): Future[Unit]= Future{
      Plotly.plot("kmedianasSeq.html", dataSeq, layoutSeq)
    }





      // Probar lo paralelo
      val puntosPar = puntosSeq
      val medianasPar = medianasSeq
      val medianasParfin = kMedianasPar(puntosPar, medianasPar, eta)
      val clasifFinalPar = clasificarPar(umbral(puntosPar.length))(puntosPar,medianasParfin)
      val tiempoPar = tiempoDe(kMedianasPar(puntosPar, medianasPar, eta))

      // Hacer gráfica de los resultados del proceso paralelo
      val trazosPar = for {
        (p,ppar) <- clasifFinalPar
        ejeXpar = for {
          pto <- ppar
        } yield pto.x
        ejeYpar = for {
          pto <- ppar
        } yield pto.y
      } yield Scatter(
        ejeXpar.toSeq,
        ejeYpar.toSeq
      ).withMode(ScatterMode(ScatterMode.Markers)).withName(s"Puntos: $p.x" ++ s"$p.y")

      val ejeXMedianasPar = for {
        p <- medianasPar
      } yield p.x

      val ejeYMedianasPar = for {
        p <- medianasPar
      } yield p.y

      val ejeXMedianasFinPar = for {
        p <- medianasParfin
      } yield p.x

      val ejeYMedianasFinPar = for {
        p <- medianasParfin
      } yield p.y

      val trazo2Par= Scatter(
        ejeXMedianasPar,
        ejeYMedianasPar
      ).withMode(ScatterMode(ScatterMode.Markers)).withName("Medianas")

      val trazo3Par= Scatter(
        ejeXMedianasFinPar,
        ejeYMedianasFinPar
      ).withMode(ScatterMode(ScatterMode.Markers)).withName("Medianas Finales")

      val dataPar = (trazo2Par +:  (trazo3Par +: trazosPar.toSeq))

      val layoutPar = Layout().withTitle("Plotting de puntos al azar y medianas iniciales y finales - Versión Paralela")

    def plotGrafico2(dataPar: Seq[Trace], layoutPar: Layout): Future[Unit]= Future{
      Plotly.plot("kmedianasPar.html", dataPar, layoutPar)
    }
    val future1=plotGrafico1(dataSeq,layoutSeq)
    println("Se imprimieron los resultados secuenciales, revise archivo .html")
    val future2=plotGrafico2(dataPar.toSeq,layoutPar)
    println("Se imprimieron los resultados paralelos, revise archivo .html")

    (tiempoSeq, tiempoPar, tiempoSeq.value/tiempoPar.value)
    }
}

//  (medianasSeq, medianasSeqfin, clasifFinalSeq, tiempoSeq, tiempoSeq.value)
//  (medianasPar, medianasParfin, clasifFinalPar, tiempoPar, tiempoPar.value)