import java.io.{File, PrintWriter}
import scala.util.{Try, Using}

object Main {
  def main(args: Array[String]): Unit = {
    println("B-TREE INMUTABLE - EXPERIMENTOS NETFLIX")
    println("=" * 50)

    // Verificar que existe el dataset
    val datasetPath = if (args.length > 0) args(0) else "data/netflix_titles.csv"
    val datasetFile = new java.io.File(datasetPath)

    if (!datasetFile.exists()) {
      println(s"""
                 |Dataset no encontrado: $datasetPath
                 |
                 |Por favor descarga el dataset de:
                 |https://www.kaggle.com/datasets/shivamb/netflix-shows
                 |
                 |Y colócalo en: data/netflix_titles.csv
                 |
                 |O ejecuta con: sbt "run /ruta/a/tu/dataset.csv"
                 |
                 |Mientras tanto, ejecutando con datos de prueba...
                 |""".stripMargin)

      // Ejecutar con datos de prueba en lugar de fallar
      runWithSampleData()
    } else {
      // Ejecutar con dataset real
      runWithRealData(datasetPath)
    }
  }

  private def runWithRealData(datasetPath: String): Unit = {
    println("CARGANDO DATASET REAL DE NETFLIX...")

    // 1. Cargar datos
    val records = NetflixDataLoader.loadCSV(datasetPath)
    val validRecords = NetflixDataLoader.filterValidRecords(records)

    if (validRecords.isEmpty) {
      println("No se pudieron cargar registros válidos")
      return
    }

    // 2. Mostrar análisis del dataset
    NetflixDataLoader.analyzeDataset(validRecords)
    NetflixDataLoader.showSampleRecords(validRecords, 3)

    // 3. Configurar experimentos
    val sampleSizes = calculateSampleSizes(validRecords.length)
    val t = 3 // Grado mínimo del B-tree

    println(s"\nCONFIGURACIÓN DE EXPERIMENTOS:")
    println(s"   • Tamaños de muestra: ${sampleSizes.mkString(", ")}")
    println(s"   • Grado mínimo (t): $t")
    println(s"   • Total de registros: ${validRecords.length}")

    // 4. Ejecutar experimentos completos
    val results = ExperimentRunner.runNetflixExperiment(validRecords, t, sampleSizes)

    if (results.nonEmpty) {
      // 5. Guardar resultados para gráficas
      ensureResultsDirectory()
      ExperimentRunner.saveResultsToCSV(results, "results/experiment_results.csv")

      // 6. GENERAR ARCHIVOS ESPECÍFICOS PARA GRÁFICAS
      ExperimentRunner.generateChartData(results)

      // 7. Generar y guardar reporte de análisis
      val report = ExperimentRunner.generateAnalysisReport(results)

      Using(new PrintWriter(new File("results/analisis_final.md"))) { writer =>
        writer.write(report)
      }

      println("\nEXPERIMENTOS COMPLETADOS EXITOSAMENTE")
      println("Resultados guardados en carpeta: results/")
      println("atos para gráficas: results/experiment_results.csv")
      println("Archivos de gráficas: results/graficas/")
      println("Reporte analítico: results/analisis_final.md")

      // Mostrar qué archivos se crearon
      printGeneratedFiles()

      // Mostrar resumen en consola
      println("\n" + "=" * 50)
      println("RESUMEN EJECUTIVO")
      println("=" * 50)
      printExecutiveSummary(results)

    } else {
      println("No se generaron resultados de experimentos")
    }
  }

  private def runWithSampleData(): Unit = {
    println("EJECUTANDO CON DATOS DE PRUEBA...")

    // Crear datos de prueba
    val sampleRecords = List(
      NetflixRecord("s1", "The Matrix", "Movie", "Lana Wachowski", "Keanu Reeves, Laurence Fishburne", "USA", "1999-03-31", 1999, "R", "136 min", "Action, Sci-Fi", "A computer hacker learns from mysterious rebels about the true nature of his reality."),
      NetflixRecord("s2", "Inception", "Movie", "Christopher Nolan", "Leonardo DiCaprio, Joseph Gordon-Levitt", "USA", "2010-07-16", 2010, "PG-13", "148 min", "Action, Thriller", "A thief who steals corporate secrets through dream-sharing technology."),
      NetflixRecord("s3", "Stranger Things", "TV Show", "The Duffer Brothers", "Millie Bobby Brown, Finn Wolfhard", "USA", "2016-07-15", 2016, "TV-14", "4 Seasons", "Horror, Mystery", "When a young boy vanishes, a small town uncovers a mystery involving secret experiments."),
      NetflixRecord("s4", "The Crown", "TV Show", "Peter Morgan", "Claire Foy, Olivia Colman", "UK", "2016-11-04", 2016, "TV-MA", "4 Seasons", "Drama, History", "The reign of Queen Elizabeth II of the United Kingdom."),
      NetflixRecord("s5", "La Casa de Papel", "TV Show", "Álex Pina", "Úrsula Corberó, Álvaro Morte", "Spain", "2017-05-02", 2017, "TV-MA", "5 Seasons", "Action, Crime", "An unusual group of robbers attempt to carry out the most perfect robbery.")
    )

    println(s"Usando ${sampleRecords.length} registros de prueba")

    // Ejecutar prueba rápida
    ExperimentRunner.runQuickTest(sampleRecords, 2)

    println("\nPara experimentos completos, descarga el dataset real:")
    println("   https://www.kaggle.com/datasets/shivamb/netflix-shows")
    println("   y colócalo en: data/netflix_titles.csv")
  }

