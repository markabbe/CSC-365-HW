import java.util.NoSuchElementException;

class BST {
    Node root;

    void printAll(){
        printAll(root);
    }

    void printAll(Node t){
        if(t == null){
            return;
        }
        printAll(t.left);
        System.out.println(t.key);
        printAll(t.right);
    }

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

    private Node predecessor(Node t){
        if(t == null){
            return null;
        }

        if(t.left != null){
            t = t.left;
            while(t.right != null){
                t = t.right;
            }
            return t;
        }
        else{
            Node parent = t.parent;
            while(parent != null && t != parent.right){
                t = parent;
                parent = parent.parent;
            }
            return parent;
        }
    }

    float getHighest(){
        if(root == null){
            throw new NoSuchElementException();
        }

        Node t = root;

        while(t.right != null){
            t = t.right;
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
            while(parent != null && t == parent.right){
                t = parent;
                parent = parent.parent;
            }
            return parent;
        }
    }

    static class Node {
        float key;
        Node left, right, parent;
        boolean isRed;
    }
}
