package com.fullsecurity.microservices

import com.fullsecurity.common.Payload
import com.fullsecurity.common.Utilities

//class SDVRequestProcessorEncryptionLoop(port: Int) : MSCommunicationsWrapper(port, nameUsedForOutputToLogger) {
//
//    private val encryptionTestSend =
//            "\nThis be the verse you grave for me;\n" +
//                    "Here he lies where he longed to be,\n" +
//                    "Home is the sailor,\n" +
//                    "Home from the sea;\n" +
//                    "And the hunter,\n" +
//                    "Home from the hill.\n"
//
//    override fun setOutputPayloads(request: Payload) {
//        request.setPayload(0, Utilities.concatenateByteArrays(request.getPayload(0), encryptionTestSend.toByteArray()))
//        processReturnValue(request)
//    }
//
//    companion object {
//
//        private val nameUsedForOutputToLogger = "encryptionLoopMicroservice: "
//    }
//}
