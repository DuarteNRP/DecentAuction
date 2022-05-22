package myBlockchain;

import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.Random;

@Getter
@Setter
public class Stake{
    public static ArrayList<Staker> stakerList;
    public static void add(Staker s){
        stakerList.add(s);
    }
    //random choice, we need to choose a better way
    public static Staker chooseBlockValidator(){
        Random rand = new Random();
        return stakerList.get(rand.nextInt(stakerList.size()-1));
    }
}