package com.example.hackzurich.blockchain;

import android.util.Log;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class BlockChain implements Serializable{
    private static final String TAG="BlockChain";

    transient Principle myPrinciple;
    PublicKey myPartner;
    String name;

    private LinkedList<Block> blocks;
    private byte[] lastBlockHash;

    public BlockChain(Principle me, PublicKey partner, String name){
        myPartner=partner;
        myPrinciple=me;
        blocks=new LinkedList<Block>();
        lastBlockHash=ByteBuffer.allocate(8).putLong(42).array();
        this.name=name;
    }

    public void setPrinciple(Principle principle){
        this.myPrinciple=principle;
    }

    public String getName(){
        return name;
    }

    public Block createBlock(long amount){
        Block newBlock=new Block(getPublicKeyHash(myPrinciple.getPublicKey()), getPublicKeyHash(myPartner), amount, lastBlockHash);
        return newBlock;
    }

    public boolean saveBlock(Block block, byte[] receivedSignature) throws Exception{
        Signature signature = Signature.getInstance("SHA1withRSA", "BC");
        signature.initVerify(myPartner);
        ArrayList<byte[]> fields=block.getElementsToSign();
        for(byte[] field:fields)
            signature.update(field);

        boolean result = signature.verify(receivedSignature);
        if(result){
            block.sign(receivedSignature);
            blocks.add(block);
            lastBlockHash=block.getBlockHash();
        }
        return result;
    }

    public byte[] singAndAdd(Block block) throws Exception{
        if(Arrays.equals(block.getPrevBlockHash(), lastBlockHash) || true){
            byte[] signature=myPrinciple.signFields(block.getElementsToSign());
            block.sign(signature);
            this.blocks.add(block);
            lastBlockHash=block.getBlockHash();

            return signature;
        }else{
            Log.e(TAG,"NO, previous hash doesn't match");
        }

        return null;
    }

    public void printCompleteChain(){
        Log.d(TAG, "Printing all blocks");
        for (Block b : blocks){
            Log.d(TAG, b.serialize());
        }
    }

    private byte[] getPublicKeyHash(PublicKey pubKey){
        MessageDigest md= null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(pubKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md.digest();
    }

    long getBalance(){
        byte[] myHash=getPublicKeyHash(myPrinciple.getPublicKey());
        long balance=0;
        for (Block block:blocks){
            if(Arrays.equals(block.senderHash, myHash)){
                balance-=block.amount;
            }else {
                balance+=block.amount;
            }
        }

        return balance;
    }
}

