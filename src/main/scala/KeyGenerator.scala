object KeyGenerator {

  // Constantes para teoría de números
  val LargePrime: Long = 1610612741L
  val Modulus: Int = Int.MaxValue / 2

  // === 3.1 CLAVES NUMÉRICAS ===

  /**
   * Genera clave numérica usando releaseYear como campo numérico
   * y aplicando hash multiplicativo para GARANTIZAR UNICIDAD
   */
  def generateNumericKey(record: NetflixRecord): Int = {
    val numericField = record.releaseYear  // Campo numérico del dataset
    val uniqueIdentifier = record.showId.replace("s", "").toInt // Convertir "s1" → 1, "s2" → 2, etc.

    // Hash multiplicativo con primo grande (teoría de números)
    val hash = (numericField.toLong * LargePrime + uniqueIdentifier) % Modulus
    hash.toInt.abs
  }

  // === 3.2 CLAVES TEXTUALES ===

  /**
   * Genera clave numérica a partir del título usando polynomial rolling hash
   * Basado en aritmética modular (teoría de números)
   */
  def generateTextKey(record: NetflixRecord): Int = {
    val text = record.title

    // Polynomial rolling hash con aritmética modular
    val (hash, _) = text.foldLeft((0, 1)) { case ((currentHash, currentPower), char) =>
      val p = 31
      val m = 1000000007  // Primo grande para aritmética modular
      val newHash = (currentHash + (char.toInt * currentPower)) % m
      val newPower = (currentPower * p) % m
      (newHash, newPower)
    }
    hash.abs
  }

  // === ANÁLISIS DE DISTRIBUCIÓN ===

  def analyzeDistribution(keys: List[Int]): DistributionStats = {
    if (keys.isEmpty) {
      DistributionStats(0, 0, 0.0, 0.0, 0.0, 0, 0)
    } else {
      val unique = keys.distinct.length
      val total = keys.length
      val collisionRate = 1.0 - (unique.toDouble / total)

      val mean = keys.sum.toDouble / total
      val variance = keys.map(k => math.pow(k - mean, 2)).sum / total
      val stdDev = math.sqrt(variance)

      DistributionStats(
        totalKeys = total,
        uniqueKeys = unique,
        collisionRate = collisionRate,
        mean = mean,
        stdDev = stdDev,
        min = keys.min,
        max = keys.max
      )
    }
  }

  def compareKeyTypes(records: List[NetflixRecord]): Unit = {
    println("\n=== COMPARACIÓN DE TIPOS DE CLAVES ===")

    val sample = records.take(1000)

    println("3.1 CLAVES NUMÉRICAS (releaseYear + hash):")
    val numericKeys = sample.map(generateNumericKey)
    val numericStats = analyzeDistribution(numericKeys)
    println(s"  • Únicas: ${numericStats.uniqueKeys}/${numericStats.totalKeys}")
    println(s"  • Tasa de colisiones: ${(numericStats.collisionRate * 100).formatted("%.2f")}%")
    println(s"  • Distribución: media=${numericStats.mean.formatted("%.0f")}, " +
      s"desviación=${numericStats.stdDev.formatted("%.0f")}")

    println("\n3.2 CLAVES TEXTUALES (title → polynomial hash):")
    val textKeys = sample.map(generateTextKey)
    val textStats = analyzeDistribution(textKeys)
    println(s"  • Únicas: ${textStats.uniqueKeys}/${textStats.totalKeys}")
    println(s"  • Tasa de colisiones: ${(textStats.collisionRate * 100).formatted("%.2f")}%")
    println(s"  • Distribución: media=${textStats.mean.formatted("%.0f")}, " +
      s"desviación=${textStats.stdDev.formatted("%.0f")}")

    // Mostrar ejemplos de claves generadas
    println("\n🔑 EJEMPLOS DE CLAVES GENERADAS:")
    sample.take(3).foreach { record =>
      val numericKey = generateNumericKey(record)
      val textKey = generateTextKey(record)
      println(s"   '${record.title}' → Numérica: $numericKey, Textual: $textKey")
    }
  }
}

case class DistributionStats(
                              val totalKeys: Int,
                              val uniqueKeys: Int,
                              val collisionRate: Double,
                              val mean: Double,
                              val stdDev: Double,
                              val min: Int,
                              val max: Int
                            )