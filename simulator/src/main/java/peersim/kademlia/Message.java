/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia;

import java.util.HashMap;
import java.util.Map;
import peersim.kademlia.KademliaNode;
import peersim.kademlia.SimpleEvent;

public class Message
extends SimpleEvent {
    private static long ID_GENERATOR = 0L;
    public static final int MSG_EMPTY = 0;
    public static final int MSG_STORE = 1;
    public static final int MSG_INIT_FIND = 2;
    public static final int MSG_INIT_GET = 3;
    public static final int MSG_INIT_PUT = 4;
    public static final int MSG_FIND = 5;
    public static final int MSG_FIND_DIST = 6;
    public static final int MSG_RESPONSE = 7;
    public static final int MSG_PUT = 8;
    public static final int MSG_GET = 9;
    public static final int MSG_REGISTER = 10;
    public static final int MSG_INIT_REGISTER = 11;
    public static final int MSG_TICKET_REQUEST = 12;
    public static final int MSG_TICKET_RESPONSE = 13;
    public static final int MSG_TOPIC_QUERY = 14;
    public static final int MSG_REGISTER_RESPONSE = 15;
    public static final int MSG_TOPIC_QUERY_REPLY = 16;
    public static final int MSG_INIT_TOPIC_LOOKUP = 17;
    public static final int MSG_GET_RESPONSE = 18;
    public static final int MSG_ACK = 19;
    public static final int MSG_PONG = 21;
    public Object body = null;
    public Object value = null;
    public long id;
    public long ackId;
    public long operationId;
    public KademliaNode dst;
    public KademliaNode src;
    protected int nrHops = 0;

    public Message() {
        this(0, "");
    }

    public Message(int messageType) {
        this(messageType, "");
    }

    public Message(int messageType, Object body) {
        super(messageType);
        this.id = ID_GENERATOR++;
        this.body = body;
    }

    public Message(int messageType, Object body, Object value) {
        super(messageType);
        this.id = ID_GENERATOR++;
        this.body = body;
        this.value = value;
    }

    public static final Message makeInitFindNode(Object body) {
        return new Message(2, body);
    }

    public static final Message makeInitGetValue(Object body) {
        return new Message(3, body);
    }

    public static final Message makeInitPutValue(Object body, Object value) {
        return new Message(4, body, value);
    }

    public String toString() {
        String s = "[ID=" + this.id + "][DEST=" + String.valueOf(this.dst) + "]";
        return s + "[Type=" + this.typeToString() + "] BODY=(...)";
    }

    public Message copy() {
        Message dolly = new Message();
        dolly.type = this.type;
        dolly.src = this.src;
        dolly.dst = this.dst;
        dolly.operationId = this.operationId;
        dolly.body = this.body;
        return dolly;
    }

    public String typeToString() {
        switch (this.type) {
            case 0: {
                return "MSG_EMPTY";
            }
            case 1: {
                return "MSG_STORE";
            }
            case 2: {
                return "MSG_INIT_FIND";
            }
            case 5: {
                return "MSG_FIND";
            }
            case 7: {
                return "MSG_RESPONSE";
            }
            case 10: {
                return "MSG_REGISTER";
            }
            case 11: {
                return "MSG_INIT_REGISTER";
            }
            case 12: {
                return "MSG_TICKET_REQUEST";
            }
            case 13: {
                return "MSG_TICKET_RESPONSE";
            }
            case 14: {
                return "MSG_TOPIC_QUERY";
            }
            case 16: {
                return "MSG_TOPIC_QUERY_REPLY";
            }
            case 15: {
                return "MSG_REGISTER_RESPONSE";
            }
            case 17: {
                return "MSG_INIT_TOPIC_LOOKUP";
            }
        }
        return "UNKNOW:" + this.type;
    }

    public Map<String, Object> toMap(boolean sent) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", this.id);
        result.put("type", this.typeToString());
        result.put("src", this.src.getId());
        result.put("dst", this.dst.getId());
        if (sent) {
            result.put("status", "sent");
        } else {
            result.put("status", "received");
        }
        return result;
    }
}

