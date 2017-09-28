package com.example.hackzurich;

import android.util.Base64;
import android.util.Log;

import com.example.hackzurich.blockchain.Block;
import com.example.hackzurich.blockchain.BlockChain;
import com.example.hackzurich.blockchain.BlockChainCollection;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Implement protocol for sender side:
 */
public class SenderCommunicator extends Communicator {
    private static final String TAG="SenderCommunicator";

    private String publicKey;

    private String remoteKey = "";
    private String signedTransaction = "";
    private long amount;

    private BlockChainCollection blockChainCollection;
    private BlockChain blockChain;
    private Block blockToValidate;

    SenderCommunicator(CommunicationLayer comLayer, long amount) {
        super(comLayer);
        this.amount = amount;
        state = State.SEND;
        blockChainCollection=comLayer.getBCCollection();
        publicKey=blockChainCollection.getPublicKeyStrnig();
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
        Log.d(TAG, "Send key!");
        recvCallback = new Runnable() {
            @Override
            public void run() {
                receiveKey();
            }
        };
    }

    private void receiveKey() {
        remoteKey = receivedMessage;
        // TODO find the right blockchain, validate, etc
        Log.d(TAG, "Received key, decoding it and");
        try {
            KeyFactory kf= KeyFactory.getInstance("RSA", "BC");
            PublicKey pubKey=kf.generatePublic(new X509EncodedKeySpec(Base64.decode(remoteKey, Base64.DEFAULT)));
            blockChain=blockChainCollection.getPartner(pubKey);
            Log.d(TAG, "Got blockchain from a partner");
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
        Log.d(TAG, "Creating block with required amount, serializing it");
        blockToValidate=blockChain.createBlock(amount);
        messageToSend=blockToValidate.serialize();

        sendCallback = new Runnable() {
            @Override
            public void run() {
                sendTransactionData();
            }
        };
    }

    private void sendTransactionData() {
        // TODO do something here?

        recvCallback = new Runnable() {
            @Override
            public void run() {
                receiveSignedTransaction();
            }
        };
    }

    private void receiveSignedTransaction() {
        Log.d(TAG, "Got transaction signature back, have to check if its valid");
        signedTransaction = receivedMessage;
        // TODO validate, commit to storage
        try {
            if(!blockChain.saveBlock(blockToValidate, Base64.decode(signedTransaction, Base64.DEFAULT))){
                Log.e(TAG,"Block is not valid! Cannot add it");
            }
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
        // TODO do something here?
        // TODO some clean return thing
        state = State.DONE;
    }
}
