package main.java.loader;

import java.io.*;
import java.util.*;

// Implements a hash table that can be serialized
public class PersistentHashTable implements Serializable {
    private final List<Map<String, String>> buckets;

    // Constructor
    public PersistentHashTable(int size) {
        this.buckets = new ArrayList<>(Collections.nCopies(size, new HashMap<>()));
    }

    // Puts a key-value pair into the hash table
    public void put(String key, String value) {
        String normalizedKey = key.toLowerCase();
        int hash = normalizedKey.hashCode();
        int bucketIndex = Math.abs(hash % buckets.size());
        Map<String, String> bucket = buckets.get(bucketIndex);
        bucket.put(normalizedKey, value);

        System.out.println("Putting key: " + normalizedKey + " with value: " + value + " into bucket: " + bucketIndex);
        System.out.println("Current bucket size: " + bucket.size());
    }

    // Retrieves a value associated with a key from the hash table
    public String get(String key) {
        String normalizedKey = key.toLowerCase();
        int hash = normalizedKey.hashCode();
        int bucketIndex = Math.abs(hash % buckets.size());
        Map<String, String> bucket = buckets.get(bucketIndex);
        String value = bucket.get(normalizedKey);

        System.out.println("Retrieving key: " + normalizedKey + " from bucket: " + bucketIndex + ". Found: " + value);
        return value;
    }

    /* // ?????
    public void serializeToFile(String path) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        }
    }*/

    // Loads a hash table from a file and returns it as a PersistentHashTable object.
    public static PersistentHashTable deserializeFromFile(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (PersistentHashTable) ois.readObject();
        }
    }
}