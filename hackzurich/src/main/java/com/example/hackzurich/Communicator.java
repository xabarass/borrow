package com.example.hackzurich;

/**
 * Created by milan on 9/16/17.
 */

public abstract class Communicator {
    int lastId;
    CommunicationLayer comLayer;

    protected enum State {
        /**
         * Show message, go to RECV when ACK is seen
         */
        SEND,
        /**
         * Show RECV, go to ACK when answer is seen
         */
        RECV,
        /**
         * Show ACK, go to SEND when RECV is seen
         */
        ACK,
        /**
         * Finished, stop
         */
        DONE
    }

    protected State state;

    protected Runnable sendCallback;
    protected Runnable recvCallback;
    protected Runnable ackCallback;

    protected String receivedMessage = "";
    protected String messageToSend = "";

    public Communicator(CommunicationLayer comLayer) {
        super();
        this.comLayer = comLayer;
    }

    public void onStart() {
        switch (state) {
            case SEND:
                sendMessage(messageToSend);
                break;
            case RECV:
                sendRecv();
                break;
            case ACK:
                sendAck();
                break;
            case DONE:
                break;
        }
    }

    public void onMessageReceived(String text) {
        receivedMessage = text;
        switch (state) {
            case SEND:
                if (isAck(text)) {
                    state = State.RECV;
                    sendCallback.run();
                    sendRecv();
                }
                break;
            case RECV:
                if (!isAck(text) && !isRecv(text)) {
                    state = State.ACK;
                    recvCallback.run();
                    sendAck();
                }
                break;
            case ACK:
                if (isRecv(text)) {
                    state = State.SEND;
                    ackCallback.run();

                    if (state != State.DONE) {
                        sendMessage(messageToSend);
                    }
                }
                break;
            case DONE:
                finished();
                break;
        }
    }

    public void onEnd() {
        // ??
    }

    public void errorOccured() {
        comLayer.errorOccured();

        onEnd();
    }

    private void finished() {
        comLayer.finishedTransaction();

        onEnd();// TODO do something here?
    }

    private static boolean isAck(String message) {
        return message.equals("ack");
    }

    private static boolean isRecv(String message) {
        return message.equals("recv");
    }

    private void sendAck() {
        //TODO: Send ack to other device, special packet for ack
        comLayer.sendString("ack");
    }

    private void sendRecv() {
        //TODO: Send recv to other device, special packet for recv
        comLayer.sendString("recv");
    }

    private void sendMessage(String text) {
        //TODO: Generate header id stuff like that... If no ack has been received for previous throw exception and stuff like that
        comLayer.sendString(text);
    }
}
