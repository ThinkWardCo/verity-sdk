package com.evernym.verity.sdk.handlers;

import com.evernym.verity.sdk.exceptions.WalletException;
import com.evernym.verity.sdk.utils.Context;
import org.json.JSONObject;

import com.evernym.verity.sdk.utils.Util;

import java.util.ArrayList;

/**
 * Stores an array of message handlers that are used when receiving an inbound message
 */
public class Handlers {
    private ArrayList<MessageHandler> messageHandlers = new ArrayList<MessageHandler>();
    private MessageHandler defaultHandler;
    private MessageHandler problemReportHandler;

    /**
     * Adds a MessageHandler for a message type to the list if current message handlers
     * @param messageType the type of message to be handled
     * @param messageHandler the handler function itself
     */
    public void addHandler(String messageType, MessageHandler.Handler messageHandler) {
        messageHandlers.add(new MessageHandler(messageType, messageHandler));
    }

    /**
     * Adds a MessageHandler for a message type and status to the list if current message handlers
     * @param messageType the type of message to be handled
     * @param messageStatus the status of the message to be handled
     * @param messageHandler the handler function itself
     */
    public void addHandler(String messageType, Integer messageStatus, MessageHandler.Handler messageHandler) {
        messageHandlers.add(new MessageHandler(messageType, messageStatus, messageHandler));
    }

    /**
     * Adds a handler that is called for all problem report messages not handled directly.
     * @param messageHandler the function that will be called
     */
    public void addProblemReportHandler(MessageHandler.Handler messageHandler) {
        problemReportHandler = new MessageHandler(null, messageHandler);
    }

    /**
     * Adds a handler for all message types not handled by other message handlers
     * @param messageHandler the function that will be called
     */
    public void addDefaultHandler(MessageHandler.Handler messageHandler) {
        defaultHandler = new MessageHandler(null, messageHandler);
    }

    /**
     * Calls all of the handlers that support handling of this particular message type and message status
     * @param context an instance of Context configured with the results of the provision_sdk.py script
     * @param rawMessage the raw bytes received from Verity
     * @throws WalletException when there are issues with encryption and decryption
     */
    public void handleMessage(Context context, byte[] rawMessage) throws WalletException {
        JSONObject message = Util.unpackMessage(context, rawMessage);
        boolean handled = false;
        for(MessageHandler messageHandler: messageHandlers) {
            if(messageHandler.handles(message)) {
                messageHandler.handle(message);
                handled = true;
            }
        }
        if(!handled) {
            if(isProblemReport(message.getString("@type")) && problemReportHandler != null) {
                problemReportHandler.handle(message);
            } else if(defaultHandler != null) {
                defaultHandler.handle(message);
            }
        }
    }

    static boolean isProblemReport(String messageType) {
        return messageType.split("/")[3].equals("problem-report");
    }
    
}