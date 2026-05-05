package utils;

/**
 * Union Find for finding and grouping pixels into clusters
 */
public class UnionFind {


    private int[] parent; //stores the parent of each pixel
    private int[] size; //stores the size of each set

    public UnionFind(int n) {
        parent = new int[n]; // array to store parent of each pixel
        size = new int[n]; //array to store size of each set
        for (int i = 0; i < n; i++) {
            parent[i] = -1; // all pixels set to -1
            size[i] = 0; //size of set i 0
        }
    }

    public void activate(int i) {
        if (parent[i] == -1) {
            parent[i] = i;
            size[i] = 1; // A single white pixel has a size of 1
        }
    }

    /**
     * Merge the sets containing p and q
     */
    public void union(int p, int q) {
        int rootP = find(p); //find p root
        int rootQ = find(q); //find q root
        if (rootP != rootQ) { //only union if they aren't in the same set
            //Attach the smaller set to the larger set
            if (size[rootP] < size[rootQ]) {
                parent[rootP] = rootQ;
                size[rootQ] += size[rootP]; //update size
            } else {
                parent[rootQ] = rootP;
                size[rootP] += size[rootQ];
            }
        }
    }

    /**
     * Finds the root of set containing element i
     * @return the root of set i, or -1 if invalid
     */

    public int find(int i) {
        // Invalid elements are given value -1
        if (i < 0 || i >= parent.length || parent[i] == -1) {
            return -1; //the element is inactive or out of bounds
        }
        // If the node points to itself it is the root
        if (parent[i] == i) {
            return i;
        }
        // Use recursion to find root, uses path compression to flatten tree
        return parent[i] = find(parent[i]);
    }
    public int[] getParent() {
        return parent;
    }
    public int[] getSize() {
        return size;
    }
    private void setParent(int[] parent) {
        this.parent = parent;
    }
    private void setSize(int[] size) {
        this.size = size;
    }

}