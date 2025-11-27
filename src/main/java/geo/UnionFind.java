package main.java.geo;

public class UnionFind {
    // Declare arrays for storing the root and rank of each element, and a counter for the number of disjoint sets
    private int[] root;
    private int[] rank;
    private int count;

    // Constructor to initialize union-find structure with specified size
    public UnionFind(int size) {
        root = new int[size];
        rank = new int[size];
        count = size;
        for (int i = 0; i < size; i++) {
            root[i] = i;
            rank[i] = 1;
        }
    }

    // Find operation with path compression
    public int find(int x) {
        if (root[x] != x) {
            root[x] = find(root[x]); // Path compression
        }
        return root[x];
    }

    // Union operation using union by rank
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            if (rank[rootX] > rank[rootY]) {
                root[rootY] = rootX;
            } else if (rank[rootX] < rank[rootY]) {
                root[rootX] = rootY;
            } else {
                root[rootY] = rootX;
                rank[rootX]++;
            }
            count--;
        }
    }

    // Get the current number of disjoint sets
    public int getCount() {
        return count;
    }
}
