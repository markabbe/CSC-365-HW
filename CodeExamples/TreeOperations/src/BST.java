import java.util.NoSuchElementException;

class BST {
    Node root;

    void printAll(){
        printAll(root);
    }

    //In order traversal
    void printAll(Node t){
        if(t == null){
            return;
        }
        printAll(t.left);
        System.out.println(t.key);
        printAll(t.right);
    }

    //Return lowest numerical key
    float getLowest(){
        if(root == null){
            throw new NoSuchElementException();
        }

        Node t = root;

        while(t.left != null){
            t = t.left;
        }

        return t.key;
    }

    private Node successor(Node t){
        if(t == null){
            return null;
        }

        if(t.right != null){
            t = t.right;
            while(t.left != null){
                t = t.left;
            }
            return t;
        }
        else{
            Node parent = t.parent;
            while(parent != null && t != parent.left){
                t = parent;
                parent = parent.parent;
            }
            return parent;
        }
    }

    //Node class
    static class Node {
        float key;

        Node left, right, parent;

        boolean isRed;
    }
}