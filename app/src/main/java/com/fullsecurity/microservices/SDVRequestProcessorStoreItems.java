package com.fullsecurity.microservices;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.PurchaseDBCreator;
import com.fullsecurity.server.StoreDBCreator;

@SuppressWarnings("all")
public class SDVRequestProcessorStoreItems extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "storeItemMicroservice: ";

    private byte[] incoming;

    public SDVRequestProcessorStoreItems(int port) { super(port, nameUsedForOutputToLogger); }

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
            try {
                if (selectQuery.startsWith("INSERT")) {
                    PurchaseDBCreator purchaseDBCreator = new PurchaseDBCreator(mainActivity);
                    SQLiteDatabase database = purchaseDBCreator.getWritableDatabase();
                    String[] splitLine = selectQuery.split("[:]");
                    ContentValues values = new ContentValues();
                    String[] allPurchaseColumns = {
                            PurchaseDBCreator.ROWID,
                            PurchaseDBCreator.NAME,
                            PurchaseDBCreator.DESCRIPTION,
                            PurchaseDBCreator.STATUS,
                            PurchaseDBCreator.COST,
                            PurchaseDBCreator.WEIGHT
                    };
                    for (int i = 1; i <= 5; i++) values.put(allPurchaseColumns[i], splitLine[i]);
                    long insertId = database.insert(purchaseDBCreator.TABLE_PURCHASES, null, values);
                    returnValue = "STORED:" + insertId;
                } else {
                    StoreDBCreator storeDBCreator = new StoreDBCreator(mainActivity);
                    SQLiteDatabase database = storeDBCreator.getWritableDatabase();
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
