import config.Utils;
import kademlia.Node;

public class DecentAuction {
    private static final Utils utils = new Utils();
    public static void main(String[] args) {
        String s = "10010011";
        String s2 = "01011010";
        byte[] b = s.getBytes();
        byte[] b2 = s2.getBytes();
        Node n1 = new Node(b);
        Node n2 = new Node(b2);
        System.out.println(utils.getStringFromBytes(n1.distanceXOR(n2.getNodeId())));

    }
}
