/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.kademlia.KademliaCommonConfig;
import peersim.kademlia.KademliaNode;
import peersim.kademlia.KademliaObserver;
import peersim.kademlia.KeyValueStore;
import peersim.kademlia.Message;
import peersim.kademlia.RoutingTable;
import peersim.kademlia.SimpleEvent;
import peersim.kademlia.Timeout;
import peersim.kademlia.Util;
import peersim.kademlia.operations.FindOperation;
import peersim.kademlia.operations.GetOperation;
import peersim.kademlia.operations.PutOperation;
import peersim.transport.UnreliableTransport;

public class KademliaProtocol
implements Cloneable,
EDProtocol {
    final String PAR_K = "K";
    final String PAR_ALPHA = "ALPHA";
    final String PAR_BITS = "BITS";
    final String PAR_FINDMODE = "FINDMODE";
    private static final String PAR_TRANSPORT = "transport";
    private static String prefix = null;
    private UnreliableTransport transport;
    private int tid;
    private int kademliaid;
    private static boolean _ALREADY_INSTALLED = false;
    private RoutingTable routingTable;
    private TreeMap<Long, Long> sentMsg;
    private LinkedHashMap<Long, FindOperation> findOp;
    public KademliaNode node = null;
    protected Logger logger;
    private Map<BigInteger, Integer> chunkRetryIndex = new HashMap<BigInteger, Integer>();
    private final HashMap<BigInteger, Boolean> retrievedChunks = new HashMap();
    private KeyValueStore kv;
    public int chunkSize = 1024;
    private double totalExpectedAcks = 0.0;
    private int receivedAcks = 0;
    private long putOperationStartTime = -1L;
    private BigInteger currentPutFileKey = null;
    private long chunkStartTime = -1L;
    private int requiredChunkCount = 1;
    private List<BigInteger> targetChunks = new ArrayList<BigInteger>();
    Map<Long, Long> findStartTimeMap = new HashMap<Long, Long>();

    @Override
    public Object clone() {
        KademliaProtocol dolly = new KademliaProtocol(prefix);
        return dolly;
    }

    public KademliaProtocol(String prefix) {
        KademliaProtocol.prefix = prefix;
        this._init();
        this.routingTable = new RoutingTable(KademliaCommonConfig.NBUCKETS, KademliaCommonConfig.K, KademliaCommonConfig.MAXREPLACEMENT);
        this.sentMsg = new TreeMap();
        this.findOp = new LinkedHashMap();
        this.tid = Configuration.getPid(prefix + ".transport");
        this.kv = new KeyValueStore();
    }

    private void _init() {
        if (_ALREADY_INSTALLED) {
            return;
        }
        KademliaCommonConfig.K = Configuration.getInt(prefix + ".K", KademliaCommonConfig.K);
        KademliaCommonConfig.ALPHA = Configuration.getInt(prefix + ".ALPHA", KademliaCommonConfig.ALPHA);
        KademliaCommonConfig.BITS = Configuration.getInt(prefix + ".BITS", KademliaCommonConfig.BITS);
        KademliaCommonConfig.FINDMODE = Configuration.getInt(prefix + ".FINDMODE", KademliaCommonConfig.FINDMODE);
        _ALREADY_INSTALLED = true;
    }

    private Node nodeIdtoNode(BigInteger searchNodeId) {
        BigInteger mId;
        if (searchNodeId == null) {
            return null;
        }
        int inf = 0;
        int sup = Network.size() - 1;
        while (inf <= sup) {
            int m = (inf + sup) / 2;
            mId = ((KademliaProtocol)Network.get(m).getProtocol(this.kademliaid)).getNode().getId();
            if (mId.equals(searchNodeId)) {
                return Network.get(m);
            }
            if (mId.compareTo(searchNodeId) < 0) {
                inf = m + 1;
                continue;
            }
            sup = m - 1;
        }
        int i = Network.size() - 1;
        while (i >= 0) {
            mId = ((KademliaProtocol)Network.get(i).getProtocol(this.kademliaid)).getNode().getId();
            if (mId.equals(searchNodeId)) {
                return Network.get(i);
            }
            --i;
        }
        return null;
    }

    private void handleResponse(Message m, int myPid) {
        FindOperation fop;
        if (m.src != null) {
            this.routingTable.addNeighbour(m.src.getId());
        }
        if ((fop = this.findOp.get(m.operationId)) != null) {
            BigInteger[] neighbours2;
            GetOperation getOp;
            BigInteger neighbour;
            BigInteger[] neighbours;
            fop.elaborateResponse((BigInteger[])m.body);
            this.logger.info("Handleresponse FindOperation " + fop.getId() + " " + fop.getAvailableRequests());
            BigInteger[] bigIntegerArray = neighbours = (BigInteger[])m.body;
            int n = neighbours.length;
            int n2 = 0;
            while (n2 < n) {
                neighbour = bigIntegerArray[n2];
                this.routingTable.addNeighbour(neighbour);
                ++n2;
            }
            if (!fop.isFinished() && Arrays.asList(neighbours).contains(fop.getDestNode())) {
                this.logger.warning("Found node " + String.valueOf(fop.getDestNode()));
                KademliaObserver.find_ok.add(1.0);
                fop.setFinished(true);
            }
            if (fop instanceof GetOperation && m.value instanceof byte[]) {
                getOp = (GetOperation)fop;
                BigInteger chunkKey = (BigInteger)getOp.getBody();
                this.retrievedChunks.put(chunkKey, true);
                getOp.setValue(m.value);
                fop.setFinished(true);
                if (this.retrievedChunks.size() >= this.requiredChunkCount) {
                    long totalTime = CommonState.getTime() - this.chunkStartTime;
                    System.out.println("[GET] \ud83c\udf89 Sufficient chunks retrieved! Stopping.");
                    System.out.println("GET operation took " + totalTime + " time units.");
                    KademliaObserver.getTimeStore.add(totalTime);
                    KademliaObserver.hopStore.add(fop.nrHops);
                    fop.setFinished(true);
                    this.retrievedChunks.clear();
                }
                return;
            }
            if (m.value == null && m.type == 18) {
                List<BigInteger> neighbours3;
                getOp = (GetOperation)fop;
                BigInteger chunkKey = (BigInteger)getOp.getBody();
                int retryIndex = this.chunkRetryIndex.getOrDefault(chunkKey, 0);
                if (retryIndex < (neighbours3 = fop.getNeighboursList()).size()) {
                    BigInteger nextNodeId = neighbours3.get(retryIndex);
                    Message retryMsg = new Message(9);
                    retryMsg.operationId = fop.getId();
                    retryMsg.src = this.getNode();
                    retryMsg.dst = this.nodeIdtoNode(nextNodeId).getKademliaProtocol().getNode();
                    retryMsg.body = getOp.getBody();
                    retryMsg.value = getOp.getValue();
                    this.sendMessage(retryMsg, nextNodeId, myPid);
                    this.chunkRetryIndex.put(chunkKey, retryIndex + 1);
                } else {
                    fop.setFinished(true);
                }
                return;
            }
            if (fop instanceof GetOperation && m.value instanceof List && !((GetOperation)fop).isMetadataHandled()) {
                ((GetOperation)fop).setMetadataHandled(true);
                fop.setFinished(true);
                ((GetOperation)fop).setValue(m.value);
                List chunkKeys = (List)m.value;
                Collections.shuffle(chunkKeys);
                this.targetChunks = chunkKeys.subList(0, 1023);
                this.requiredChunkCount = this.targetChunks.size();
                System.out.println("[GET] \ud83c\udfaf Required chunks to retrieve: " + this.requiredChunkCount);
                for (BigInteger chunkKey : this.targetChunks) {
                    GetOperation chunkFop = new GetOperation(this.node.getId(), chunkKey, m.timestamp);
                    chunkFop.setBody(chunkKey);
                    this.findOp.put(chunkFop.getId(), chunkFop);
                    neighbours2 = this.routingTable.getNeighbours(chunkKey, this.getNode().getId());
                    chunkFop.elaborateResponse(neighbours2);
                    chunkFop.setAvailableRequests(KademliaCommonConfig.ALPHA);
                    Message findMsg = new Message(5);
                    findMsg.operationId = chunkFop.getId();
                    findMsg.src = this.getNode();
                    findMsg.body = chunkKey;
                    chunkFop.nrHops++;
                    this.findStartTimeMap.put(findMsg.operationId, CommonState.getTime());
                    int i = 0;
                    while (i < KademliaCommonConfig.ALPHA) {
                        BigInteger nextNode = chunkFop.getNeighbour();
                        if (nextNode != null) {
                            findMsg.dst = this.nodeIdtoNode(nextNode).getKademliaProtocol().getNode();
                            this.sendMessage(findMsg.copy(), nextNode, myPid);
                        }
                        ++i;
                    }
                }
            }
            while (fop.getAvailableRequests() > 0) {
                neighbour = fop.getNeighbour();
                if (neighbour != null) {
                    if (fop.isFinished()) continue;
                    Message request = fop.getClass() == GetOperation.class ? new Message(5) : (KademliaCommonConfig.FINDMODE == 0 ? new Message(5) : new Message(6));
                    request.operationId = m.operationId;
                    request.src = this.getNode();
                    request.dst = this.nodeIdtoNode(neighbour).getKademliaProtocol().getNode();
                    this.findStartTimeMap.put(request.operationId, CommonState.getTime());
                    request.body = KademliaCommonConfig.FINDMODE == 0 || request.getType() == 9 ? fop.getDestNode() : Integer.valueOf(Util.logDistance(fop.getDestNode(), (BigInteger)fop.getBody()));
                    fop.nrHops++;
                    this.sendMessage(request, neighbour, myPid);
                    continue;
                }
                if (fop.getAvailableRequests() == KademliaCommonConfig.ALPHA) {
                    Long start = this.findStartTimeMap.get(fop.getId());
                    if (start != null) {
                        long duration = CommonState.getTime() - start;
                        KademliaObserver.findTimeStore.add(duration);
                    }
                    if (fop instanceof PutOperation) {
                        PutOperation putOp = (PutOperation)fop;
                        for (BigInteger id : fop.getNeighboursList()) {
                            Message putMsg = new Message(8);
                            putMsg.operationId = m.operationId;
                            putMsg.src = this.getNode();
                            putMsg.dst = this.nodeIdtoNode(id).getKademliaProtocol().getNode();
                            putMsg.body = putOp.getBody();
                            putMsg.value = putOp.getValue();
                            this.sendMessage(putMsg, id, myPid);
                        }
                        this.findOp.remove(fop.getId());
                    } else if (fop instanceof GetOperation) {
                        GetOperation getOp2 = (GetOperation)fop;
                        BigInteger chunkKey = (BigInteger)getOp2.getBody();
                        // recovery: CFR slip fixed by hand (bytecode re-checked against Damir's build)
                        List<BigInteger> neighboursList = fop.getNeighboursList();
                        if (this.retrievedChunks.containsKey(chunkKey)) {
                            this.findOp.remove(fop.getId());
                            return;
                        }
                        int index = this.chunkRetryIndex.getOrDefault(chunkKey, 0);
                        if (index >= neighboursList.size()) {
                            this.findOp.remove(fop.getId());
                            return;
                        }
                        BigInteger targetNode = neighboursList.get(index);
                        Message getMsg = new Message(9);
                        getMsg.operationId = m.operationId;
                        getMsg.src = this.getNode();
                        getMsg.dst = this.nodeIdtoNode(targetNode).getKademliaProtocol().getNode();
                        getMsg.body = chunkKey;
                        this.sendMessage(getMsg, targetNode, myPid);
                        this.chunkRetryIndex.put(chunkKey, index + 1);
                    } else {
                        this.findOp.remove(fop.getId());
                    }
                    if (fop.getBody().equals("Automatically Generated Traffic") && fop.getClosest().containsKey(fop.getDestNode())) {
                        long timeInterval = CommonState.getTime() - fop.getTimestamp();
                        KademliaObserver.timeStore.add(timeInterval);
                        KademliaObserver.hopStore.add(fop.nrHops);
                        KademliaObserver.msg_deliv.add(1.0);
                    }
                    return;
                }
                return;
            }
        } else {
            return;
        }
    }

    private void handlePut(Message m, int myPid) {
        Message ack = new Message(19);
        ack.operationId = m.operationId;
        ack.src = this.getNode();
        ack.dst = m.src;
        this.sendMessage(ack, m.src.getId(), myPid);
        this.kv.add((BigInteger)m.body, m.value);
    }

    private void handleFind(Message m, int myPid) {
        this.logger.info("handleFind received from " + String.valueOf(m.src.getId()) + " " + m.operationId);
        if (m.getType() != 9) {
            KademliaObserver.find_op.add(1.0);
        }
        BigInteger[] neighbours = new BigInteger[KademliaCommonConfig.K];
        if (m.getType() == 5 || m.getType() == 9) {
            neighbours = this.routingTable.getNeighbours((BigInteger)m.body, m.src.getId());
        } else if (m.getType() == 6) {
            neighbours = this.routingTable.getNeighbours((Integer)m.body);
        } else {
            return;
        }
        Message response = new Message(7, neighbours);
        response.operationId = m.operationId;
        response.dst = m.dst;
        response.src = this.getNode();
        response.ackId = m.id;
        this.sendMessage(response, m.src.getId(), myPid);
        if (m.getType() == 9) {
            Message response1 = new Message(18, neighbours);
            response1.operationId = m.operationId;
            response1.dst = m.dst;
            response1.src = this.getNode();
            response1.ackId = m.id;
            response1.value = this.kv.get((BigInteger)m.body);
            this.sendMessage(response1, m.src.getId(), myPid);
        }
    }

    private void handleInit(Node myNode, Message m, int myPid) {
        FindOperation fop;
        this.logger.info("handleInitFind " + String.valueOf((BigInteger)m.body));
        KademliaObserver.find_op.add(1.0);
        switch (m.type) {
            case 2: {
                fop = new FindOperation(this.node.getId(), (BigInteger)m.body, m.timestamp);
                break;
            }
            case 3: {
                KademliaObserver.reset();
                System.out.println("\ud83d\udce5 MSG_INIT_GET received");
                this.chunkStartTime = CommonState.getTime();
                BigInteger fileKey = (BigInteger)m.body;
                GetOperation fop2 = new GetOperation(this.node.getId(), fileKey, m.timestamp);
                fop2.setBody(fileKey);
                this.findOp.put(fop2.getId(), fop2);
                BigInteger[] neighbors = this.routingTable.getNeighbours(fileKey, this.node.getId());
                fop2.elaborateResponse(neighbors);
                fop2.setAvailableRequests(KademliaCommonConfig.ALPHA);
                int i = 0;
                while (i < KademliaCommonConfig.ALPHA) {
                    BigInteger nextNode = fop2.getNeighbour();
                    if (nextNode != null) {
                        Message findMsg = new Message(5);
                        findMsg.operationId = fop2.getId();
                        findMsg.src = this.getNode();
                        findMsg.dst = this.nodeIdtoNode(nextNode).getKademliaProtocol().getNode();
                        findMsg.body = fileKey;
                        this.findStartTimeMap.put(findMsg.operationId, CommonState.getTime());
                        fop2.nrHops++;
                        this.sendMessage(findMsg.copy(), nextNode, myPid);
                    }
                    ++i;
                }
                return;
            }
            case 4: {
                BigInteger fileKey1;
                byte[] fileData;
                System.out.println("\ud83d\udce5 MSG_INIT_PUT received");
                if (m.value instanceof String) {
                    fileData = ((String)m.value).getBytes(StandardCharsets.UTF_8);
                } else if (m.value instanceof byte[]) {
                    fileData = (byte[])m.value;
                } else {
                    throw new IllegalArgumentException("Unsupported data type for m.value");
                }
                this.putOperationStartTime = CommonState.getTime();
                Map<BigInteger, byte[]> chunkMap = this.chunkFileData(fileData, this.chunkSize);
                ArrayList<BigInteger> chunkKeys = new ArrayList<BigInteger>(chunkMap.keySet());
                ArrayList<BigInteger> fileMeta = new ArrayList<BigInteger>(chunkKeys);
                this.currentPutFileKey = fileKey1 = (BigInteger)m.body;
                this.receivedAcks = 0;
                System.out.println("\ud83d\udce6 Storing chunkKey list at fileKey: " + fileKey1.toString(16));
                PutOperation fileMetaOp = new PutOperation(this.node.getId(), fileKey1, m.timestamp);
                fileMetaOp.setValue(fileMeta);
                fileMetaOp.setBody(fileKey1);
                this.findOp.put(fileMetaOp.getId(), fileMetaOp);
                BigInteger[] fileMetaPeers = this.routingTable.getNeighbours(fileKey1, this.node.getId());
                fileMetaOp.elaborateResponse(fileMetaPeers);
                fileMetaOp.setAvailableRequests(KademliaCommonConfig.ALPHA);
                int i = 0;
                while (i < KademliaCommonConfig.ALPHA) {
                    BigInteger nextNode = fileMetaOp.getNeighbour();
                    if (nextNode != null) {
                        Message findMsg = new Message(5);
                        findMsg.src = this.getNode();
                        findMsg.dst = this.nodeIdtoNode(nextNode).getKademliaProtocol().getNode();
                        findMsg.body = fileKey1;
                        findMsg.operationId = fileMetaOp.getId();
                        this.findStartTimeMap.put(findMsg.operationId, CommonState.getTime());
                        fileMetaOp.nrHops++;
                        this.sendMessage(findMsg.copy(), nextNode, myPid);
                    }
                    ++i;
                }
                for (Map.Entry<BigInteger, byte[]> entry : chunkMap.entrySet()) {
                    BigInteger chunkKey = entry.getKey();
                    byte[] chunkValue = entry.getValue();
                    PutOperation chunkOp = new PutOperation(this.node.getId(), chunkKey, m.timestamp);
                    chunkOp.setValue(chunkValue);
                    chunkOp.setBody(chunkKey);
                    this.findOp.put(chunkOp.getId(), chunkOp);
                    BigInteger[] chunkPeers = this.routingTable.getNeighbours(chunkKey, this.node.getId());
                    chunkOp.elaborateResponse(chunkPeers);
                    chunkOp.setAvailableRequests(KademliaCommonConfig.ALPHA);
                    int i2 = 0;
                    while (i2 < KademliaCommonConfig.ALPHA) {
                        BigInteger nextNode = chunkOp.getNeighbour();
                        if (nextNode != null) {
                            Message chunkFindMsg = new Message(5);
                            chunkFindMsg.src = this.getNode();
                            chunkFindMsg.dst = this.nodeIdtoNode(nextNode).getKademliaProtocol().getNode();
                            chunkFindMsg.body = chunkKey;
                            chunkFindMsg.operationId = chunkOp.getId();
                            this.findStartTimeMap.put(chunkFindMsg.operationId, CommonState.getTime());
                            chunkOp.nrHops++;
                            this.sendMessage(chunkFindMsg.copy(), nextNode, myPid);
                        }
                        ++i2;
                    }
                }
                return;
            }
            default: {
                fop = new FindOperation(this.node.getId(), (BigInteger)m.body, m.timestamp);
            }
        }
        fop.setBody(m.body);
        this.findOp.put(fop.getId(), fop);
        BigInteger[] neighbours_default = this.routingTable.getNeighbours((BigInteger)m.body, this.getNode().getId());
        fop.elaborateResponse(neighbours_default);
        fop.setAvailableRequests(KademliaCommonConfig.ALPHA);
        m.operationId = fop.getId();
        m.src = this.getNode();
        int i = 0;
        while (i < KademliaCommonConfig.ALPHA) {
            BigInteger nextNode = fop.getNeighbour();
            if (nextNode != null) {
                m.dst = this.nodeIdtoNode(nextNode).getKademliaProtocol().getNode();
                if (m.type == 3) {
                    m.type = 9;
                } else if (KademliaCommonConfig.FINDMODE == 0) {
                    m.type = 5;
                } else {
                    m.type = 6;
                    m.body = Util.logDistance(nextNode, (BigInteger)fop.getBody());
                }
                this.logger.info("sendMessage to " + String.valueOf(nextNode));
                this.sendMessage(m.copy(), nextNode, myPid);
                fop.nrHops++;
            }
            ++i;
        }
    }

    private Map<BigInteger, byte[]> chunkFileData(byte[] fileData, int chunkSize) {
        LinkedHashMap<BigInteger, byte[]> chunkMap = new LinkedHashMap<BigInteger, byte[]>();
        this.totalExpectedAcks = (fileData.length / chunkSize + 1) * KademliaCommonConfig.K;
        int i = 0;
        while (i < fileData.length) {
            int end = Math.min(fileData.length, i + chunkSize);
            byte[] chunk = Arrays.copyOfRange(fileData, i, end);
            byte[] combined = ByteBuffer.allocate(4).putInt(i).array();
            byte[] uniqueHashInput = this.concat(combined, chunk);
            BigInteger chunkKey = new BigInteger(1, Util.SHA256(uniqueHashInput));
            chunkMap.put(chunkKey, chunk);
            i += chunkSize;
        }
        return chunkMap;
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public void sendMessage(Message m, BigInteger destId, int myPid) {
        if (m.getType() != 19) {
            KademliaObserver.msg_deliv.add(1.0);
            this.routingTable.addNeighbour(destId);
        }
        assert (m.src != null);
        assert (m.dst != null);
        Node src = this.nodeIdtoNode(this.getNode().getId());
        Node dest = this.nodeIdtoNode(destId);
        this.transport = (UnreliableTransport)Network.prototype.getProtocol(this.tid);
        this.transport.send(src, dest, m, this.kademliaid);
        long latency = this.transport.getLatency(src, dest);
        Timeout t = null;
        if (m.getType() == 5 || m.getType() == 6 || m.getType() == 9) {
            t = new Timeout(destId, m.id, m.operationId, false);
            EDSimulator.add(8L * latency, t, src, myPid);
        }
        if (m.getType() == 0) {
            t = new Timeout(destId, m.id, m.operationId, true);
            EDSimulator.add(8L * latency, t, src, myPid);
        }
        if (t != null) {
            this.sentMsg.put(m.id, m.timestamp);
        }
    }

    @Override
    public void processEvent(Node myNode, int myPid, Object event) {
        Message m;
        this.kademliaid = myPid;
        if (event instanceof Message) {
            m = (Message)event;
            KademliaObserver.reportMsg(m, false);
        }
        switch (((SimpleEvent)event).getType()) {
            case 7: {
                m = (Message)event;
                this.sentMsg.remove(m.ackId);
                this.handleResponse(m, myPid);
                break;
            }
            case 19: {
                m = (Message)event;
                if (this.currentPutFileKey == null) break;
                ++this.receivedAcks;
                if ((double)this.receivedAcks != this.totalExpectedAcks) break;
                long fullTime = CommonState.getTime() - this.putOperationStartTime;
                KademliaObserver.putTimeStore.add(fullTime);
                System.out.println("[PUT] \u2705 Full operation took " + fullTime + " time units for fileKey at " + String.valueOf(this.currentPutFileKey));
                this.currentPutFileKey = null;
                break;
            }
            case 18: {
                m = (Message)event;
                this.handleResponse(m, myPid);
                break;
            }
            case 2: 
            case 3: 
            case 4: {
                m = (Message)event;
                this.handleInit(myNode, m, myPid);
                break;
            }
            case 5: 
            case 6: 
            case 9: {
                m = (Message)event;
                this.handleFind(m, myPid);
                break;
            }
            case 8: {
                m = (Message)event;
                this.handlePut(m, myPid);
                break;
            }
            case 0: {
                m = (Message)event;
                if (m.src == null) break;
                Message pong = new Message(21);
                pong.operationId = m.operationId;
                pong.id = CommonState.r.nextLong();
                pong.ackId = m.id;
                pong.src = this.getNode();
                pong.dst = m.src;
                this.sendMessage(pong, m.src.getId(), myPid);
                break;
            }
            case 1: {
                break;
            }
            case 21: {
                m = (Message)event;
                if (!this.sentMsg.containsKey(m.ackId)) break;
                this.sentMsg.remove(m.ackId);
                break;
            }
            case 100: {
                boolean isChunkGetOperation;
                FindOperation fop;
                Timeout t = (Timeout)event;
                if (!this.sentMsg.containsKey(t.msgID)) break;
                this.sentMsg.remove(t.msgID);
                BigInteger timedOutNodeId = t.node;
                long operationId = t.opID;
                if (t.isEmptyPing) {
                    this.routingTable.removeNeighbour(timedOutNodeId);
                    break;
                }
                Message empty = new Message(0);
                empty.operationId = operationId;
                empty.id = CommonState.r.nextLong();
                empty.src = this.getNode();
                Node destNode = this.nodeIdtoNode(timedOutNodeId);
                if (destNode != null) {
                    empty.dst = destNode.getKademliaProtocol().getNode();
                    this.sendMessage(empty, timedOutNodeId, myPid);
                }
                if ((fop = this.findOp.get(operationId)) == null || fop.isFinished()) break;
                boolean bl = isChunkGetOperation = fop instanceof GetOperation && fop.getBody() instanceof BigInteger;
                if (isChunkGetOperation) {
                    GetOperation getOp = (GetOperation)fop;
                    BigInteger chunkKey = (BigInteger)getOp.getBody();
                    List<BigInteger> neighbours3 = getOp.getNeighboursList();
                    int retryIndex = this.chunkRetryIndex.getOrDefault(chunkKey, 0);
                    if (retryIndex < neighbours3.size()) {
                        BigInteger nextNodeId = neighbours3.get(retryIndex);
                        Message retryMsg = new Message(9);
                        retryMsg.operationId = operationId;
                        retryMsg.src = this.getNode();
                        Node destNodeObj = this.nodeIdtoNode(nextNodeId);
                        if (destNodeObj == null) break;
                        retryMsg.dst = destNodeObj.getKademliaProtocol().getNode();
                        retryMsg.body = chunkKey;
                        this.sendMessage(retryMsg, nextNodeId, myPid);
                        this.chunkRetryIndex.put(chunkKey, retryIndex + 1);
                        break;
                    }
                    if (!this.findOp.containsKey(operationId)) break;
                    this.findOp.get(operationId).setFinished(true);
                    break;
                }
                fop.getClosest().remove(timedOutNodeId);
                fop.setAvailableRequests(fop.getAvailableRequests() + 1);
                ArrayList<Long> otherOpIds = new ArrayList<Long>(this.findOp.keySet());
                Iterator iterator = otherOpIds.iterator();
                while (iterator.hasNext()) {
                    FindOperation otherFop;
                    long otherId = (Long)iterator.next();
                    if (otherId == operationId || (otherFop = this.findOp.get(otherId)) == null || otherFop.isFinished()) continue;
                    otherFop.getClosest().remove(timedOutNodeId);
                }
                break;
            }
        }
    }

    public KademliaNode getNode() {
        return this.node;
    }

    public RoutingTable getRoutingTable() {
        return this.routingTable;
    }

    public void setProtocolID(int protocolID) {
        this.kademliaid = protocolID;
    }

    public void setNode(KademliaNode node) {
        this.node = node;
        this.routingTable.setNodeId(node.getId());
        this.logger = Logger.getLogger(node.getId().toString());
        this.logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        this.logger.setLevel(Level.WARNING);
        handler.setFormatter(new SimpleFormatter(){
            private static final String format = "[%d][%s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format, CommonState.getTime(), KademliaProtocol.this.logger.getName(), lr.getMessage());
            }
        });
        this.logger.addHandler(handler);
    }
}

