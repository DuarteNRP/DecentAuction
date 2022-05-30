package config;

public class Constraints {

    /*
    Sets the amount of zeros at the start of the hash when mining a block
     */
    public static final int MINING_DIFFICULTY = 2;

    public static final float COIN_REWARD = 0.01f;
    public static final float TRANSACTION_FEE = 0.01f;

    /*
    Sets the max amount of transactions per block
     */
    public static final int MAX_TRANSACTIONS_PER_BLOCK = 10;
    /*
    kademlia constraints
     */
    public static final int ALPHA = 3;
    public static final int ID_LENGTH = 5;//160
    public static final int K = 3;//K=20
    //milliseconds
    public static final int T_EXPIRE = 86400000;
    public static final int T_REFRESH = 3600000;
    public static final int T_REPLICATE = 3600000;
    public static final int T_REPUBLISH = 86400000;


}
