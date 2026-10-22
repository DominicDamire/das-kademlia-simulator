/*
 * Decompiled with CFR 0.152.
 */
package peersim.kademlia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.kademlia.Message;
import peersim.util.IncrementalStats;

public class KademliaObserver
implements Control {
    private static final String PAR_STEP = "step";
    public static IncrementalStats hopStore = new IncrementalStats();
    public static IncrementalStats timeStore = new IncrementalStats();
    public static IncrementalStats msg_deliv = new IncrementalStats();
    public static IncrementalStats find_op = new IncrementalStats();
    private static final String PAR_PROT = "protocol";
    public static IncrementalStats find_ok = new IncrementalStats();
    private static HashMap<String, Map<String, Object>> messages = new HashMap();
    private static String logFolderName;
    private static int observerStep;
    public static IncrementalStats rttStore;
    public static IncrementalStats getTimeStore;
    public static IncrementalStats putTimeStore;
    public static IncrementalStats findTimeStore;

    static {
        rttStore = new IncrementalStats();
        getTimeStore = new IncrementalStats();
        putTimeStore = new IncrementalStats();
        findTimeStore = new IncrementalStats();
    }

    public KademliaObserver(String prefix) {
        observerStep = Configuration.getInt(prefix + ".step");
        logFolderName = "./logs";
    }

    public static void reset() {
        hopStore = new IncrementalStats();
        rttStore = new IncrementalStats();
        timeStore = new IncrementalStats();
        msg_deliv = new IncrementalStats();
        find_op = new IncrementalStats();
        getTimeStore = new IncrementalStats();
        putTimeStore = new IncrementalStats();
        findTimeStore = new IncrementalStats();
        find_ok = new IncrementalStats();
        messages.clear();
    }

    private static void writeMap(Map<String, Map<String, Object>> map, String filename) {
        // recovery: CFR slip fixed by hand (bytecode re-checked against Damir's build)
        try (FileWriter writer = new FileWriter(filename);){
            Set<String> keySet = map.entrySet().iterator().next().getValue().keySet();
            String header = "";
            for (String string : keySet) {
                header = header + string + ",";
            }
            header = header.substring(0, header.length() - 1);
            header = header + "\n";
            writer.write(header);
            for (Map<String, Object> map2 : map.values()) {
                String line = "";
                for (String key : keySet) {
                    Object value = map2.get(key);
                    line = line + (value != null ? value.toString() : "") + ",";
                }
                line = line.substring(0, line.length() - 1);
                line = line + "\n";
                writer.write(line);
            }
            writer.close();
        }
        catch (IOException e) {
            System.err.println("Error writing messages map to CSV: " + e.getMessage());
            e.printStackTrace();
        }
        catch (NoSuchElementException e) {
            System.err.println("Cannot write messages map: map is empty.");
        }
    }

    public static void writeOut() {
        File directory = new File(logFolderName);
        if (!directory.exists()) {
            boolean created = directory.mkdir();
            if (!created) {
                System.err.println("Failed to create log directory: " + logFolderName);
                return;
            }
        }
        if (!messages.isEmpty()) {
            KademliaObserver.writeMap(messages, logFolderName + "/messages.csv");
        }
    }

    @Override
    public boolean execute() {
        int sz = 0;
        int i = 0;
        while (i < Network.size()) {
            if (Network.get(i) != null && Network.get(i).isUp()) {
                ++sz;
            }
            ++i;
        }
        System.gc();
        String s = String.format("[time=%d]:[N=%d current nodes UP] [D=%f msg deliv] [ HOP AVG h=%f] [ GET AVG l=%d msec] [ PUT AVG l=%d msec] [ FIND MIN l=%d msec] [ FIND MAX l=%d msec] [ FIND AVG l=%d msec] [find op sent=%d] ", CommonState.getTime(), sz, msg_deliv.getSum(), hopStore.getAverage(), (int)getTimeStore.getAverage(), (int)putTimeStore.getAverage(), (int)findTimeStore.getMin(), (int)findTimeStore.getMax(), (int)findTimeStore.getAverage(), (int)find_op.getSum());
        if (CommonState.getEndTime() <= (long)observerStep + CommonState.getTime()) {
            KademliaObserver.writeOut();
        }
        System.err.println(s);
        return false;
    }

    public static void reportMsg(Message m, boolean sent) {
        if (m.src == null) {
            return;
        }
        messages.put(String.valueOf(m.id), m.toMap(sent));
    }
}

