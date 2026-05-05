package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnionFindTest {

    @Test
    void singleActivatedElement(){
        UnionFind uf = new UnionFind(5);
        uf.activate(0);
        assertEquals(0,uf.find(0));
        assertEquals(-1,uf.find(1000));
        assertEquals(-1,uf.find(-1));
    }


    @Test
    void union() {
        UnionFind uf = new UnionFind(10);
        uf.activate(0);
        uf.activate(1);
        assertEquals(0,uf.find(0));
        assertEquals(1,uf.find(1));
        uf.union(0,1);
        assertEquals(0,uf.find(0));
        assertEquals(uf.find(0),uf.find(1));
    }
    @Test
    void unionBySize() {
        UnionFind uf = new UnionFind(10);
        uf.activate(0);
        uf.activate(1);
        uf.union(0, 1);
        int root = uf.find(0);
        assertEquals(2, uf.getSize()[root]);
    }

    @Test
    void find() {
        UnionFind uf = new UnionFind(10);
        uf.activate(0);
        uf.activate(1);
        assertEquals(0,uf.find(0));
        assertEquals(1,uf.find(1));
    }
    @Test
    void pathCompression(){
        UnionFind uf = new UnionFind(10);
        uf.activate(0);
        uf.activate(1);
        uf.activate(2);
        uf.activate(3);
        uf.union(0,1);
        uf.union(1,2);
        uf.union(2,3);
        int root = uf.find(0);
        int[] parent = uf.getParent();
        assertEquals(root, parent[0]);
        assertEquals(root, parent[1]);
        assertEquals(root, parent[2]);

    }
    @Test
    void inactiveElement(){
        UnionFind uf = new UnionFind(10);
        assertEquals(-1,uf.find(0));
    }

    @Test
    void unionSameSet(){
        UnionFind uf = new UnionFind(10);
        uf.activate(0);
        uf.union(0,0);
        assertEquals(0,uf.find(0));
        assertEquals(1,uf.getSize()[0]);
    }
    @Test
    void multipleUnions(){
        UnionFind uf = new UnionFind(10);
        for(int i = 0; i < 5; i++){
            uf.activate(i);
        }
        uf.union(0,1);
        uf.union(1,2);
        uf.union(3,4);
        assertEquals(uf.find(0), uf.find(2));
        assertEquals(uf.find(3), uf.find(4));
        assertNotEquals(uf.find(0), uf.find(3));
    }
}