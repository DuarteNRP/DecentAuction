package ssd.assignment.client;
import ssd.assignment.myBlockchain.Chain;

public class Menu {
    public Menu(){
        System.out.println("1. Genesis Block");
        System.out.println("2. Last Block");
        System.out.println("3. Send Coin");
        System.out.println("4. Create Block (mining)");
        System.out.println("5. Check Balance");
        System.out.println("6. Transaction History");
        System.out.println("7. Blockchain Explorer");
        System.out.println("8. Exit");

    }
    /*void getInput(int n){
        int selection = 0;
        while (selection != 20)
        {
            switch (selection)
            {
                case 1:
                    DoGenesisBlock();

                    break;
                case 2:
                    DoLastBlock();

                    break;

                case 3:
                    DoSendCoin();

                    break;

                case 4:

                    DoCreateBlock();

                    break;

                case 5:
                    DoGetBalance();

                    break;
                case 6:
                    DoGetTransactionHistory();


                    break;
                case 7:
                    DoShowBlockchain();

                    break;

                case 8:
                    DoExit();
                    break;
            }
    }*/
}
