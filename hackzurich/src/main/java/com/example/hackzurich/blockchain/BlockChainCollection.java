package com.example.hackzurich.blockchain;

import android.util.Base64;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlockChainCollection implements Serializable {
    private HashMap<PublicKey, BlockChain> collection;
    private transient Principle myPrinciple;

    private ArrayList<String> randomNames;

    public BlockChainCollection(Principle myPrinciple){
        randomNames=new ArrayList<String>();
        randomNames.add("John");
        randomNames.add("Michael");
        randomNames.add("David");
        randomNames.add("Steve");
        randomNames.add("Hubert");
        randomNames.add("Sven");
        randomNames.add("Eddy");
        randomNames.add("Milan");

        collection=new HashMap<PublicKey, BlockChain>();
        this.myPrinciple=myPrinciple;
    }

    public void setPrinciple(Principle myPrinciple){
        this.myPrinciple=myPrinciple;
        for (Map.Entry<PublicKey, BlockChain> entry : collection.entrySet())
        {
            entry.getValue().setPrinciple(myPrinciple);
        }
    }

    public BlockChain getPartner(PublicKey pubKey){
        if(collection.containsKey(pubKey)){
            return collection.get(pubKey);
        }else{
            int index= pubKey.hashCode()%randomNames.size();
            BlockChain bc=new BlockChain(myPrinciple, pubKey, randomNames.get(Math.abs(index) ));
            collection.put(pubKey, bc);
            return bc;
        }
    }

    public String getPublicKeyStrnig(){
        String base64 = Base64.encodeToString(myPrinciple.getPublicKey().getEncoded(), Base64.DEFAULT);
        return base64;
    }

    public List<Balance> getBalanceList(){
        ArrayList<Balance> result=new ArrayList<>();

        for (Map.Entry<PublicKey, BlockChain> entry : collection.entrySet())
        {
            long balance=entry.getValue().getBalance();
            String name=entry.getValue().getName();
            result.add(new Balance(balance, name));
        }

        return result;
    }

    public class Balance{
        public Balance(long amount, String name){
            this.name=name;
            this.amount=amount;
        }

        public String name;
        public long amount;

    }

}
