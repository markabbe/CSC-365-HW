public class Main {
}

class BST {
    Node root;
    void rotateRight(Node x, Node y, Node z){
        Node b = y.left;
        Node p = x.parent;
        y.left = x;
        x.parent = y;
        x.right = b;
        if(b != null){
            b.parent = x;
        }
        y.parent = p;
        if(p == null){
            root = y;
        }
        else if(x == p.left){
            p.left = y;
        }
        else{
            p.right = y;
        }
    }
    static class Node {
        float key;

        Node left, right, parent;

        boolean isRed;
    }
}