case class Leaf(
                 override val keys: List[Int],
                 override val t: Int
               ) extends BTree {
  override val size: Int = keys.length
  override val height: Int = 1
  override val isLeaf: Boolean = true

  override def search(key: Int): Boolean = {
    keys.contains(key)
  }

  override def insert(key: Int): BTree = {
    val newKeys = (keys :+ key).sorted

    if (newKeys.length <= 2 * t - 1) {
      
      Leaf(newKeys, t)
    } else {
      
      val midIndex = t
      val midKey = newKeys(midIndex)
      val leftKeys = newKeys.take(midIndex)
      val rightKeys = newKeys.drop(midIndex + 1)

      println(s"SPLITTING LEAF: midKey=$midKey, leftKeys=$leftKeys, rightKeys=$rightKeys")

      
      InternalNode(
        keys = List(midKey),
        children = List(Leaf(leftKeys, t), Leaf(rightKeys, t)),
        t = t
      )
    }
  }

  override def printTree(level: Int = 0): Unit = {
    val indent = "  " * level
    println(s"${indent}Leaf(keys=$keys, size=$size)")
  }
}