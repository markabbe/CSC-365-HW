class DigitalTree {
    static class Node {
        long key;
        Node left, right;
        Node(long k) { key = k; }
    }
    Node root;
    public boolean contains(long key) {
        return contains(key, root, 0);
    }
    boolean contains(long key, Node t,  int level) {
        if (t == null)
            return false;
        else if (t.key == key)
            return true;
        else if ((key & (1L << level)) == 0)
            return contains(key, t.left, level+1);
        else
            return contains(key, t.right, level+1);
    }
}