package com.github.mustang.stubs

import com.github.mustang.api.*
import java.util.concurrent.CompletableFuture

class QueryService : WorkflowService {

    override fun call(request: WorkflowServiceRequest): CompletableFuture<WorkflowServiceResponse> {
        println("IN Query Service")
        return CompletableFuture.supplyAsync { WorkflowServiceResponse(listOf(Output("query", "1"))) }
    }
}

class SqlService : WorkflowService {
    override fun call(request: WorkflowServiceRequest): CompletableFuture<WorkflowServiceResponse> {
        println("In SqlService")
        return CompletableFuture.supplyAsync { WorkflowServiceResponse(listOf(Output("sql-service", "2"))) }
    }
}

val stubRegistry = ServiceRegistry(
    mapOf(
        "queryService" to QueryService(),
        "sql-service" to SqlService()
    )
)