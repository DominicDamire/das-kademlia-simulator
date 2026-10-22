package peersim.kademlia;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * This control implements a simple traffic pattern for testing Kademlia PUT and GET operations. It
 * first initiates a PUT operation for a predefined key ("t1") with randomly generated hex data. On
 * subsequent executions, it initiates a GET operation for the same key.
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class TrafficGeneratorPutGet implements Control {

  /** Configuration parameter name for the Kademlia protocol */
  private static final String PAR_PROT = "protocol";

  /** The protocol identifier (PID) of the Kademlia protocol obtained from the configuration. */
  private final int pid;

  /** Flag to ensure the PUT message is sent only on the first execution. */
  private boolean first = true;

  public TrafficGeneratorPutGet(String prefix) {
    pid = Configuration.getPid(prefix + "." + PAR_PROT);
  }

  private Message generatePutMessage() {
    // Define the desired length for the random hex value.
    int desiredLength = (25600) * 1024; // Chosen length, might relate to chunking tests.
    StringBuilder sb = new StringBuilder(desiredLength);
    Random random = new Random();
    String hexChars = "0123456789abcdef"; // Characters for random hex string.

    // Generate the random hex string value.
    for (int i = 0; i < desiredLength; i++) {
      sb.append(hexChars.charAt(random.nextInt(hexChars.length())));
    }
    String value = sb.toString();

    MessageDigest digest;
    BigInteger id; // This will be the Kademlia key.

    try {
      // Use "t1" as the basis for the Kademlia key.
      String topic = "t1";
      digest = MessageDigest.getInstance("SHA-256"); // Use SHA-256 for hashing.
      byte[] hash = digest.digest(topic.getBytes(StandardCharsets.UTF_8)); // Hash the topic string.
      // Convert the hash bytes to a positive BigInteger to serve as the Kademlia key.
      id = new BigInteger(1, hash);

      // Create the initial PUT message using the factory method.
      Message m = Message.makeInitPutValue(id, value);
      m.timestamp = CommonState.getTime(); // Record the creation time.
      // System.out.println(
      //  "Generating Put message for key t1 with value: " + value); // Log generated value
      return m;
    } catch (NoSuchAlgorithmException e) {
      // Handle potential error if SHA-256 algorithm is not available.
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Generates an initial GET message (MSG_INIT_GET). The key is derived from the SHA-256 hash of
   * the string "t1" (same key as PUT).
   */
  private Message generateGetMessage() {

    MessageDigest digest;
    BigInteger id; // The Kademlia key to retrieve.
    try {
      // Use "t1" as the topic to generate the same key used in generatePutMessage.
      String topic = "t1";
      digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(topic.getBytes(StandardCharsets.UTF_8));
      id = new BigInteger(1, hash); // Convert hash to the BigInteger key.

      // Create the initial GET message using the factory method.
      Message m = Message.makeInitGetValue(id);
      m.timestamp = CommonState.getTime(); // Record the creation time.
      System.out.println("Generating Get message for key t1");
      return m;
    } catch (NoSuchAlgorithmException e) {
      // Handle potential error if SHA-256 algorithm is not available.
      e.printStackTrace();
      return null;
    }
  }

  public boolean execute() {

    Node start; // The node that will initiate the PUT or GET operation.

    do {
      start = Network.get(CommonState.r.nextInt(Network.size()));
    } while ((start == null) || (!start.isUp()));

    // Check the 'first' flag to decide whether to send PUT or GET.
    if (first) {
      // First execution: Generate and schedule a PUT message immediately (delay 0).
      Message putMsg = generatePutMessage();
      if (putMsg != null) {
        EDSimulator.add(0, putMsg, start, pid); // Schedule the PUT message.
      }
      first = false; // Set flag to false so subsequent calls send GET.
    } else {
      // Subsequent executions: Generate and schedule a GET message with a delay.
      Message getMsg = generateGetMessage();
      if (getMsg != null) {

        // This delay allows the previous PUT operation some time to complete.
        EDSimulator.add(9999, getMsg, start, pid);
      }
    }
    // Return false to indicate the simulation should continue.
    return false;
  }
}
