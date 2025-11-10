// src/test/scala/NetflixDataLoaderSpec.scala
import munit.FunSuite
import java.nio.file.Files
import java.io.PrintWriter

class NetflixDataLoaderSpec extends FunSuite {

  test("loadCSV returns empty list for non-existent file") {
    val records = NetflixDataLoader.loadCSV("non_existent_file.csv")
    assertEquals(records, List.empty[NetflixRecord])
  }

  test("load CSV with valid data") {
    // Crear archivo CSV de prueba temporal
    val testData = """show_id,type,title,director,cast,country,date_added,release_year,rating,duration,listed_in,description
                     |s1,Movie,Test Movie,Test Director,Test Cast,USA,January 1, 2020,PG-13,90 min,Drama,Test Description""".stripMargin

    val tempFile = Files.createTempFile("test_netflix", ".csv").toFile
    val writer = new PrintWriter(tempFile)
    try {
      writer.write(testData)
    } finally {
      writer.close()
    }

    val records = NetflixDataLoader.loadCSV(tempFile.getAbsolutePath)

    assert(records.nonEmpty, "Should load at least one record")
    assertEquals(records.head.title, "Test Movie")
    assertEquals(records.head.showId, "s1")
    assertEquals(records.head.releaseYear, 2020)

    tempFile.delete()
  }

  test("parseCSVLine handles valid CSV line") {
    val validLine = "s1,Movie,Movie Title,Director,Cast,Country,2020-01-01,2020,PG-13,90 min,Drama,Description"
    val record = NetflixDataLoader.parseCSVLine(validLine)

    assert(record.isDefined)
    assertEquals(record.get.showId, "s1")
    assertEquals(record.get.title, "Movie Title")
    assertEquals(record.get.releaseYear, 2020)
    assertEquals(record.get.duration, "90 min")
  }

  test("parseCSVLine returns None for invalid CSV line") {
    val invalidLine = "s1,Movie Title" // Campos insuficientes
    val record = NetflixDataLoader.parseCSVLine(invalidLine)

    assert(record.isEmpty)
  }

  test("parseCSVLine handles empty fields") {
    val lineWithEmptyFields = "s1,Movie Title,Movie,,,USA,,2020,,90 min,,"
    val record = NetflixDataLoader.parseCSVLine(lineWithEmptyFields)

    assert(record.isDefined)
    assertEquals(record.get.director, "")
    assertEquals(record.get.cast, "")
    assertEquals(record.get.rating, "")
  }

  test("splitCSV handles basic CSV") {
    val line = "field1,field2,field3"
    val fields = NetflixDataLoader.splitCSV(line)
    assertEquals(fields.toList, List("field1", "field2", "field3"))
  }

  test("splitCSV handles quoted fields") {
    val line = """field1,"field,with,commas",field3"""
    val fields = NetflixDataLoader.splitCSV(line)
    assertEquals(fields.toList, List("field1", "field,with,commas", "field3"))
  }

  test("splitCSV handles empty fields") {
    val line = "field1,,field3"
    val fields = NetflixDataLoader.splitCSV(line)
    assertEquals(fields.toList, List("field1", "", "field3"))
  }

  test("filterValidRecords keeps only valid records") {
    val validRecord = NetflixRecord("s1", "Valid Movie", "Movie", "", "", "", "", 2020, "", "", "", "")
    val invalidRecord1 = NetflixRecord("", "Invalid ShowId", "Movie", "", "", "", "", 2020, "", "", "", "")
    val invalidRecord2 = NetflixRecord("s2", "Invalid Year", "Movie", "", "", "", "", 1800, "", "", "", "")

    val filtered = NetflixDataLoader.filterValidRecords(List(validRecord, invalidRecord1, invalidRecord2))

    assertEquals(filtered.length, 1)
    assertEquals(filtered.head.showId, "s1")
    assertEquals(filtered.head.title, "Valid Movie")
  }

  test("sampleRecords returns correct sample size") {
    val records = (1 to 10).map(i =>
      NetflixRecord(s"s$i", s"Movie $i", "Movie", "", "", "", "", 2020, "", "", "", "")
    ).toList

    val sample = NetflixDataLoader.sampleRecords(records, 5)
    assertEquals(sample.length, 5)
  }

  test("analyzeDataset works with empty list") {
    // No debería lanzar excepción
    NetflixDataLoader.analyzeDataset(List.empty)
  }

  test("analyzeDataset works with valid records") {
    val records = List(
      NetflixRecord("s1", "Movie 1", "Movie", "", "", "", "", 2020, "", "", "", ""),
      NetflixRecord("s2", "Movie 2", "Movie", "", "", "", "", 2021, "", "", "", ""),
      NetflixRecord("s3", "TV Show 1", "TV Show", "", "", "", "", 2022, "", "", "", "")
    )

    // No debería lanzar excepción
    NetflixDataLoader.analyzeDataset(records)
  }

    test("splitCSV method handles quoted fields correctly") {
      // Probar el metodo splitCSV directamente
      val testLine1 = """s1,Movie,"Movie Title",Director A,"Actor1, Actor2",Country A"""
      val result1 = NetflixDataLoader.splitCSV(testLine1)

      assertEquals(result1.length, 6)
      assertEquals(result1(0), "s1")
      assertEquals(result1(1), "Movie")
      assertEquals(result1(2), "Movie Title") // ✅ Esto era el problema
      assertEquals(result1(3), "Director A")
      assertEquals(result1(4), "Actor1, Actor2") // También con coma interna

      // Probar sin comillas
      val testLine2 = "s2,Movie,Simple Movie,Director B,Actor3,Country B"
      val result2 = NetflixDataLoader.splitCSV(testLine2)
      assertEquals(result2(2), "Simple Movie")

      // Probar campo vacío
      val testLine3 = "s3,Movie,Another Movie,,Actor4,Country C"
      val result3 = NetflixDataLoader.splitCSV(testLine3)
      assertEquals(result3(3), "") // Campo director vacío
    }

    test("parseCSVLine handles real Netflix data format") {
      // Simular una línea real del dataset Netflix
      val realLine = """s1,Movie,Movie Title,Director Name,"Actor1, Actor2, Actor3","United States, Canada","September 1, 2020",2020,PG-13,90 min,"Dramas, Romantic Movies","A description of the movie"""

      val record = NetflixDataLoader.parseCSVLine(realLine)

      assert(record.isDefined)
      assertEquals(record.get.showId, "s1")
      assertEquals(record.get.title, "Movie Title") // ✅ El problema principal
      assertEquals(record.get.`type`, "Movie")
      assertEquals(record.get.director, "Director Name")
      assertEquals(record.get.cast, "Actor1, Actor2, Actor3")
      assertEquals(record.get.country, "United States, Canada")
      assertEquals(record.get.releaseYear, 2020)
      assertEquals(record.get.rating, "PG-13")
      assertEquals(record.get.duration, "90 min")
      assertEquals(record.get.listedIn, "Dramas, Romantic Movies")
    }

    private def createTestCSV(content: String): String = {
      val tempFile = Files.createTempFile("netflix-test", ".csv")
      Files.write(tempFile, content.getBytes("UTF-8"))
      tempFile.toString
    }
  
}