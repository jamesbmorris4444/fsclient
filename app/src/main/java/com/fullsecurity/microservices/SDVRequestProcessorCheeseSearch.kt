package com.fullsecurity.microservices


import com.fullsecurity.common.Payload
import com.fullsecurity.server.Cheeses

class SDVRequestProcessorCheeseSearch(port: Int) : MSCommunicationsWrapper(port, nameUsedForOutputToLogger) {

    private val cheeses: Cheeses

    init { cheeses = Cheeses() }

    companion object { private val nameUsedForOutputToLogger = "cheeseSearchMicroservice: " }

    override fun setOutputPayloads(request: Payload) {
        // request is the input Payload
        val incoming = String(request.getPayload(0))
        var returnValue: String = ""
        val sTemp = cheeses.cheeses.filter { it.contains(incoming) || incoming == "ALL" }
        when (sTemp.size) {
            0 -> returnValue = "<empty list>"
            else -> returnValue = sTemp.reduce { s, t -> s + "|" + t }
        }
        request.setPayload(0, returnValue.toByteArray())
        processReturnValue(request)
    }

}