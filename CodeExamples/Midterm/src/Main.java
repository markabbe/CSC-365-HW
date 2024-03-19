import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Create two BitSet64 instances
        BitSet64 a = new BitSet64();
        BitSet64 b = new BitSet64();

        // Add elements to set a
        a.add(0);
        a.add(2);
        a.add(4);
        a.add(5);

        // Add elements to set b
        b.add(0);
        b.add(1);
        b.add(3);
        b.add(4);

        // Calculate and print the total size of unique elements in both sets
        int totalSize = BitSet64.totalSize(a, b);
        System.out.println("Total unique elements in both sets: " + totalSize);

        // Optionally, print elements of both sets to visually verify
        System.out.print("Elements in set a: ");
        a.printAll();
        System.out.println(); // Move to the next line

        System.out.print("Elements in set b: ");
        b.printAll();
        System.out.println(); // Move to the next line
    }
}

class BitSet64 {
    long bits;
    static long bitFor(int i) { return 1L << i; }
    void add(int key) { bits |= bitFor(key); }
    boolean contains(int key) { return (bits & bitFor(key)) != 0; }
    void remove(int key) { bits &= ~bitFor(key); }
    void addAll(BitSet64 x) { bits |= x.bits; }
    void removeAll(BitSet64 x) { bits &= ~x.bits; }
    boolean isEmpty() { return (bits == 0L); }
    void printAll() {
        for (int i = 0; i < 64; ++i) {
            if (contains(i))
                System.out.print(i);
        }
    }
    int size() { return Long.bitCount(bits); }
    int any() {
        if (bits == 0L) throw new NoSuchElementException();
        return Long.numberOfTrailingZeros(bits);
    }

    static int totalSize(BitSet64 a, BitSet64 b) {
        BitSet64 unionSet = new BitSet64();
        unionSet.addAll(a);
        unionSet.addAll(b);
        return unionSet.size();
    }
}

class Box implements Comparable<Box>{
    int height, width, depth;
    Box(int h, int w, int d){height = h; width = w; depth = d;}
    public boolean equals(Object o){
        if (!(o instanceof Box)){
            return false;
        }
        Box x = (Box) o;
        return height == x.height && width == x.width && depth == x.depth;
    }
    public int hashCode() {
        int result = 17;
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + depth;
        return result;
    }
    public int compareTo(Box r) {
        int thisVolume = this.height * this.width * this.depth;
        int otherVolume = r.height * r.width * r.depth;
        return Integer.compare(thisVolume, otherVolume);
    }
}

class RBTreeSet{
    static class Node{
        float key;
        Node left, right, parent;
        boolean isRed;

        // Constructor for convenience
        Node(float key) {
            this.key = key;
            left = right = parent = null;
            isRed = true;
        }
    }
    Node root;

    boolean hasSmallerKey(float key){
        Node current = root;

        while (current != null) {
            if (current.key < key) {
                return true;
            } else {
                current = current.left;
            }
        }
        return false;
    }
}
