package grpcClient;

import crypto.Crypto;
import kademlia.Node;
import kademlia.TripleNode;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair pair = Crypto.createKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        System.out.println(privateKey);
        System.out.println(publicKey);
        byte[] pk = privateKey.getEncoded();
        byte[] puk = publicKey.getEncoded();
        PublicKey publicKey1 =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(puk));
        PrivateKey privateKey1 =
                KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pk));
        System.out.println(privateKey1);
        System.out.println(publicKey1);
    }
}
