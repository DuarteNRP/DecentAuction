import config.Utils;
import crypto.Crypto;
import kademlia.Node;
import java.math.BigInteger;
public class DecentAuction {
    private static final Utils utils = new Utils();
    private static final Crypto cripto = new Crypto();
    public static void main(String[] args) {
        /*String s = "10010011";
        String s2 = "01011010";
        char a = 'A';
        char b1 = 'B';
        System.out.println(a^b1);
        byte[] b = s.getBytes();
        byte[] b2 = s2.getBytes();
        Node n1 = new Node(b);
        Node n2 = new Node(b2);
        System.out.println(utils.getStringFromBytes(n1.distanceXOR(n2.getNodeId())));
         */
        System.out.println(utils.log2((int)14.333));
        String test = cripto.hash("fioerhgur4go3454otn3krgnl4");
        System.out.println(test);
        System.out.println(new BigInteger(test,16));
        System.out.println(new BigInteger(test,16).toString(2).length());
        String bin = "1111";
        String bin2 = "1010";
        int numero1 = Integer.parseInt(bin, 2);
        int numero2 = Integer.parseInt(bin2, 2);//Nome da variavel e tipo, 2 = binary. Converte o binario para int
        System.out.println(numero1^numero2);

    }
}
