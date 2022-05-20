package ssd.assignment.myBlockchain;

import java.security.PublicKey;
import ssd.assignment.config.Utils;
import ssd.assignment.crypto.Crypto;

public class TransactionOutput {
    private static final Utils utils = new Utils();
    private static final Crypto cripto = new Crypto();
    public String id;
    public PublicKey receiver; //also known as the new owner of these coins.
    public float amount; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    //Constructor
    public TransactionOutput(PublicKey receiver, float amount, String parentTransactionId) {
        this.receiver = receiver;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = cripto.hash(utils.getStringFromKey(receiver)+Float.toString(amount)+parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == receiver);
    }

}
