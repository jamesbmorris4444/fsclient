package com.fullsecurity.microservices;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fullsecurity.common.Payload;
import com.fullsecurity.server.PurchaseDBCreator;

@SuppressWarnings("all")
public class SDVRequestProcessorShoppingCart extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "shoppingCartMicroservice: ";

    private byte[] incoming;

    public SDVRequestProcessorShoppingCart(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        int clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        String returnValue;
        StringBuffer sb = new StringBuffer();
        String selectQuery = request.getAndRemoveArgument("|");
        int totalCost = 0;
        int totalWeight = 0;
        if (selectQuery == null)
            processReturnValue("ERR:"+(-IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST)+"|"+nameUsedForOutputToLogger);
        else {
            // START MY WORK
            try {
                if (selectQuery.startsWith("DELETE")) {
                    PurchaseDBCreator purchaseDBCreator = new PurchaseDBCreator(mainActivity);
                    SQLiteDatabase database = purchaseDBCreator.getWritableDatabase();
                    String idsCSV = selectQuery.substring(7);
                    String rowid = purchaseDBCreator.ROWID;
                    int insertId = database.delete(purchaseDBCreator.TABLE_PURCHASES, rowid + " IN (" + idsCSV + ");", null);
                    returnValue = "DELETED:" + insertId;
                } else {
                    PurchaseDBCreator purchaseDBCreator = new PurchaseDBCreator(mainActivity);
                    SQLiteDatabase database = purchaseDBCreator.getWritableDatabase();
                    Cursor cursor = database.rawQuery(selectQuery, null);
                    if (cursor != null) {
                        boolean first = true;
                        while (cursor.moveToNext()) {
                            if (first)
                                first = false;
                            else
                                sb.append("|");
                            totalWeight += cursor.getInt(4);
                            totalCost += cursor.getInt(3);
                            sb.append(cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2) + "," +
                                      cursor.getString(3) + "," + cursor.getString(4) + "," + cursor.getString(5));
                        }
                        cursor.close();
                    }
                    returnValue = sb.toString();
                    if (returnValue.length() == 0)
                        returnValue = " ,No Items Purchased, ,-1,0,-1";
                    else {
                        returnValue += "| ,Subtotal Cost, ," + totalCost + "," + totalWeight + ",-1";
                        int tax = totalCost / 10;
                        totalCost += tax;
                        returnValue += "| ,Taxes, ," + tax + ",0,-1";
                        int shipping = totalWeight * 150;
                        totalCost += shipping;
                        returnValue += "| ,Shipping, ," + shipping + ",0,-1";
                        returnValue += "| ,Total Cost, ," + totalCost + ",0,-1";
                    }
                }
                debugLogOutput(returnValue, "exited normally");
                request.setPayload(0, returnValue.getBytes());
                processReturnValue(request);
            } catch (Exception e) {
                Log.d("JIM", "SDVRequestProcessorShoppingCart: DB EXCEPTION: " + e.toString());
                e.printStackTrace();
                debugLogOutput("EXCEPTION", "exited with db exception");
                processReturnValue("ERR:"+(-ERROR_IN_DATABASE_OPERATION));
            }
            // FINISHED MY WORK
        }
    }
}
