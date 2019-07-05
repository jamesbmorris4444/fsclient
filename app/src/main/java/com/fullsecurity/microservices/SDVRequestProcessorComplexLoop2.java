package com.fullsecurity.microservices;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fullsecurity.common.Payload;

public class SDVRequestProcessorComplexLoop2 extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "complexLoop2Microservice: ";

    private byte[] incoming;
    private String dbResult;

    public SDVRequestProcessorComplexLoop2(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        debugLogOutput(incoming, "entered");
        String selectQuery = request.getAndRemoveArgument("|");
        if (selectQuery == null)
            processReturnValue("ERR:"+(-IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST)+"|"+nameUsedForOutputToLogger);
        else {
            // START MY WORK
            SQLiteDatabase database = mainActivity.baseballDBCreator.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);
            dbResult = "<empty list>";
            if (cursor != null) {
                StringBuffer sb = new StringBuffer();
                boolean first = true;
                while (cursor.moveToNext()) {
                    if (first)
                        first = false;
                    else
                        sb.append("|");
                    sb.append(cursor.getString(0) + " " + cursor.getString(1));
                }
                ;
                dbResult = sb.toString();
                if (dbResult.length() == 0) dbResult = "<empty list>";
                cursor.close();
            }
            // FINISHED MY WORK
            debugLogOutput(dbResult, "exited normally");
            request.addPayload(dbResult.getBytes());
            processReturnValue(request);
        }
    }
}
