package com.example.hackzurich;

/**
 * Created by milan on 9/16/17.
 */

import android.util.Base64;
import android.util.Log;

import com.example.hackzurich.blockchain.Block;
import com.example.hackzurich.blockchain.BlockChain;
import com.example.hackzurich.blockchain.BlockChainCollection;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implement receiver side of the protocol:
 */

public class ReceiverCommunicator extends Communicator {

    private static final String TAG="ReceiveCommunicator";

    private String publicKey;
    private String signedTransaction;

    private String remoteKey = "";
    private String transaction = "";

    private BlockChainCollection blockChainCollection;
    private BlockChain blockChain;

    ReceiverCommunicator(CommunicationLayer comLayer) {
        super(comLayer);
        state = State.RECV;
        blockChainCollection=comLayer.getBCCollection();
        publicKey=blockChainCollection.getPublicKeyStrnig();

        recvCallback = new Runnable() {
            @Override
            public void run() {
                receiveKey();
            }
        };
    }

    private void receiveKey() {
        Log.d(TAG, "Receivde key");
        remoteKey = receivedMessage;
        try {
            KeyFactory kf= KeyFactory.getInstance("RSA", "BC");
            PublicKey pubKey=kf.generatePublic(new X509EncodedKeySpec(Base64.decode(remoteKey, Base64.DEFAULT)));
            blockChain=blockChainCollection.getPartner(pubKey);
            Log.d(TAG, "Got my partner");
        }catch (Exception e){
            e.printStackTrace();
        }

        ackCallback = new Runnable() {
            @Override
            public void run() {
                ackKey();
            }
        };
    }

    private void ackKey() {

        messageToSend = publicKey;

        sendCallback = new Runnable() {
            @Override
            public void run() {
                sendKey();
            }
        };
    }

    private void sendKey() {
        // TODO do something?
        Log.d(TAG, "Sending my public key");
        recvCallback = new Runnable() {
            @Override
            public void run() {
                receiveTransaction();
            }
        };
    }

    private void receiveTransaction() {
        Log.d(TAG, "I got a transaction that i have to verify");
        transaction = receivedMessage;
        // TODO validate, sign
        try {
            Log.d(TAG, "Parsing block");
            Block newBlock=new Block(transaction);
            byte[] signature = blockChain.singAndAdd(new Block(transaction));
            signedTransaction = Base64.encodeToString(signature, Base64.DEFAULT);
            Log.d(TAG, "signed and addded");

        }catch (Exception e){
            e.printStackTrace();
        }

        ackCallback = new Runnable() {
            @Override
            public void run() {
                ackTransaction();
            }
        };
    }

    private void ackTransaction() {
        messageToSend = signedTransaction;

        sendCallback = new Runnable() {
            @Override
            public void run() {
                sendSignedTransaction();
            }
        };
    }

    private void sendSignedTransaction() {
        // TODO do something here?
        // TODO commit to storage?
        // TODO some clean return thing
    }
}