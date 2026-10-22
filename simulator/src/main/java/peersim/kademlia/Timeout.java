/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia;

import java.math.BigInteger;
import peersim.kademlia.SimpleEvent;

public class Timeout
extends SimpleEvent {
    public static final int TIMEOUT = 100;
    public BigInteger node;
    public long msgID;
    public long opID;
    public boolean isEmptyPing;

    public Timeout(BigInteger node, long msgID, long opID, boolean isEmptyPing) {
        super(100);
        this.node = node;
        this.msgID = msgID;
        this.opID = opID;
        this.isEmptyPing = isEmptyPing;
    }
}

