package benchmarks;

import org.openjdk.jmh.annotations.*;
import utils.MergeSort;
import utils.UnionFind;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class LeafBenchmark {

    private UnionFind uf;

    @Setup(Level.Trial)
    public void setup() {
        uf = new UnionFind(512 * 512);
        for (int i = 0; i < 512 * 512 - 1; i++) {
            uf.activate(i);
            uf.activate(i + 1);
            uf.union(i, i + 1);
        }
    }

    @Benchmark
    public void benchmarkUnionFind() {
        for (int i = 0; i < 512 * 512 - 1; i++) {
            uf.activate(i);
            uf.activate(i + 1);
            uf.union(i, i + 1);
        }
    }

    @Benchmark
    public int benchmarkFind() {
        return uf.find(512 * 512 - 1);
    }

    @Benchmark
    public void benchmarkMergeSort() {
        List<Integer> sizes = new ArrayList<>();
        for (int i = 262_144; i >= 0; i--) {
            sizes.add(i);
        }
        MergeSort.sort(sizes, Integer::compareTo);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
