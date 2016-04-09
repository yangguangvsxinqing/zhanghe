package com.fineos.fileexplorer.operations;

/**
 * Created by acmllaugh on 14-12-12.
 */
public class CancelMessage {

    private static CancelMessage msg = null;

    private CancelMessage() {

    }

    public static synchronized CancelMessage getInstance() {
        if (msg == null) {
            msg = new CancelMessage();
        }
        return msg;
    }
}
