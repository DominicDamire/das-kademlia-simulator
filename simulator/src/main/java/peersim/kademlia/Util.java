/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import peersim.kademlia.KademliaCommonConfig;

public class Util {
    public static final String put0(BigInteger b) {
        if (b == null) {
            return null;
        }
        // recovery: CFR slip fixed by hand (bytecode re-checked against Damir's build)
        String s = b.toString(2);
        while (s.length() < KademliaCommonConfig.BITS) {
            s = "0" + s;
        }
        return s;
    }

    public static int logDistance(BigInteger a, BigInteger b) {
        BigInteger x = a.xor(b);
        return x.bitLength();
    }

    public static BigInteger xorDistance(BigInteger a, BigInteger b) {
        return a.xor(b);
    }

    public static byte[] SHA256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

