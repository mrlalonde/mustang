package com.github.mustang.api

import java.lang.UnsupportedOperationException
import java.util.concurrent.CompletableFuture

object NoopService : WorkflowService {
    override fun call(request: WorkflowServiceRequest): CompletableFuture<WorkflowServiceResponse> {
        throw UnsupportedOperationException()
    }

}

class ServiceRegistry(private val map: Map<String, WorkflowService>) {
    fun getServiceByName(name: String): WorkflowService {
        if (!map.containsKey(name)) throw IllegalArgumentException("name '$name' doesn't have a matching service!")

        return map.getOrDefault(name, NoopService)
    }
}