import Benchmark._
import kmedianas2D._

object EjecucionKMedianas extends App {

  val puntos1_1 = generarPuntos(2, 128)
  println("Ejecutando tiemposKmedianas con 2 clusters, 128 puntos y eta 0.01")
  val resultado1_1 = tiemposKmedianas(puntos1_1, 2, 0.01)
  println(resultado1_1)

  val puntos1_2 = generarPuntos(4, 128)
  println("Ejecutando tiemposKmedianas con 4 clusters, 128 puntos y eta 0.01")
  val resultado1_2 = tiemposKmedianas(puntos1_2, 4, 0.01)
  println(resultado1_2)

  val puntos1_3 = generarPuntos(8, 128)
  println("Ejecutando tiemposKmedianas con 8 clusters, 128 puntos y eta 0.01")
  val resultado1_3 = tiemposKmedianas(puntos1_3, 8, 0.01)
  println(resultado1_3)

  val puntos2_1 = generarPuntos(8, 512)
  println("Ejecutando tiemposKmedianas con 8 clusters, 512 puntos y eta 0.01")
  val resultado2_1 = tiemposKmedianas(puntos2_1, 8, 0.01)
  println(resultado2_1)

  val puntos2_2 = generarPuntos(16, 512)
  println("Ejecutando tiemposKmedianas con 16 clusters, 512 puntos y eta 0.01")
  val resultado2_2 = tiemposKmedianas(puntos2_2, 16, 0.01)
  println(resultado2_2)

  val puntos2_3 = generarPuntos(32, 512)
  println("Ejecutando tiemposKmedianas con 32 clusters, 512 puntos y eta 0.01")
  val resultado2_3 = tiemposKmedianas(puntos2_3, 32, 0.01)
  println(resultado2_3)

  val puntos3_1 = generarPuntos(16, 4096)
  println("Ejecutando tiemposKmedianas con 16 clusters, 4096 puntos y eta 0.01")
  val resultado3_1 = tiemposKmedianas(puntos3_1, 16, 0.01)
  println(resultado3_1)

  val puntos3_2 = generarPuntos(32, 4096)
  println("Ejecutando tiemposKmedianas con 32 clusters, 4096 puntos y eta 0.01")
  val resultado3_2 = tiemposKmedianas(puntos3_2, 32, 0.01)
  println(resultado3_2)

  val puntos3_3 = generarPuntos(64, 4096)
  println("Ejecutando tiemposKmedianas con 64 clusters, 4096 puntos y eta 0.01")
  val resultado3_3 = tiemposKmedianas(puntos3_3, 64, 0.01)
  println(resultado3_3)

  val puntos4_1 = generarPuntos(32, 16384)
  println("Ejecutando tiemposKmedianas con 32 clusters, 16384 puntos y eta 0.01")
  val resultado4_1 = tiemposKmedianas(puntos4_1, 32, 0.01)
  println(resultado4_1)

  val puntos4_2 = generarPuntos(64, 16384)
  println("Ejecutando tiemposKmedianas con 64 clusters, 16384 puntos y eta 0.01")
  val resultado4_2 = tiemposKmedianas(puntos4_2, 64, 0.01)
  println(resultado4_2)

  val puntos4_3 = generarPuntos(128, 16384)
  println("Ejecutando tiemposKmedianas con 128 clusters, 16384 puntos y eta 0.01")
  val resultado4_3 = tiemposKmedianas(puntos4_3, 128, 0.01)
  println(resultado4_3)

  val puntos5_1 = generarPuntos(64, 65536)
  println("Ejecutando tiemposKmedianas con 64 clusters, 65536 puntos y eta 0.01")
  val resultado5_1 = tiemposKmedianas(puntos5_1, 64, 0.01)
  println(resultado5_1)

  val puntos5_2 = generarPuntos(128, 65536)
  println("Ejecutando tiemposKmedianas con 128 clusters, 65536 puntos y eta 0.01")
  val resultado5_2 = tiemposKmedianas(puntos5_2, 128, 0.01)
  println(resultado5_2)

  val puntos5_3 = generarPuntos(256, 65536)
  println("Ejecutando tiemposKmedianas con 256 clusters, 65536 puntos y eta 0.01")
  val resultado5_3 = tiemposKmedianas(puntos5_3, 256, 0.01)
  println(resultado5_3)

}

