trait BTree {
  val t: Int
  val keys: List[Int]
  val size: Int
  val height: Int
  def search(key: Int): Boolean
  def insert(key: Int): BTree
  def isLeaf: Boolean
  def printTree(level: Int = 0): Unit // Para debugging
}

object BTree {
  def empty(t: Int): BTree = Leaf(List.empty, t)

  def fromList(keys: List[Int], t: Int): BTree = {
    keys.foldLeft(empty(t))((tree, key) => tree.insert(key))
  }
}