// src/main/scala/NetflixDataLoader.scala
import scala.io.Source
import scala.util.{Try, Using}

case class NetflixRecord(
                          val showId: String,
                          val title: String,
                          val `type`: String,
                          val director: String,
                          val cast: String,
                          val country: String,
                          val dateAdded: String,
                          val releaseYear: Int,
                          val rating: String,
                          val duration: String,
                          val listedIn: String,
                          val description: String
                        )

object NetflixDataLoader {

  // Ruta relativa al archivo CSV real
  val DefaultCSVPath: String = "data/netflix_titles.csv"

  /**
   * Carga el dataset real de Netflix desde el archivo CSV
   */
  def loadCSV(filePath: String = DefaultCSVPath): List[NetflixRecord] = {
    println("🎬 CARGANDO DATASET NETFLIX...")
    println(s"📂 Ruta: $filePath")

    Using(Source.fromFile(filePath, "UTF-8")) { source =>
      val lines = source.getLines().toList

      if (lines.isEmpty) {
        println("❌ El archivo CSV está vacío")
        List.empty[NetflixRecord]
      } else {
        val header = lines.head
        val dataLines = lines.tail

        println(s"✅ Archivo encontrado: ${dataLines.length} registros")

        // Procesar las líneas de datos
        val (successfulRecords, failedRecords) = dataLines.zipWithIndex
          .map { case (line, index) =>
            parseCSVLine(line).fold {
              if (index < 3) println(s"⚠️  Línea ${index + 2} con error: ${line.take(80)}...")
              None
            }(Some(_))
          }
          .partition(_.isDefined)

        val records = successfulRecords.flatten

        if (failedRecords.count(_.isEmpty) > 0) {
          println(s"⚠️  ${failedRecords.count(_.isEmpty)} registros no pudieron ser parseados")
        }

        println(s"📊 Registros cargados exitosamente: ${records.size}")
        records
      }
    }.getOrElse {
      println(s"❌ ERROR CRÍTICO: No se pudo leer el archivo: $filePath")
      println("   Por favor verifica:")
      println("   1. 📁 Que la carpeta 'data/' existe en la raíz del proyecto")
      println("   2. 📄 Que el archivo 'netflix_titles.csv' está en la carpeta 'data/'")
      println("   3. 🔄 Que el archivo no está corrupto")
      println("   4. 📥 Que descargaste el dataset de: https://www.kaggle.com/datasets/shivamb/netflix-shows")
      List.empty[NetflixRecord]
    }
  }

  /**
   * Parsea una línea del CSV real en un NetflixRecord
   */
  def parseCSVLine(line: String): Option[NetflixRecord] = {
    Try {
      val fields = splitCSV(line)

      if (fields.length >= 12) {
        // Limpiar y normalizar campos del dataset real
        val cleanFields = fields.map { field =>
          if (field == null || field.isEmpty || field == "null") ""
          else field.trim.replaceAll("^\"|\"$", "")
        }

        NetflixRecord(
          showId = cleanFields(0),
          title = cleanFields(1),
          `type` = cleanFields(2),
          director = cleanFields(3),
          cast = cleanFields(4),
          country = cleanFields(5),
          dateAdded = cleanFields(6),
          releaseYear = Try(cleanFields(7).toInt).getOrElse(0),
          rating = cleanFields(8),
          duration = cleanFields(9),
          listedIn = cleanFields(10),
          description = cleanFields(11)
        )
      } else {
        throw new IllegalArgumentException(s"Línea CSV incompleta: ${fields.length} campos")
      }
    }.recover {
        case e: Exception =>
          // println(s"Error parseando línea: ${e.getMessage}")
          null
      }.toOption
      .flatMap(Option(_)) // Convertir null a None
  }

