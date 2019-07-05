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
public class SDVRequestProcessorComplexLoop extends MSCommunicationsWrapper {

    private static final String nameUsedForOutputToLogger = "complexLoopMicroservice: ";
    private static final String calledMicroServiceName1 = "complexloop1";
    private static final String calledMicroServiceName3 = "complexloop3";
    private static final String calledMicroServiceName6 = "complexloop6";

    private byte[] incoming;
    private String dbResult;
    private int clientId;
    private SDVRequestProcessorComplexLoop1 ms1;
    private SDVRequestProcessorComplexLoop3 ms3;
    private SDVRequestProcessorComplexLoop6 ms6;
    private int msIndex;
    private Payload save1 = null;
    private Payload save2 = null;

    public SDVRequestProcessorComplexLoop(int port) { super(port, nameUsedForOutputToLogger); }

    @Override
    public void setOutputPayloads(Payload request) {
        // request is the input Payload
        incoming = request.getPayload(0);
        clientId = request.getClientId();
        debugLogOutput(incoming, "entered");
        request.setMSName(calledMicroServiceName1);
        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName1);
        if (msIndex < 0) {
            debugLogOutput(incoming, "exit with ERR msIndex < 0");
            processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName1);
        } else {
            ms1 = (SDVRequestProcessorComplexLoop1) startMicroservice(msIndex);
            if (ms1 == null) {
                debugLogOutput(incoming, "exit with ERR startMicroservice returned null");
                processReturnValue("ERR:" + (-UNABLE_TO_FIND_MICROSERVICE) + "|" + calledMicroServiceName1);
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
                    try { ms1.startupSemaphore.acquire(); } catch (Exception e) {}
                    String sResult = sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe());
                    if (sResult.startsWith("ERR:"))
                        return errorPayload(sResult);
                    else {
                        save1 = new Payload(Base64.decode(sResult, Base64.DEFAULT));
                        return new Payload(save1); // strips off all the payloads except the argument payload, will pick up the stripped-off payloads later
                    }
                })
                // serialized output Payload is returned here from the downstream microservice
                // The returned Payload may have other outputs added
                .map((Payload request) -> {
                    if (request.getType() < 0) // pass on an error payload without processing
                        return request;
                    else {
                        request.setMSName(calledMicroServiceName3);
                        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName3);
                        if (msIndex < 0)
                            return new Payload.PayloadBuilder()
                                .setErrorValueforErrorPayload(UNABLE_TO_FIND_MICROSERVICE)
                                .build();
                        else {
                            ms3 = (SDVRequestProcessorComplexLoop3) startMicroservice(msIndex);
                            // block until downstream microservice is listening, no need to release semaphore, because downstream microservice will not try to acquire it again
                            try { ms3.startupSemaphore.acquire(); } catch (Exception e) {}
                            String sResult = sendToMicroservice(request, clientId, msMap.get(msIndex).isReturnToMe());
                            if (sResult.startsWith("ERR:"))
                                return errorPayload(sResult);
                            else {
                                save2 = new Payload(Base64.decode(sResult, Base64.DEFAULT));
                                return new Payload(save2); // strips off all the payloads except the argument payload, will pick up the stripped-off payloads later
                            }
                        }
                    }
                })
                .map((Payload request) -> {
                    if (request.getType() < 0) // pass on an error payload without processing
                        return "ERR:" + (-request.getType());
                    else {
                        request.setMSName(calledMicroServiceName6);
                        msIndex = mapFromMSNameToMSIndex(msMap, calledMicroServiceName6);
                        if (msIndex < 0)
                            return "ERR:" + (-UNABLE_TO_FIND_MICROSERVICE);
                        else {
                            ms6 = (SDVRequestProcessorComplexLoop6) startMicroservice(msIndex);
                            // block until downstream microservice is listening, no need to release semaphore, because downstream microservice will not try to acquire it again
                            try { ms6.startupSemaphore.acquire(); } catch (Exception e) {}
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
        } else if (save1 == null || save2 == null) {
            debugLogOutput(result, "exited with save1=null|save2=null");
            processReturnValue((String) null);
        } else {
            Payload inflated = new Payload(Base64.decode(result, Base64.DEFAULT));
            debugLogOutput(dbResult, "exited normally");
            inflated.join(save2);
            inflated.join(save1);
            inflated.addPayload(dbResult.getBytes()); // add my output to the output that is accumulating
            processReturnValue(inflated);
        }
    }

}