  private def calculateSampleSizes(totalRecords: Int): List[Int] = {
    if (totalRecords <= 100) {
      List(totalRecords) // Usar todos los registros si son pocos
    } else {
      // Muestras progresivas para análisis de escalabilidad
      val sizes = List(100, 500, 1000, 5000).filter(_ <= totalRecords)
      if (sizes.isEmpty) List(100) else sizes
    }
  }

  private def ensureResultsDirectory(): Unit = {
    new File("results").mkdirs()
    new File("results/graficas").mkdirs()
  }

  private def printExecutiveSummary(results: List[ExperimentResult]): Unit = {
    val numericResults = results.filter(_.keyType == ExperimentRunner.NumericKeyType)
    val textResults = results.filter(_.keyType == ExperimentRunner.TextKeyType)

    if (numericResults.nonEmpty && textResults.nonEmpty) {
      val latestNumeric = numericResults.last
      val latestText = textResults.last

      println("CLAVES NUMÉRICAS:")
      println(s"   • Dataset: ${latestNumeric.datasetSize} registros")
      println(s"   • Inserción: ${latestNumeric.insertionTime / 1e6}%.2f ms")
      println(s"   • Búsqueda: ${latestNumeric.searchTime / 1e6}%.2f ms")
      println(s"   • Altura árbol: ${latestNumeric.treeHeight}")
      println(s"   • Colisiones: ${(latestNumeric.keyDistribution.collisionRate * 100).formatted("%.2f")}%")

      println("\nCLAVES TEXTUALES:")
      println(s"   • Dataset: ${latestText.datasetSize} registros")
      println(s"   • Inserción: ${latestText.insertionTime / 1e6}%.2f ms")
      println(s"   • Búsqueda: ${latestText.searchTime / 1e6}%.2f ms")
      println(s"   • Altura árbol: ${latestText.treeHeight}")
      println(s"   • Colisiones: ${(latestText.keyDistribution.collisionRate * 100).formatted("%.2f")}%")

      println("\nOBSERVACIONES:")
      if (latestNumeric.insertionTime < latestText.insertionTime) {
        println("   • Claves numéricas son más rápidas para inserción")
      } else {
        println("   • Claves textuales son más rápidas para inserción")
      }

      if (latestNumeric.treeHeight <= latestText.treeHeight) {
        println("   • Claves numéricas producen árboles más balanceados")
      } else {
        println("   • Claves textuales producen árboles más balanceados")
      }

      println("   • La inmutabilidad garantiza seguridad en operaciones")
    }
  }

  private def printGeneratedFiles(): Unit = {
    val graficasDir = new File("results/graficas")
    if (graficasDir.exists() && graficasDir.isDirectory) {
      val files = graficasDir.listFiles()
      if (files != null && files.nonEmpty) {
        println("\nARCHIVOS GENERADOS PARA GRÁFICAS:")
        files.foreach { file =>
          println(s"   • ${file.getName} (${file.length()} bytes)")
        }
      } else {
        println("\nNo se generaron archivos en results/graficas/")
        println("   Verifica que ExperimentRunner.generateChartData esté implementado")
      }
    } else {
      println("\nNo se pudo crear la carpeta results/graficas/")
    }
  }
}


object GraphicsGenerator {
  def generarGraficasSiEsPosible(): Unit = {
    println("\nINTENTANDO GENERAR GRÁFICAS AUTOMÁTICAMENTE...")

    try {
      val pythonScript = new File("results/graficas/generar_graficas_auto.py")
      if (pythonScript.exists()) {
        val process = Runtime.getRuntime().exec(Array("python", pythonScript.getAbsolutePath))
        val exitCode = process.waitFor()

        if (exitCode == 0) {
          println("Gráficas generadas automáticamente")
          println("Revisa: results/graficas/")
        } else {
          println("Las gráficas no se pudieron generar automáticamente")
          println("Ejecuta manualmente: python results/graficas/generar_graficas_auto.py")
        }
      } else {
        println("Script de gráficas no encontrado")
        println("Crea el archivo: results/graficas/generar_graficas_auto.py")
      }
    } catch {
      case e: Exception =>
        println(s"Error generando gráficas: ${e.getMessage}")
        println("Asegúrate de tener Python instalado")
    }
  }
}