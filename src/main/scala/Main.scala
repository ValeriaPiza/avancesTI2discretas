// src/main/scala/Main.scala

object Main {

  // Configuración para el dataset real
  val DefaultT: Int = 3
  val SampleSizes: List[Int] = List(100, 500, 1000) // Tamaños para experimentos

  def main(args: Array[String]): Unit = {
    println("🌳 B-TREE CON DATASET NETFLIX REAL")
    println("=============================================")

    // Crear directorios necesarios
    createDirectories()

    // 1. CARGAR DATASET REAL
    println("\n1. 🗂️  CARGANDO DATASET NETFLIX REAL...")
    val allRecords = NetflixDataLoader.loadCSV()

    if (allRecords.isEmpty) {
      showDatasetHelp()
      return
    }

    // 2. FILTRAR Y ANALIZAR
    println("\n2. 🔍 FILTRANDO Y ANALIZANDO DATOS...")
    val validRecords = NetflixDataLoader.filterValidRecords(allRecords)

    if (validRecords.isEmpty) {
      println("❌ No hay registros válidos después del filtrado")
      return
    }

    NetflixDataLoader.analyzeDataset(validRecords)
    NetflixDataLoader.showSampleRecords(validRecords, 2)

    // 3. COMPARAR GENERACIÓN DE CLAVES
    println("\n3. 🔑 ANALIZANDO GENERACIÓN DE CLAVES...")
    KeyGenerator.compareKeyTypes(validRecords)

    // 4. EJECUTAR EXPERIMENTOS
    println(s"\n4. 🧪 EJECUTANDO EXPERIMENTOS (t=$DefaultT)...")
    val experimentResults = ExperimentRunner.runNetflixExperiment(
      validRecords,
      t = DefaultT,
      sampleSizes = SampleSizes
    )

    // 5. GUARDAR RESULTADOS
    println(s"\n5. 💾 GUARDANDO RESULTADOS...")
    val timestamp = System.currentTimeMillis()
    val resultsFile = s"results/experiment_results_${timestamp}.csv"
    val reportFile = s"results/analysis_report_${timestamp}.txt"

    ExperimentRunner.saveResultsToCSV(experimentResults, resultsFile)

    // 6. GENERAR REPORTE FINAL
    val analysisReport = ExperimentRunner.generateAnalysisReport(experimentResults)
    println("\n" + analysisReport)

    saveToFile(analysisReport, reportFile)
    saveToFile(analysisReport, "doc/informe_analisis.txt")

    println("\n🎉 ANÁLISIS COMPLETADO EXITOSAMENTE!")
    println("=============================================")
    println("✅ 3.1 Claves numéricas: releaseYear + hash")
    println("✅ 3.2 Claves textuales: title → polynomial hash")
    println(s"📊 Resultados detallados: $resultsFile")
    println(s"📝 Reporte completo: $reportFile")
    println("=============================================")
  }

  private def createDirectories(): Unit = {
    new java.io.File("data").mkdirs()    // Para el CSV real
    new java.io.File("results").mkdirs() // Para resultados
    new java.io.File("doc").mkdirs()     // Para documentación
    println("✅ Directorios configurados")
  }

  private def showDatasetHelp(): Unit = {
    println("\n NO SE PUDO CARGAR EL DATASET")
    println("\nPARA CONFIGURAR CORRECTAMENTE:")
    println("1. 📥 Ve a: https://www.kaggle.com/datasets/shivamb/netflix-shows")
    println("2. ⬇️  Descarga 'netflix_titles.csv'")
    println("3. 📁 Crea una carpeta 'data/' en tu proyecto")
    println("4. 📄 Coloca el archivo en: data/netflix_titles.csv")
    println("\n📂 ESTRUCTURA REQUERIDA:")
    println("   tu_proyecto/")
    println("   ├── data/")
    println("   │   └── netflix_titles.csv  ")
    println("   ├── src/")
    println("   └── build.sbt")
  }

  private def saveToFile(content: String, filePath: String): Unit = {
    val writer = new java.io.PrintWriter(new java.io.File(filePath))
    try {
      writer.write(content)
      println(s"✅ Archivo guardado: $filePath")
    } finally {
      writer.close()
    }
  }
}