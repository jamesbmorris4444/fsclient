package com.fullsecurity.microservices;

import com.fullsecurity.common.Payload;
import com.fullsecurity.common.Utilities;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;

@SuppressWarnings("all")
public class SDVRequestProcessorEncryptionLoop extends MSCommunicationsWrapper {

    private final String encryptionTestSend =
            "\nThis be the verse you grave for me;\n" +
                    "Here he lies where he longed to be,\n" +
                    "Home is the sailor,\n" +
                    "Home from the sea;\n" +
                    "And the hunter,\n" +
                    "Home from the hill.\n";

    private static final String nameUsedForOutputToLogger = "encryptionLoopMicroservice: ";

    private BitSet portAssignments;
    private Semaphore portSemaphore;
    private ArrayList<ServerState> serverStateList;
    private Semaphore sslSemaphore;
    private ArrayList<MSSettings> msMap;
    private MainActivity mainActivity;
    private int port;

    private byte[] incoming;
    private String dbResult;
    private  int clientId;

    public SDVRequestProcessorEncryptionLoop(int port) {

        super(port, nameUsedForOutputToLogger);
        this.portAssignments = portAssignments;
        this.portSemaphore = portSemaphore;
        this.serverStateList = serverStateList;
        this.sslSemaphore = sslSemaphore;
        this.msMap = msMap;
        this.mainActivity = mainActivity;
        this.port = port;
    }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        // START MY WORK
        request.setPayload(0, Utilities.concatenateByteArrays(incoming, encryptionTestSend.getBytes()));
        // FINISHED MY WORK
        processReturnValue(request);
    }
}
