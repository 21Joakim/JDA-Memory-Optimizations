//   Copyright (c) 1999 CERN - European Organization for Nuclear Research.

package com.jockie.jda.memory.map.tiny.trove;

import java.util.Arrays;

/*
 * Modified for Trove to use the java.util.Arrays sort/search
 * algorithms instead of those provided with colt.
 */

/**
 * Used to keep hash table capacities prime numbers.
 * Not of interest for users; only for implementors of hashtables.
 *
 * <p>Choosing prime numbers as hash table capacities is a good idea
 * to keep them working fast, particularly under hash table
 * expansions.
 *
 * <p>However, JDK 1.2, JGL 3.1 and many other toolkits do nothing to
 * keep capacities prime.  This class provides efficient means to
 * choose prime capacities.
 *
 * <p>Choosing a prime is <tt>O(log 300)</tt> (binary search in a list
 * of 300 ints).  Memory requirements: 1 KB static memory.
 *
 * @author wolfgang.hoschek@cern.ch
 * @version 1.0, 09/24/99
 */
public final class TinyPrimeFinder {
    /**
     * The largest prime this class can generate; currently equal to
     * the largest prime which fits within <tt>2^16 - 1</tt>.
     */
    public static final int largestPrime = 65521;

    /**
     * The prime number list consists of 11 chunks.
     *
     * Each chunk contains prime numbers.
     *
     * A chunk starts with a prime P1. The next element is a prime
     * P2. P2 is the smallest prime for which holds: P2 >= 2*P1.
     *
     * The next element is P3, for which the same holds with respect
     * to P2, and so on.
     *
     * Chunks are chosen such that for any desired capacity >= 1000
     * the list includes a prime number <= desired capacity * 1.11.
     *
     * Therefore, primes can be retrieved which are quite close to any
     * desired capacity, which in turn avoids wasting memory.
     *
     * For example, the list includes
     * 1039,1117,1201,1277,1361,1439,1523,1597,1759,1907,2081.
     *
     * So if you need a prime >= 1040, you will find a prime <=
     * 1040*1.11=1154.
     *
     * Chunks are chosen such that they are optimized for a hashtable
     * growthfactor of 2.0;
     *
     * If your hashtable has such a growthfactor then, after initially
     * "rounding to a prime" upon hashtable construction, it will
     * later expand to prime capacities such that there exist no
     * better primes.
     *
     * In total these are about 32*10=320 numbers -> 1 KB of static
     * memory needed.
     *
     * If you are stingy, then delete every second or fourth chunk.
     */

    private static final int[] primeCapacities = {
        //chunk #0
        largestPrime,

        //chunk #1
        5,11,23,47,97,197,397,797,1597,3203,6421,12853,25717,51437,

        //chunk #2
        433,877,1759,3527,7057,14143,28289,56591,

        //chunk #3
        953,1907,3821,7643,15287,30577,61169,

        //chunk #4
        1039,2081,4177,8363,16729,33461,

        //chunk #5
        31,67,137,277,557,1117,2237,4481,8963,17929,35863,

        //chunk #6
        599,1201,2411,4831,9677,19373,38747,

        //chunk #7
        311,631,1277,2557,5119,10243,20507,41017,

        //chunk #8
        3,7,17,37,79,163,331,673,1361,2729,5471,10949,21911,43853,

        //chunk #9
        43,89,179,359,719,1439,2879,5779,11579,23159,46327,

        //chunk #10
        379,761,1523,3049,6101,12203,24407,48817
    };

    static { //initializer
        // The above prime numbers are formatted for human readability.
        // To find numbers fast, we sort them once and for all.

        Arrays.sort(primeCapacities);
    }

    /**
     * Returns a prime number which is <code>&gt;= desiredCapacity</code>
     * and very close to <code>desiredCapacity</code> (within 11% if
     * <code>desiredCapacity &gt;= 1000</code>).
     *
     * @param desiredCapacity the capacity desired by the user.
     * @return the capacity which should be used for a hashtable.
     */
    public static int nextPrime(int desiredCapacity) {
        if (desiredCapacity >= largestPrime) {
            return largestPrime;
        }
        int i = Arrays.binarySearch(primeCapacities, desiredCapacity);
        if (i<0) {
            // desired capacity not found, choose next prime greater
            // than desired capacity
            i = -i -1; // remember the semantics of binarySearch...
        }
        return primeCapacities[i];
    }
}
