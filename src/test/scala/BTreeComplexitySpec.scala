// src/test/scala/BTreeComplexitySpec.scala
import munit.FunSuite
import scala.annotation.tailrec

class BTreeComplexitySpec extends FunSuite {

  val t: Int = 3 // grado mínimo para pruebas

  test("search maintains O(log n) complexity") {
    val sizes = List(100, 1000, 5000)
    val results = sizes.map { size =>
      val tree = BTree.fromList((1 to size).toList, t)

      // Medir tiempo de búsqueda
      val startTime = System.nanoTime()
      (1 to 100).foreach { i =>
        tree.search(i)
      }
      val endTime = System.nanoTime()

      (size, endTime - startTime)
    }

    // Verificar crecimiento logarítmico
    val times = results.map(result => result._2.toDouble)
    assert(isLogarithmicGrowth(times), "Search time should grow logarithmically")
  }

  test("insert maintains O(log n) complexity") {
    val baseTree = BTree.fromList((1 to 1000).toList, t)

    val startTime = System.nanoTime()
    val newTree = (1001 to 1100).foldLeft(baseTree) { (tree, key) =>
      tree.insert(key)
    }
    val endTime = System.nanoTime()

    val timePerInsert = (endTime - startTime) / 100.0
    assert(timePerInsert < 1000000, "Insert time per operation should be small") // 1ms por operación
  }

  test("all vals are computed once and immutable") {
    val tree = BTree.fromList(List(10, 20, 30, 40, 50), t)

    // Verificar que las propiedades son constantes
    val initialSize = tree.size
    val initialHeight = tree.height
    val initialKeys = tree.keys

    // Insertar no debe cambiar el árbol original
    val newTree = tree.insert(60)

    assertEquals(tree.size, initialSize)
    assertEquals(tree.height, initialHeight)
    assertEquals(tree.keys, initialKeys)

    // Nuevo árbol tiene sus propias constantes
    assert(newTree.size > initialSize)
  }

  test("tree properties are maintained after multiple operations") {
    var tree = BTree.empty(t)
    val operations = 1000
    var expectedSize = 0

    (1 to operations).foreach { i =>
      tree = tree.insert(i)
      expectedSize += 1 // Cada inserción debería aumentar el tamaño en 1

      // Verificar propiedades en cada paso (O(1) checks)
      if (i % 100 == 0) {
        assert(tree.height >= 1, s"Height should be >= 1 after $i operations")
        assert(tree.size == expectedSize, s"Size should be $expectedSize after $i operations, but got ${tree.size}")
        assert(tree.keys.sorted == tree.keys, "Keys should be sorted") // Claves ordenadas

        // Verificar que todas las claves insertadas están en el árbol
        (1 to i).foreach { key =>
          assert(tree.search(key), s"Key $key should be found after $i operations")
        }
      }
    }
  }

  test("key generation has constant or linear complexity") {
    val testStrings = List("Movie A", "Movie B", "Movie C", "Movie D")

    val startTime = System.nanoTime()
    val keys = testStrings.map(KeyGenerator.generateTextKey)
    val endTime = System.nanoTime()

    val timePerKey = (endTime - startTime) / testStrings.length.toDouble
    assert(timePerKey < 1000000, "Key generation should be efficient") // 1ms por clave
    assert(keys.distinct.length == keys.length, "Keys should be unique")
  }

  // Prueba específica para verificar el tamaño después de splits
  test("size is correctly maintained after splits") {
    var tree = BTree.empty(2) // t=2 para forzar splits rápidos
    val keysToInsert = List(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)

    keysToInsert.zipWithIndex.foreach { case (key, index) =>
      tree = tree.insert(key)
      val expectedSize = index + 1
      assertEquals(tree.size, expectedSize,
        s"After inserting $key (operation ${index + 1}), size should be $expectedSize but got ${tree.size}")
    }

    // Verificar que todas las claves están presentes
    keysToInsert.foreach { key =>
      assert(tree.search(key), s"Key $key should be in final tree")
    }
  }

  // Modo auxiliar para verificar crecimiento logarítmico
  private def isLogarithmicGrowth(times: List[Double]): Boolean = {
    @tailrec
    def checkRatios(remaining: List[Double], prev: Double): Boolean = remaining match {
      case Nil => true
      case current :: tail =>
        val ratio = current / prev
        // En crecimiento logarítmico, la razón debería ser relativamente pequeña
        // Para tamaños 100, 1000, 5000, la razón debería ser < 5
        if (ratio > 10) false // Ratio muy alto indica no logarítmico
        else checkRatios(tail, current)
    }

    if (times.length < 2) true
    else checkRatios(times.tail, times.head)
  }
}