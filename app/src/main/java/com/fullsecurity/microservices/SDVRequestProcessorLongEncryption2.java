package com.fullsecurity.microservices;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.fullsecurity.common.Payload;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("all")
public class SDVRequestProcessorLongEncryption2 extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "longEncryption2Microservice: ";
    private static final String calledMicroServiceName = "longencryption3";

    private byte[] incoming;
    private String dbResult;
    private int clientId;
    private SDVRequestProcessorLongEncryption3 ms;
    private int msIndex;

    public SDVRequestProcessorLongEncryption2(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        request.setMSName(calledMicroServiceName);
        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName);
        if (msIndex < 0) {
            debugLogOutput(incoming, "exit with ERR msINdex < 0");
            processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName);
        } else {
            ms = (SDVRequestProcessorLongEncryption3) startMicroservice(msIndex);
            if (ms == null) {
                debugLogOutput(incoming, "exit with ERR startMicroservice returned null");
                processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName);
            } else {
                // get incoming parameters
                String selectQuery = request.getAndRemoveArgument("|");
                if (selectQuery == null)
                    processReturnValue("ERR:" + (-IMPROPER_EXPECTED_ARGUMENT_IN_REQUEST) + "|" + nameUsedForOutputToLogger);
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
                    initializeObservable(Observable.just(request));
                }
            }
        }
    }

    private void initializeObservable(Observable<Payload> observable) {
        observable
                .observeOn(Schedulers.io())
                .map((Payload request) -> {
                    // block until downstream microservice is listening, no need to release semaphore, because downstream microservice will not try to acquire it again
                    try { ms.startupSemaphore.acquire(); } catch (Exception e) {}
                    return sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe());
                })
                // serialized output Payload is returned here from the downstream microservice
                // The returned Payload may have other outputs added
                .timeout(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNormalResponse, throwable -> { handleException(throwable, incoming); });
    }

    private void handleNormalResponse(String result) {
        if (result.equals("NOACTION")) {
            debugLogOutput(result, "exited with NOACTION");
            processReturnValue(result);
        } else if (result.startsWith("ERR:")) {
            debugLogOutput(result, "exited with ERR");
            processReturnValue(result);
        } else {
            debugLogOutput(dbResult, "exited normally");
            Payload inflated = new Payload(Base64.decode(result, Base64.DEFAULT));
            inflated.addPayload(dbResult.getBytes());
            processReturnValue(inflated);
        }
    }

}
