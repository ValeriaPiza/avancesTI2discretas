// src/test/scala/KeyGeneratorSpec.scala
import munit.FunSuite

class KeyGeneratorSpec extends FunSuite {

  val testRecord1: NetflixRecord = NetflixRecord(
    "s1",
    "The Matrix",
    "Movie",
    "Lana Wachowski",
    "Keanu Reeves",
    "USA",
    "2020-01-01",
    1999,
    "R",
    "136 min",
    "Action",
    "A computer hacker learns about the true nature of his reality."
  )

  val testRecord2: NetflixRecord = NetflixRecord(
    "s2",
    "The Matrix Reloaded",
    "Movie",
    "Lana Wachowski",
    "Keanu Reeves",
    "USA",
    "2020-01-01",
    2003,
    "R",
    "138 min",
    "Action",
    "Neo and the rebel leaders estimate they have 72 hours until Zion falls."
  )

  test("numeric key generation produces consistent results") {
    val key1 = KeyGenerator.generateNumericKey(testRecord1)
    val key2 = KeyGenerator.generateNumericKey(testRecord1)

    assertEquals(key1, key2, "Same record should produce same numeric key")
  }

  test("text key generation produces consistent results") {
    val key1 = KeyGenerator.generateTextKey(testRecord1)
    val key2 = KeyGenerator.generateTextKey(testRecord1)

    assertEquals(key1, key2, "Same record should produce same text key")
  }

  test("different records produce different numeric keys") {
    val key1 = KeyGenerator.generateNumericKey(testRecord1)
    val key2 = KeyGenerator.generateNumericKey(testRecord2)

    assert(key1 != key2, "Different records should produce different numeric keys")
  }

  test("different titles produce different text keys") {
    val key1 = KeyGenerator.generateTextKey(testRecord1)
    val key2 = KeyGenerator.generateTextKey(testRecord2)

    assert(key1 != key2, "Different titles should produce different text keys")
  }

  test("numeric keys are positive integers") {
    val key = KeyGenerator.generateNumericKey(testRecord1)
    assert(key > 0, "Numeric keys should be positive")
    assert(key <= Int.MaxValue, "Numeric keys should fit in Int range")
  }

  test("text keys are positive integers") {
    val key = KeyGenerator.generateTextKey(testRecord1)
    assert(key > 0, "Text keys should be positive")
    assert(key <= Int.MaxValue, "Text keys should fit in Int range")
  }

  test("key distribution analysis works correctly") {
    val keys = List(1, 2, 3, 4, 5, 1, 2) // Duplicados
    val stats = KeyGenerator.analyzeDistribution(keys)

    assertEquals(stats.totalKeys, 7)
    assertEquals(stats.uniqueKeys, 5)
    assert(stats.collisionRate > 0)
    assertEquals(stats.min, 1)
    assertEquals(stats.max, 5)
  }

  test("key distribution analysis handles empty list") {
    val stats = KeyGenerator.analyzeDistribution(List.empty)
    assertEquals(stats.totalKeys, 0)
    assertEquals(stats.uniqueKeys, 0)
    assertEquals(stats.collisionRate, 0.0)
  }

  test("key distribution analysis handles single element") {
    val stats = KeyGenerator.analyzeDistribution(List(42))
    assertEquals(stats.totalKeys, 1)
    assertEquals(stats.uniqueKeys, 1)
    assertEquals(stats.collisionRate, 0.0)
  }
}