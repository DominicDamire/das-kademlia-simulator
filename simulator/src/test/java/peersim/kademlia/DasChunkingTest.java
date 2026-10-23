package peersim.kademlia;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DasChunkingTest {

    private static byte[] data(int n) {
        byte[] b = new byte[n];
        new Random(1).nextBytes(b);
        return b;
    }

    @Test
    void chunkCountIsCeilOfSizeOver1024() {
        for (int n : new int[] {1, 1023, 1024, 1025, 5000, 25 * 1024}) {
            assertEquals((n + 1023) / 1024, KademliaProtocol.chunk(data(n), 1024).size(), "size " + n);
        }
    }

    @Test
    void sameInputGivesSameKeysInSameOrder() {
        byte[] d = data(10_000);
        assertEquals(
                new ArrayList<>(KademliaProtocol.chunk(d, 1024).keySet()),
                new ArrayList<>(KademliaProtocol.chunk(d.clone(), 1024).keySet()));
    }

    @Test
    void keyIsSha256OfOffsetThenChunkAndChunksReassemble() {
        byte[] d = data(2500);
        Map<BigInteger, byte[]> m = KademliaProtocol.chunk(d, 1024);
        int off = 0;
        for (Map.Entry<BigInteger, byte[]> e : m.entrySet()) {
            byte[] c = e.getValue();
            byte[] in = ByteBuffer.allocate(4 + c.length).putInt(off).put(c).array();
            assertEquals(new BigInteger(1, Util.SHA256(in)), e.getKey());
            byte[] expected = java.util.Arrays.copyOfRange(d, off, Math.min(d.length, off + 1024));
            assertArrayEquals(expected, c);
            off += 1024;
        }
        assertEquals(1024, m.values().iterator().next().length);
    }

    @Test
    void identicalChunksAtDifferentOffsetsGetDifferentKeys() {
        // offset is part of the hash input, so repeated content must not collapse
        assertEquals(4, KademliaProtocol.chunk(new byte[4096], 1024).size());
    }

    @Test
    void differentDataGivesDifferentKeys() {
        byte[] a = data(2048);
        byte[] b = a.clone();
        b[0] ^= 1;
        assertNotEquals(
                KademliaProtocol.chunk(a, 1024).keySet(), KademliaProtocol.chunk(b, 1024).keySet());
    }

    @Test
    void replicationTargetIsChunksPlusOneTimesK() {
        // 25 MiB = 25,600 chunks -> (25,600 + 1) * 36 = 921,636 (the README figure)
        assertEquals(921_636.0, KademliaProtocol.expectedAcks(25_600 * 1024, 1024, 36));
        assertEquals(5 * 3.0, KademliaProtocol.expectedAcks(4 * 1024 + 10, 1024, 3));
    }

    @Test
    void randomSampleHasConfiguredNumberOfDistinctKeys() {
        List<BigInteger> keys = new ArrayList<>(KademliaProtocol.chunk(data(25_600 * 1024 / 8), 1024).keySet());
        assertTrue(keys.size() >= 1023);
        Set<BigInteger> all = new HashSet<>(keys);
        List<BigInteger> s = KademliaProtocol.sampleKeys(keys, 1023);
        assertEquals(1023, s.size());
        assertEquals(1023, new HashSet<>(s).size());
        assertTrue(all.containsAll(s));
    }

    @Test
    void sampleLargerThanListThrows() {
        // documents the known limit from the README: payloads under 1,023 chunks fail
        List<BigInteger> keys = new ArrayList<>(KademliaProtocol.chunk(data(2048), 1024).keySet());
        org.junit.jupiter.api.Assertions.assertThrows(
                IndexOutOfBoundsException.class, () -> KademliaProtocol.sampleKeys(keys, 1023));
    }
}
