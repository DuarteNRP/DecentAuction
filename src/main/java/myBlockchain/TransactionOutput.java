package myBlockchain;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import config.Utils;
import crypto.Crypto;

public class TransactionOutput implements Serializable {
    private static final Utils utils = new Utils();
    private static final Crypto crypto = new Crypto();
    public String id;
    public byte[] receiver; //also known as the new owner of these coins.
    public float amount; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    //Constructor
    public TransactionOutput(byte[] receiver, float amount, String parentTransactionId) {
        this.receiver = receiver;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = crypto.hash(utils.getStringFromBytes(receiver)+Float.toString(amount)+parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //System.out.println(publicKey);
        //System.out.println(receiver);
        //System.out.println(Arrays.toString(publicKey));
        //System.out.println(Arrays.toString(receiver));
        return (Arrays.toString(publicKey).equals(Arrays.toString(receiver)));
    }

}
