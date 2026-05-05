package utils;

public class LeafCluster {
    public final int root;
    public final int minX, maxX, minY, maxY;
    public final int pixelCount;
    public int rank;

    /**
     * Represents a full leaf of connected pixels, storing its coords and size
     */
    public LeafCluster(int root, int minX, int maxX, int minY, int maxY, int pixelCount) {
        this.root = root;
        this.minX = minX; this.maxX = maxX;
        this.minY = minY; this.maxY = maxY;
        this.pixelCount = pixelCount;
    }
}