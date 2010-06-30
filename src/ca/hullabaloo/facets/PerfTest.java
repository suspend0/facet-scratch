package ca.hullabaloo.facets;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PerfTest {
    private static final int WARMUP = 20000;
    private static final int TIMED = 1000000;

    public static void main(String... args) {
        AssetFacets af = new CompressedAssetFacets();
        List<FExpr> queries = populate(af);

        // warmup
        run("warmup1", WARMUP, af, queries);
        run("warmup2", WARMUP, af, queries);

        // timing
        for (int i = 0; i < 10; i++)
            run("timed" + i, TIMED / 10, af, queries);
    }

    private static void run(String label, final int finds, AssetFacets af, List<FExpr> queries) {
        long start = System.nanoTime();
        Iterator<FExpr> expr = Iterators.cycle(queries);
        for (int n = finds; n != 0; n--)
            af.find(expr.next());
        long end = System.nanoTime();
        System.out.printf("%s: %s runs\nElapsed: %1.3f secs, %s msecs per 1000 queries\n\n",
                label, finds,
                TimeUnit.NANOSECONDS.toMillis(end - start) / 1000d,
                TimeUnit.NANOSECONDS.toMillis((end - start) / (finds / 1000))
        );

    }

    private static List<FExpr> populate(AssetFacets af) {
        long start, end;
        System.out.printf("Generating raw data [%s]\n", af.getClass());

        String[] assets = new RandomStrings().size(50000).minLength(20).maxLength(30).asArray();
        String[] types = new RandomStrings().size(18).minLength(5).maxLength(20).asArray();
        String[] facets = new RandomStrings().size(300).minLength(10).maxLength(30).asArray();

        System.out.printf("%s assets, %s types, %s values\n", assets.length, types.length, facets.length);

        long product = 1;
        product *= assets.length;
        product *= types.length;
        product *= facets.length;

        double density = 0.01d;
        int count = (int) (product * density);

        Iterator<String> assetSeq = Iterators.cycle(assets);
        Iterator<String> typesSeq = Iterators.cycle(types);
        Iterator<String> facetsSeq = Iterators.cycle(facets);

        System.out.printf("Adding %s asset/facet associations (%1.0f%% density)\n", count, density * 100);
        start = System.nanoTime();

        List<Facet> toQuery = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            if (i > 0 && i % 100000 == 0) System.out.println(i);
            Asset a = new Asset(assetSeq.next());
            Facet f = Facet.create(typesSeq.next(), facetsSeq.next());
            af.add(a, f);
            if (i % 40 == 0) toQuery.add(f);
        }
        end = System.nanoTime();
        System.out.printf("%1.3f secs\n", TimeUnit.NANOSECONDS.toMillis(end - start) / 1000d);

        System.out.println("Compiling Queries");
        start = System.nanoTime();

        List<FExpr> queries = Lists.newArrayList();
        Iterator<Facet> iter = toQuery.iterator();
        while (iter.hasNext()) {
            StringBuilder q = new StringBuilder();
            q.append(iter.next());
            if (iter.hasNext())
                q.append(" && ").append(iter.next());
            if (iter.hasNext())
                q.append(" || ").append(iter.next());
            queries.add(FExpr.compile(q.toString()));
        }
        end = System.nanoTime();
        System.out.printf("%1.3f secs\n", TimeUnit.NANOSECONDS.toMillis(end - start) / 1000d);


        System.out.printf("Loaded %s facets for %s assets\nQuerying %s facets with %s queries\n\n",
                count, assets.length, toQuery.size(), queries.size());

        return queries;
    }

    private static class Randomizer {
        Random r = new Random();

        String rand(String[] source) {
            return source[r.nextInt(source.length)];
        }
    }

    private static class RandomStrings {
        private static final char[] CHARS = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z', '_', '/'
        };

        private final Random r = new Random();
        private int size;
        private int minLength;
        private int maxLength;
        private byte[] bytes;
        private char[] chars;

        public RandomStrings size(int size) {
            this.size = size;
            return this;
        }

        public RandomStrings minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public RandomStrings maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public String[] asArray() {
            init();
            String[] result = new String[size];
            for (int i = 0; i < result.length; i++)
                result[i] = nextRandomString();
            return result;
        }

        private void init() {
            Preconditions.checkState(size > 0);
            Preconditions.checkState(maxLength > 0);
            Preconditions.checkState(minLength <= maxLength);
            bytes = new byte[maxLength];
            chars = new char[maxLength];
        }

        private String nextRandomString() {
            r.nextBytes(bytes);
            for (int i = 0; i < bytes.length; i++) {
                chars[i] = CHARS[(bytes[i] & 0xff) >>> 2];
            }
            return new String(chars, 0, minLength + r.nextInt(maxLength - minLength));
        }
    }
}
