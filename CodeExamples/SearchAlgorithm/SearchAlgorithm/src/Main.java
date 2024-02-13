public class Main {
    public static void main(String[] args) {

    }

    boolean binarySearch(float x, float[] array, int lo, int hi){
        if(lo >= hi){
            return false;
        }

        int mid = lo + (hi - lo)/2;

        if(x < array[mid]){
            return binarySearch(x, array, lo, mid);
        }
        else if(x == array[mid]){
            return true;
        }
        else{
            return binarySearch(x, array, mid + 1, hi);
        }
    }

}

class Tree{
    static class Node{
        float key;
        Node left,right;
    }
    Node root;

    boolean contains(float x){
        Node t = root;

        for(;;){
            if(t == null){
                return false;
            }
            else if(x < t.key){
                t = t.left;
            }
            else if (x == t.key){
                return true;
            }
            else{
                t = t.right;
            }
        }
    }
}