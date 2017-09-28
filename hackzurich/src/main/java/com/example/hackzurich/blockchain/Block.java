package com.example.hackzurich.blockchain;

import android.util.Base64;
import android.util.Log;
import android.util.StringBuilderPrinter;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Block implements Serializable{
    private static final String TAG="Block";

    Block(byte[] sender, byte[] receiver, long amount, byte[] prevBlockHash){
        this.senderHash=sender.clone();
        this.receiverHash=receiver.clone();
        this.amount=amount;
        this.prevBlockHash=prevBlockHash.clone();

    }

    public Block(String serialized){
        Log.d(TAG,serialized);
        String[] elements=serialized.split("-");

        Log.d(TAG,"We are printing all fields");
        Log.d(TAG,String.valueOf(elements.length));

        for(String s : elements) {
            Log.d(TAG, "Element "+s);
        }

        this.senderHash=Base64.decode(elements[0], Base64.DEFAULT);
        this.receiverHash=Base64.decode(elements[1], Base64.DEFAULT);

        this.amount= Long.parseLong(elements[2]);
        this.prevBlockHash=Base64.decode(elements[3], Base64.DEFAULT);
    }

    public byte[] getPrevBlockHash(){
        return this.prevBlockHash;
    }

    public String serialize(){
        StringBuilder sb=new StringBuilder();
        sb.append(Base64.encodeToString(this.senderHash, Base64.DEFAULT));
        sb.append("-");
        sb.append(Base64.encodeToString(this.receiverHash, Base64.DEFAULT));
        sb.append("-");
        sb.append(String.valueOf(amount));
        sb.append("-");
        sb.append(Base64.encodeToString(this.prevBlockHash, Base64.DEFAULT));
        return sb.toString();
    }


    public byte[] senderHash;
    public byte[] receiverHash;
    public long amount;
    public byte[] prevBlockHash;
    public byte[] signature;

    public byte[] getBlockHash(){
        MessageDigest md= null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(senderHash);
            md.update(receiverHash);
            md.update(ByteBuffer.allocate(8).putLong(amount).array());
            md.update(prevBlockHash);
            md.update(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return md.digest();
    }

    public boolean equals(Block obj) {
        return Arrays.equals(this.senderHash, obj.senderHash)
                && Arrays.equals(this.receiverHash, obj.receiverHash)
                && Arrays.equals(this.prevBlockHash, obj.prevBlockHash)
                && Arrays.equals(this.signature, obj.signature)
                && this.amount==obj.amount;
    }

    public ArrayList<byte[]> getElementsToSign(){
        ArrayList<byte[]> blocksForSignature=new ArrayList<>();
        blocksForSignature.add(this.senderHash);
        blocksForSignature.add(this.receiverHash);
        blocksForSignature.add(ByteBuffer.allocate(8).putLong(amount).array());
        blocksForSignature.add(this.prevBlockHash);
        return blocksForSignature;
    }

    public void sign(byte[] signature){
        this.signature=signature.clone();
    }
}
