package com.fullsecurity.microservices;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;

@SuppressWarnings("all")
public class SDVRequestProcessorRemoteStore1 extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "remoteSearchMicroservice: ";

    private BitSet portAssignments;
    private Semaphore portSemaphore;
    private ArrayList<ServerState> serverStateList;
    private Semaphore sslSemaphore;
    private ArrayList<MSSettings> msMap;
    private MainActivity mainActivity;
    private int port;

    private byte[] incoming;
    private String dbResult;

    public SDVRequestProcessorRemoteStore1(BitSet portAssignments, ArrayList<ServerState> serverStateList, ArrayList<MSSettings> msMap,
                                           Semaphore sslSemaphore, Semaphore portSemaphore, int port, MainActivity mainActivity) {
        super(portAssignments, serverStateList, msMap, sslSemaphore, portSemaphore, port, mainActivity, nameUsedForOutputToLogger);

        this.portAssignments = portAssignments;
        this.portSemaphore = portSemaphore;
        this.serverStateList = serverStateList;
        this.sslSemaphore = sslSemaphore;
        this.msMap = msMap;
        this.mainActivity = mainActivity;
        this.port = port;
    }

    @Override
    protected void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        int clientId = request.getId();
        debugLogOutput(incoming, "entered");
        // START MY WORK
        String returnValue;
        String searchPattern = new String(incoming);
        StringBuffer sb = new StringBuffer();
        sb.append("Clothing,Dress,2995|");
        sb.append("Clothing,Pants,3499|");
        sb.append("Clothing,Shoes,12400|");
        sb.append("Clothing,Hat,1765|");
        sb.append("Clothing,Skirt,3459|");
        sb.append("Clothing,Socks,599|");
        sb.append("Clothing,Jacket,5789|");
        sb.append("Book,Ulysses,1265|");
        sb.append("Book,Fate is the Hunter,1045|");
        sb.append("Book,All Quiet on the Western Front,1879|");
        sb.append("Book,The Old Man and the Sea,2995|");
        sb.append("Book,The Sun Also Rises,1400|");
        sb.append("Book,No Man is an Island,899|");
        sb.append("Car,Ford,2560000|");
        sb.append("Car,Chevrolet,2340000|");
        sb.append("Car,Porsche,9500000|");
        sb.append("Car,BMW,7560000|");
        sb.append("Car,Dodge,4560000|");
        sb.append("Car,Mercedes,6750000|");
        sb.append("Appliance,Dishwasher,56500|");
        sb.append("Appliance,Disposal,12500|");
        sb.append("Appliance,Washer,34999|");
        sb.append("Appliance,Mixer,8995|");
        sb.append("Appliance,Blender,4500|");
        sb.append("Appliance,BowlSet,2999|");
        sb.append("Appliance,Faucet,1800");
        returnValue = sb.toString();
        if (returnValue.length() == 0) returnValue = "<empty list>";
        request.setPayload(0, returnValue.getBytes());
        // FINISHED MY WORK
        processReturnValue(request);
    }
}