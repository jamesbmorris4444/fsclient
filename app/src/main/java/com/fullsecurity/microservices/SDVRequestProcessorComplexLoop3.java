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
public class SDVRequestProcessorComplexLoop3 extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "complexLoop3Microservice: ";
    private static final String calledMicroServiceName4 = "complexloop4";
    private static final String calledMicroServiceName5 = "complexloop5";

    private byte[] incoming;
    private String dbResult;
    private int clientId;
    private SDVRequestProcessorComplexLoop4 ms4;
    private SDVRequestProcessorComplexLoop5 ms5;
    private int msIndex;
    private Payload save = null;

    public SDVRequestProcessorComplexLoop3(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        request.setMSName(calledMicroServiceName4);
        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName4);
        if (msIndex < 0) {
            debugLogOutput(incoming, "exit with ERR msIndex < 0");
            processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName4);
        } else {
            ms4 = (SDVRequestProcessorComplexLoop4) startMicroservice(msIndex);
            if (ms4 == null) {
                debugLogOutput(incoming, "exit with ERR startMicroservice returned null");
                processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName4);
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
                    try { ms4.startupSemaphore.acquire(); } catch (Exception e) {}
                    String sResult = sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe());
                    if (sResult.startsWith("ERR:"))
                        return errorPayload(sResult);
                    else {
                        save = new Payload(Base64.decode(sResult, Base64.DEFAULT));
                        return new Payload(save); // strips off all the payloads except the argument payload, will pick up the stripped-off payloads later
                    }
                })
                // serialized output Payload is returned here from the downstream microservice
                // The returned Payload hay have other outputs added
                .map((Payload request) -> {
                    if (request.getType() < 0) // pass on an error payload without processing
                        return "ERR:" + (-request.getType());
                    else {
                        request.setMSName(calledMicroServiceName5);
                        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName5);
                        if (msIndex < 0)
                            return "ERR:" + (-UNABLE_TO_FIND_MICROSERVICE);
                        else {
                            ms5 = (SDVRequestProcessorComplexLoop5) startMicroservice(msIndex);
                            // block until downstream microservice is listening, no need to release semaphore, because downstream microservice will not try to acquire it again
                            try { ms5.startupSemaphore.acquire(); } catch (Exception e) {}
                            return sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe());
                        }
                    }
                })
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
        } else if (save == null) {
            debugLogOutput(result, "exited with save==null");
            processReturnValue((String) null);
        } else {
            debugLogOutput(dbResult, "exited normally");
            Payload inflated = new Payload(Base64.decode(result, Base64.DEFAULT));
            inflated.join(save);
            inflated.addPayload(dbResult.getBytes());
            processReturnValue(inflated);
        }
    }

}