  /**
   * Divide una línea CSV manejando comas dentro de comillas
   */
  // En NetflixDataLoader.scala - corregir el metodo splitCSV
  def splitCSV(line: String): Array[String] = {
    // Manejo mejorado de CSV - dividir por comas fuera de comillas
    line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)
      .map { field =>
        // Remover comillas al inicio y final completamente
        val cleaned = field.trim
          .replaceAll("^\"\"", "\"") // Manejar comillas dobles al inicio
          .replaceAll("^\"", "")     // Remover comilla inicial
          .replaceAll("\"$", "")     // Remover comilla final
          .replaceAll("\"\"", "\"")  // Manejar comillas escapadas
          .trim

        if (cleaned.isEmpty) "" else cleaned
      }
  }

  /**
   * Filtra registros válidos para el análisis
   */
  def filterValidRecords(records: List[NetflixRecord]): List[NetflixRecord] = {
    val validRecords = records.filter { record =>
      record.showId.nonEmpty &&
        record.showId.startsWith("s") &&
        record.title.nonEmpty &&
        record.releaseYear >= 1920 &&
        record.releaseYear <= 2025 &&
        record.`type`.nonEmpty
    }

    val invalidCount = records.size - validRecords.size
    if (invalidCount > 0) {
      println(s"🔍 Filtrados $invalidCount registros inválidos")
    }

    println(s"✅ Registros válidos para análisis: ${validRecords.size}")
    validRecords
  }

  /**
   * Analiza y muestra estadísticas del dataset cargado
   */
  def analyzeDataset(records: List[NetflixRecord]): Unit = {
    if (records.isEmpty) {
      println("❌ No hay registros para analizar")
      return
    }

    println("\n" + "="*50)
    println("📊 ANÁLISIS COMPLETO DEL DATASET NETFLIX")
    println("="*50)

    println(s"📈 Total de registros: ${records.length}")

    val movies = records.count(_.`type` == "Movie")
    val tvShows = records.count(_.`type` == "TV Show")
    println(s"🎬 Películas: $movies (${(movies.toDouble/records.length*100).formatted("%.1f")}%)")
    println(s"📺 Series TV: $tvShows (${(tvShows.toDouble/records.length*100).formatted("%.1f")}%)")

    val years = records.map(_.releaseYear)
    println(s"📅 Rango de años: ${years.min} - ${years.max}")

    // Años más comunes
    val yearDistribution = records.groupBy(_.releaseYear)
      .map { case (year, items) => (year, items.size) }
      .toList
      .sortBy(-_._2)
      .take(5)

    println("🏆 Años con más contenido: " +
      yearDistribution.map { case (year, count) => s"$year($count)" }.mkString(", "))

    // Países principales
    val countries = records.flatMap(_.country.split(",").map(_.trim))
      .filter(_.nonEmpty)
      .groupBy(identity)
      .map { case (country, list) => (country, list.size) }
      .toList
      .sortBy(-_._2)
      .take(5)

    println("🌍 Países principales: " +
      countries.map { case (country, count) => s"$country($count)" }.mkString(", "))

    // Ratings más comunes
    val ratings = records.groupBy(_.rating)
      .map { case (rating, items) => (rating, items.size) }
      .toList
      .sortBy(-_._2)
      .take(5)

    println("📋 Ratings principales: " +
      ratings.map { case (rating, count) => s"$rating($count)" }.mkString(", "))

    // Ejemplos representativos
    println("\n🎭 EJEMPLOS DE CONTENIDO:")
    val sampleMovies = records.filter(_.`type` == "Movie").take(2)
    val sampleTVShows = records.filter(_.`type` == "TV Show").take(2)

    println("   🎬 Películas:")
    sampleMovies.foreach { record =>
      println(s"     • ${record.title} (${record.releaseYear}) - ${record.duration}")
    }

    println("   📺 Series TV:")
    sampleTVShows.foreach { record =>
      println(s"     • ${record.title} (${record.releaseYear}) - ${record.duration}")
    }

    println("="*50)
  }

  /**
   * Toma una muestra aleatoria del dataset
   */
  def sampleRecords(records: List[NetflixRecord], sampleSize: Int): List[NetflixRecord] = {
    val actualSize = math.min(sampleSize, records.length)
    val sample = scala.util.Random.shuffle(records).take(actualSize)
    println(s"🔬 Muestra aleatoria tomada: $actualSize registros")
    sample
  }

  /**
   * Muestra ejemplos detallados del dataset (para debugging)
   */
  def showSampleRecords(records: List[NetflixRecord], count: Int = 3): Unit = {
    println(s"\n🔍 MUESTRA DETALLADA (primeros $count registros):")
    println("-" * 60)

    records.take(count).foreach { record =>
      println(s"🎯 ID: ${record.showId}")
      println(s"   📝 Título: ${record.title}")
      println(s"   🏷️  Tipo: ${record.`type`}")
      println(s"   📅 Año: ${record.releaseYear}")
      println(s"   ⏱️  Duración: ${record.duration}")
      println(s"   🌟 Rating: ${if (record.rating.isEmpty) "N/A" else record.rating}")
      println(s"   🎬 Director: ${if (record.director.isEmpty) "N/A" else record.director.take(40)}")
      println(s"   🌍 País: ${if (record.country.isEmpty) "N/A" else record.country.take(30)}")
      println(s"   📚 Categorías: ${if (record.listedIn.isEmpty) "N/A" else record.listedIn.take(50)}")
      println("   " + "─" * 40)
    }
  }
}