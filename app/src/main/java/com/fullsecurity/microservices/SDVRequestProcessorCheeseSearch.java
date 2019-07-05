package com.fullsecurity.microservices;


import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.Cheeses;

@SuppressWarnings("all")
public class SDVRequestProcessorCheeseSearch extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "cheeseSearchMicroservice: ";

    private byte[] incoming;
    private Cheeses cheeses;

    public SDVRequestProcessorCheeseSearch(int port) {
        super(port, nameUsedForOutputToLogger);
        cheeses = new Cheeses();
    }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        int clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        // START MY WORK
        String returnValue;
        String searchPattern = new String(incoming);
        StringBuffer sb = new StringBuffer();
        Log.d("JIM","REMOVE="+cheeses.cheeses.toString());
        for (int i = 0; i < cheeses.cheeses.size(); i++) {
            if (cheeses.cheeses.get(i).contains(searchPattern) || searchPattern.equals("ALL")) {
                sb.append(cheeses.cheeses.get(i));
                if (i < cheeses.cheeses.size() - 1) sb.append("|");
            }
        }
        returnValue = sb.toString();
        if (returnValue.length() == 0) returnValue = "<empty list>";
        request.setPayload(0, returnValue.getBytes());
        // FINISHED MY WORK
        processReturnValue(request);
    }
}