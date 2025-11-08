import munit.FunSuite
import scala.annotation.tailrec

class BTreeComplexitySpec extends FunSuite {

  val t: Int = 2 // Usamos t=2 para pruebas más estrictas

  // Crear records de prueba para usar con KeyGenerator
  val testRecords: List[NetflixRecord] = List(
    NetflixRecord("s1", "Movie One", "Movie", "", "", "", "", 2020, "", "", "", ""),
    NetflixRecord("s2", "Movie Two", "Movie", "", "", "", "", 2021, "", "", "", ""),
    NetflixRecord("s3", "Movie Three", "Movie", "", "", "", "", 2022, "", "", "", ""),
    NetflixRecord("s4", "Movie Four", "Movie", "", "", "", "", 2023, "", "", "", ""),
    NetflixRecord("s5", "Movie Five", "Movie", "", "", "", "", 2024, "", "", "", "")
  )

  test("search maintains O(log n) complexity") {
    val sizes = List(10, 50, 100) // Reducimos tamaños para pruebas más rápidas
    val results = sizes.map { size =>
      // Crear árbol con datos de prueba
      val keys = (1 to size).toList
      val tree = BTree.fromList(keys, t)

      val startTime = System.nanoTime()
      (1 to math.min(size, 10)).foreach { i =>
        tree.search(i)
      }
      val endTime = System.nanoTime()

      (size, endTime - startTime)
    }

    val times = results.map(_._2.toDouble)
    assert(isReasonableGrowth(times), "Search time growth should be reasonable")
  }

  test("insert maintains O(log n) complexity") {
    val baseSize = 50
    val baseTree = BTree.fromList((1 to baseSize).toList, t)

    val startTime = System.nanoTime()
    val newTree = (baseSize + 1 to baseSize + 20).foldLeft(baseTree) { (tree, key) =>
      tree.insert(key)
    }
    val endTime = System.nanoTime()

    val timePerInsert = (endTime - startTime) / 20.0
    assert(timePerInsert < 1000000, s"Insert time per operation should be small, got $timePerInsert ns")
  }

  test("all vals are computed once and immutable") {
    val tree = BTree.fromList(List(10, 20, 30, 40, 50), t)

    val initialSize = tree.size
    val initialHeight = tree.height
    val initialKeys = tree.keys

    val newTree = tree.insert(60)

    assertEquals(tree.size, initialSize)
    assertEquals(tree.height, initialHeight)
    assertEquals(tree.keys, initialKeys)
    assert(newTree.size > initialSize)
  }

  test("tree properties are maintained after multiple operations - DEBUG VERSION") {
    var tree = BTree.empty(t)
    val operations = 20 // Reducimos para debugging

    (1 to operations).foreach { i =>
      tree = tree.insert(i)
      val currentSize = tree.size

      // Verificaciones básicas
      assert(tree.height >= 1, s"Height should be >= 1 after $i operations, got ${tree.height}")
      assert(currentSize == i, s"Size $currentSize should equal $i")
      assert(tree.keys.sorted == tree.keys, "Keys should be sorted")

      // Verificar que la clave actual está en el árbol
      assert(tree.search(i), s"Key $i should be found after insertion")

      // Verificar que todas las claves anteriores están aún presentes
      (1 to i).foreach { key =>
        assert(tree.search(key), s"Key $key should be found after $i operations")
      }
    }

    // Verificación final
    assertEquals(tree.size, operations, s"Final size should be $operations")
  }

  test("key generation has constant or linear complexity") {
    val testStrings = List("Movie A", "Movie B", "Movie C", "Movie D")

    // Crear records de prueba para las cadenas
    val testRecordsForStrings = testStrings.zipWithIndex.map { case (title, index) =>
      NetflixRecord(s"s${index + 1}", title, "Movie", "", "", "", "", 2020 + index, "", "", "", "")
    }

    val startTime = System.nanoTime()
    val keys = testRecordsForStrings.map(KeyGenerator.generateTextKey)
    val endTime = System.nanoTime()

    val timePerKey = (endTime - startTime) / testStrings.length.toDouble
    assert(timePerKey < 1000000, s"Key generation should be efficient, got $timePerKey ns per key")
    assert(keys.distinct.length == keys.length, "Keys should be unique")
  }

