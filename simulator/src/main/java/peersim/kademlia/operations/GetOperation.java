/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia.operations;

import java.math.BigInteger;
import peersim.kademlia.operations.FindOperation;

public class GetOperation
extends FindOperation {
    private Object value = null;
    private boolean metadataHandled = false;

    public GetOperation(BigInteger srcNode, BigInteger destNode, long timestamp) {
        super(srcNode, destNode, timestamp);
        // recovery: restored to match original bytecode
        this.value = null;
        this.metadataHandled = false;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isMetadataHandled() {
        return this.metadataHandled;
    }

    public void setMetadataHandled(boolean metadataHandled) {
        this.metadataHandled = metadataHandled;
    }
}

