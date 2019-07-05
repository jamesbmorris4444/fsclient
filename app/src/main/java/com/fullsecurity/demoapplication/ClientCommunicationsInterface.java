package com.fullsecurity.demoapplication;

public interface ClientCommunicationsInterface {
    void processErrorResponseFromMicroservice(String errorMessage);
    void processNormalResponseFromMicroservice(String result);
}
