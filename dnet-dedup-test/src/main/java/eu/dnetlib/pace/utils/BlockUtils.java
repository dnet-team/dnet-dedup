package eu.dnetlib.pace.utils;

import com.google.common.collect.Lists;
import eu.dnetlib.Block;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockUtils implements Serializable {

    public static double getOptimalComparisonNumber(JavaRDD<Block> blocks) {

        double SMOOTHING_FACTOR = 1.05;

        //pairRDD: cardinality, #elements
        List<Tuple2<Integer, Tuple2<Integer, Integer>>> collect = blocks.mapToPair(b -> new Tuple2<>(b.comparisons(), b.elements()))
                .mapToPair(bs -> new Tuple2<>(bs._1(), new Tuple2<>(bs._1(), bs._2())))
                .reduceByKey((a, b) -> new Tuple2<>(a._1() + b._1(), a._2() + b._2())).collect();

        collect = new ArrayList<>(collect);
        collect.sort(Comparator.comparing(Tuple2::_1));

        double[] blockAssignments = new double[collect.size()];
        double[] comparisonsLevel = new double[collect.size()];
        double[] totalComparisonsPerLevel = new double[collect.size()];
        Integer totalComparisons = collect.get(0)._2()._1();
        Integer totalBlockSize = collect.get(0)._2()._2();
        blockAssignments[0] = totalBlockSize;
        comparisonsLevel[0] = collect.get(0)._1();
        totalComparisonsPerLevel[0] = totalComparisons;
        for (int i=1; i<collect.size(); i++){
            Integer comparisonLevel = collect.get(i)._1();
            totalComparisons += collect.get(i)._2()._1();
            totalBlockSize += collect.get(i)._2()._2();

            blockAssignments[i] = totalBlockSize;
            comparisonsLevel[i] = comparisonLevel;
            totalComparisonsPerLevel[i] = totalComparisons;
        }

        double currentBC = 0;
        double currentCC = 0;
        double currentSize = 0;
        double previousBC = 0;
        double previousCC = 0;
        double previousSize = 0;
        int arraySize = blockAssignments.length;
        for (int i = arraySize-1; 0 <= i; i--) {
            previousSize = currentSize;
            previousBC = currentBC;
            previousCC = currentCC;

            currentSize = comparisonsLevel[i];
            currentBC = blockAssignments[i];
            currentCC = totalComparisonsPerLevel[i];

            if (currentBC*previousCC < SMOOTHING_FACTOR*currentCC*previousBC) {
                break;
            }
        }

        return previousSize;
    }

    public static int getOptimalBlockSize(JavaRDD<Block> blocks){

        BigInteger numberOfComparisons = BigInteger.ZERO;
        BigInteger totalSizeOfBlocks = BigInteger.ZERO;
        BigInteger blockSize;

        //block_size, frequency
        JavaPairRDD<Integer, Integer> blocksFreq = blocks.mapToPair(b -> new Tuple2<>(b.getKey(), b.elements()))
                .mapToPair(bs -> new Tuple2<>(bs._2(),1))
                .reduceByKey((a,b) -> a+b).sortByKey();

        ArrayList<Tuple2<Integer, Integer>> blockSizesAndFreq = new ArrayList<>(blocksFreq.collect());

        double CC = 0d;
        int freq;

        /*
         * statistics: array of pairs (blockSize, CC) for every blockSize
         */
        ArrayList<Tuple2<Integer, Double>> statistics = new ArrayList<>();

        for (int i = 0; i < blockSizesAndFreq.size(); i++) {
            blockSize = new BigInteger(blockSizesAndFreq.get(i)._1.toString());

            freq = blockSizesAndFreq.get(i)._2;

            totalSizeOfBlocks = totalSizeOfBlocks.add(BigInteger.valueOf(freq).multiply(blockSize));

            //accumulated number of comparisons
            numberOfComparisons = numberOfComparisons.add(BigInteger.valueOf(freq)
                    .multiply(blockSize.multiply(blockSize.subtract(BigInteger.ONE)).shiftLeft(1)));

            CC = totalSizeOfBlocks.doubleValue() / numberOfComparisons.doubleValue();

            Tuple2<Integer, Double> st = new Tuple2<>(blockSize.intValue(), CC);

            statistics.add(st);

        }

        int optimalBlockSize = statistics.get(statistics.size() - 1)._1;// lastBlockSize;

        double eps = 1d; //smoothing factor

        /*
         * find minimum difference for every adjacent pair i,i-1 the minimum difference
         * represents the optimal blockSize
         */
        for (int i = statistics.size() - 1; i >= 1; i--) {
            if (Math.abs(statistics.get(i)._2 - statistics.get(i - 1)._2) < eps) {

                eps = Math.abs(statistics.get(i)._2 - statistics.get(i - 1)._2);

                optimalBlockSize = statistics.get(i)._1;
            }
        }
        return optimalBlockSize;
    }

    //cut blocks basing on number of elements
    public static JavaRDD<Block> blockPurging(JavaRDD<Block> blocks) {

        int optimalBlockSize = getOptimalBlockSize(blocks);

        System.out.println("optimalBlockSize = " + optimalBlockSize);

        return blocks.filter(b -> b.getElements().size() < optimalBlockSize);
    }

    //cut blocks basing on number of comparisons
    public static JavaRDD<Block> blockPurging2(JavaRDD<Block> blocks) {

        double optimalComparisonNumber = getOptimalComparisonNumber(blocks);

        System.out.println("optimalComparisonNumber = " + optimalComparisonNumber);

        return blocks.filter(b -> b.comparisons() < optimalComparisonNumber);
    }

    public static JavaRDD<Block> blockFiltering(JavaRDD<Block> blocks) {
        double RATIO = 0.85;

        return blocks
                .flatMapToPair(b -> b.getElements().stream().map(e -> new Tuple2<>(e, new Tuple2<>(b.getKey(), b.comparisons()))).iterator())
                .groupByKey()
                .mapToPair(es -> {
                    List<Tuple2<String, Integer>> b = Lists.newArrayList(es._2());
                    b.sort(Comparator.comparing(Tuple2::_2));
                    int size = b.size();
                    long limit = Math.round(size*RATIO);
                    return new Tuple2<>(es._1(),b.subList(0,(int)limit));
                })
                .flatMapToPair(es -> es._2().stream().map(it -> new Tuple2<>(it._1(), es._1())).collect(Collectors.toList()).iterator())
                .groupByKey().map(b -> new Block(b._1(), b._2()));
    }

}
