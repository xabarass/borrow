package com.example.hackzurich;

import com.example.hackzurich.blockchain.BlockChainCollection;

/**
 * Created by milan on 9/16/17.
 */

// This should be abstract class for all communication, nfc, qr web etc should all expose following interface.
public interface CommunicationLayer {
    public static final String TYPE_MESSAGE = "com.hackzurick.message.commtype";

    public static final int SENDER = 0;
    public static final int RECEIVER = 1;

    void sendString(String text);

    void errorOccured();

    void finishedTransaction();

    BlockChainCollection getBCCollection();
}
