package com.fullsecurity.microservices;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.StoreDBCreator;

@SuppressWarnings("all")
public class SDVRequestProcessorCategories extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "categoriesMicroservice: ";

    private byte[] incoming;
    private String dbResult;

    public SDVRequestProcessorCategories(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        int clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        String returnValue;
        StringBuffer sb = new StringBuffer();
        String selectQuery = request.getAndRemoveArgument("|");
        if (selectQuery == null)
            processReturnValue("ERR:"+(-IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST)+"|"+nameUsedForOutputToLogger);
        else {
            // START MY WORK
            SQLiteDatabase database = new StoreDBCreator(mainActivity).getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            if (cursor != null) {
                boolean first = true;
                while (cursor.moveToNext()) {
                    if (first)
                        first = false;
                    else
                        sb.append("|");
                    sb.append(cursor.getString(0));
                }
                cursor.close();
            }
            returnValue = sb.toString();
            if (returnValue.length() == 0) returnValue = "<empty list>";
            debugLogOutput(returnValue, "exited normally");
            request.setPayload(0, returnValue.getBytes());
            // FINISHED MY WORK
            processReturnValue(request);
        }
    }
}