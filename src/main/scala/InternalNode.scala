case class InternalNode(
                         override val keys: List[Int],
                         children: List[BTree],
                         override val t: Int
                       ) extends BTree {
  override val size: Int = keys.length + children.map(_.size).sum
  override val height: Int = 1 + children.map(_.height).max
  override val isLeaf: Boolean = false

  override def search(key: Int): Boolean = {
    if (keys.contains(key)) {
      true
    } else {
      val index = keys.indexWhere(k => key < k)
      val childIndex = if (index == -1) children.length - 1 else index
      children(childIndex).search(key)
    }
  }

  override def insert(key: Int): BTree = {
    val index = keys.indexWhere(k => key < k)
    val childIndex = if (index == -1) children.length - 1 else index

    val child = children(childIndex)

    if (child.keys.length == 2 * t - 1) {
      splitChild(childIndex, key)
    } else {
      val updatedChild = child.insert(key)
      this.copy(children = children.updated(childIndex, updatedChild))
    }
  }

  private def splitChild(childIndex: Int, key: Int): InternalNode = {
    val child = children(childIndex)

    child match {
      case Leaf(childKeys, _) =>
        val allKeys = (childKeys :+ key).sorted

        val midIndex = t
        val midKey = allKeys(midIndex)
        val leftKeys = allKeys.take(midIndex)
        val rightKeys = allKeys.drop(midIndex + 1)

        println(s"SPLITTING CHILD LEAF: midKey=$midKey, leftKeys=$leftKeys, rightKeys=$rightKeys")

        val (leftKeysParent, rightKeysParent) = keys.splitAt(childIndex)
        val newKeys = (leftKeysParent :+ midKey) ++ rightKeysParent

        val (leftChildren, rightChildren) = children.splitAt(childIndex)
        val newChildren = leftChildren ++
          List(Leaf(leftKeys, t), Leaf(rightKeys, t)) ++
          rightChildren.tail

        InternalNode(newKeys, newChildren, t)

      case InternalNode(childKeys, childChildren, _) =>
        val allKeys = (childKeys :+ key).sorted

        val midIndex = t
        val midKey = allKeys(midIndex)
        val leftKeys = allKeys.take(midIndex)
        val rightKeys = allKeys.drop(midIndex + 1)

        val (leftKeysParent, rightKeysParent) = keys.splitAt(childIndex)
        val newKeys = (leftKeysParent :+ midKey) ++ rightKeysParent

        val (leftChildren, rightChildren) = children.splitAt(childIndex)
        val newChildren = leftChildren ++
          List(InternalNode(leftKeys, childChildren.take(t + 1), t),
            InternalNode(rightKeys, childChildren.drop(t + 1), t)) ++
          rightChildren.tail

        InternalNode(newKeys, newChildren, t)
    }
  }

  override def printTree(level: Int = 0): Unit = {
    val indent = "  " * level
    println(s"${indent}InternalNode(keys=$keys, size=$size)")
    children.foreach(_.printTree(level + 1))
  }
}