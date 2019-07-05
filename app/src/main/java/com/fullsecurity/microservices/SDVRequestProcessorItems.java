package com.fullsecurity.microservices;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.MSSettings;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.shared.MainActivity;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.Semaphore;

@SuppressWarnings("all")
public class SDVRequestProcessorItems extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "categoriesMicroservice: ";

    private BitSet portAssignments;
    private Semaphore portSemaphore;
    private ArrayList<ServerState> serverStateList;
    private Semaphore sslSemaphore;
    private ArrayList<MSSettings> msMap;
    private MainActivity mainActivity;
    private int port;

    private byte[] incoming;
    private String dbResult;

    public SDVRequestProcessorItems(BitSet portAssignments, ArrayList<ServerState> serverStateList, ArrayList<MSSettings> msMap,
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
        ArrayList<String> categories = new ArrayList<>();
        String returnValue;
        StringBuffer sb = new StringBuffer();
        String selectQuery = request.getAndRemoveArgument("|");
        if (selectQuery == null)
            processReturnValue("ERR:"+(-IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST)+"|"+nameUsedForOutputToLogger);
        else {
            // START MY WORK
            try {
                if (selectQuery.startsWith("INSERT")) {
                    SQLiteDatabase database = mainActivity.purchaseDBCreator.getWritableDatabase();
                    database.rawQuery(selectQuery, null);
                    returnValue = "STORED";
                } else {
                    SQLiteDatabase database = mainActivity.storeDBCreator.getWritableDatabase();
                    Cursor cursor = database.rawQuery(selectQuery, null);
                    if (cursor != null) {
                        boolean first = true;
                        while (cursor.moveToNext()) {
                            if (first)
                                first = false;
                            else
                                sb.append("|");
                            sb.append(cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2) + "," + cursor.getString(3));
                        }
                        cursor.close();
                    }
                    returnValue = sb.toString();
                    if (returnValue.length() == 0) returnValue = "<empty list>";
                }
                debugLogOutput(returnValue, "exited normally");
                request.setPayload(0, returnValue.getBytes());
                processReturnValue(request);
            } catch (Exception e) {
                Log.d("JIM", "SDVRequestProcessorItems: DB EXCEPTION: " + e.toString());
                e.printStackTrace();
                debugLogOutput("EXCEPTION", "exited with db exception");
                processReturnValue("ERR:"+(-ERROR_IN_DATABASE_OPERATION));
            }
            // FINISHED MY WORK
        }
    }
}
