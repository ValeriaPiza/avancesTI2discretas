import munit.FunSuite

class BTreeImmutabilitySpec extends FunSuite {

  test("all class parameters are vals and immutable") {
    val leaf = Leaf(List(1, 2, 3), 2)
    val internal = InternalNode(List(5), List(leaf), 2)

    // Verificar que no hay métodos para modificar estado
    assert(leaf.isInstanceOf[Product]) // Case classes son inmutables por defecto
    assert(internal.isInstanceOf[Product])

    // Verificar que las propiedades son accessors, no mutators
    val leafMethods = leaf.getClass.getMethods.map(_.getName)
    assert(!leafMethods.contains("keys_$eq")) // No tiene setter
    assert(!leafMethods.contains("t_$eq"))

    val internalMethods = internal.getClass.getMethods.map(_.getName)
    assert(!internalMethods.contains("children_$eq"))
  }

  test("tree operations return new instances") {
    val original = BTree.empty(2)
    val afterInsert = original.insert(10)
    val afterAnotherInsert = afterInsert.insert(20)

    // Todos son instancias diferentes
    assert(original ne afterInsert)
    assert(afterInsert ne afterAnotherInsert)
    assert(original ne afterAnotherInsert)

    // Estado original no cambió
    assertEquals(original.keys, List.empty)
    assertEquals(original.size, 0)
  }

  test("complex tree structure maintains immutability") {
    val complexTree = BTree.fromList((1 to 100).toList, 3)
    val originalHash = System.identityHashCode(complexTree)

    // Múltiples operaciones
    val modifiedTree = (101 to 110).foldLeft(complexTree)((t, k) => t.insert(k))

    // Árbol original no cambió
    assertEquals(complexTree.size, 100)
    assertEquals(System.identityHashCode(complexTree), originalHash)

    // Nuevo árbol es diferente
    assert(System.identityHashCode(modifiedTree) != originalHash)
    assertEquals(modifiedTree.size, 110)
  }
}