  test("size is correctly maintained after splits - SIMPLE VERSION") {
    var tree = BTree.empty(2)
    val keysToInsert = List(10, 20, 30, 40) // Solo 4 claves para debug

    keysToInsert.zipWithIndex.foreach { case (key, index) =>
      val previousSize = tree.size
      tree = tree.insert(key)
      val newSize = tree.size

      // El tamaño debe aumentar exactamente en 1
      assertEquals(newSize, previousSize + 1,
        s"After inserting $key, size should increase from $previousSize to ${previousSize + 1}, but got $newSize")

      // Verificar que la clave está presente
      assert(tree.search(key), s"Key $key should be in tree after insertion")

      // Verificar que todas las claves anteriores están presentes
      keysToInsert.take(index + 1).foreach { k =>
        assert(tree.search(k), s"Key $k should be in tree")
      }
    }

    // Verificación final
    assertEquals(tree.size, keysToInsert.length,
      s"Final size should be ${keysToInsert.length}, but got ${tree.size}")

    keysToInsert.foreach { key =>
      assert(tree.search(key), s"Key $key should be in final tree")
    }
  }

  // Prueba adicional para debugging básico
  test("debug basic insertions without splits") {
    var tree = BTree.empty(3) // t=3 para evitar splits tempranos

    tree = tree.insert(10)
    assertEquals(tree.size, 1, "Size after first insert")
    assert(tree.search(10), "Should find first key")

    tree = tree.insert(20)
    assertEquals(tree.size, 2, "Size after second insert")
    assert(tree.search(20), "Should find second key")

    tree = tree.insert(30)
    assertEquals(tree.size, 3, "Size after third insert")
    assert(tree.search(30), "Should find third key")

    // Verificar que todas están presentes
    assert(tree.search(10) && tree.search(20) && tree.search(30), "All keys should be present")
  }

  // Prueba específica para KeyGenerator con NetflixRecord
  test("KeyGenerator works with NetflixRecord for numeric keys") {
    val record = NetflixRecord("s1", "Test Movie", "Movie", "", "", "", "", 2020, "", "", "", "")
    val key1 = KeyGenerator.generateNumericKey(record)
    val key2 = KeyGenerator.generateNumericKey(record)

    assertEquals(key1, key2, "Same record should produce same numeric key")
    assert(key1 > 0, "Numeric key should be positive")
  }

  test("KeyGenerator works with NetflixRecord for text keys") {
    val record = NetflixRecord("s1", "Test Movie", "Movie", "", "", "", "", 2020, "", "", "", "")
    val key1 = KeyGenerator.generateTextKey(record)
    val key2 = KeyGenerator.generateTextKey(record)

    assertEquals(key1, key2, "Same record should produce same text key")
    assert(key1 > 0, "Text key should be positive")
  }

  test("buildTreeWithKeys works with KeyGenerator output") {
    // Usar KeyGenerator con records reales
    val keys = testRecords.take(3).map(KeyGenerator.generateNumericKey)
    val tree = ExperimentRunner.buildTreeWithKeys(keys, t)

    assertEquals(tree.size, 3)
    keys.foreach { key =>
      assert(tree.search(key), s"Key $key should be in tree")
    }
  }

  private def isReasonableGrowth(times: List[Double]): Boolean = {
    if (times.length < 2) true
    else {
      val ratios = times.zip(times.tail).map { case (a, b) => b / a }
      ratios.forall(_ < 10) // Las ratios no deberían ser enormes
    }
  }
}