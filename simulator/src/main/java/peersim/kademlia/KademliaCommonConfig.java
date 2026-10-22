/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia;

public class KademliaCommonConfig {
    public static int BITS = 256;
    public static int K = 36;
    public static int ALPHA = 3;
    public static int NBUCKETS = 256;
    public static int MAXREPLACEMENT = 20;
    public static int FINDMODE = 0;

    public static String info() {
        return String.format("[K=%d][ALPHA=%d][BITS=%d]", K, ALPHA, BITS);
    }
}

