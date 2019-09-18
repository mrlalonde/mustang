package com.github.mustang.api

import java.util.concurrent.CompletableFuture

// idea add methods to validate inputs and output schema info...
interface WorkflowService {
    fun call(request: WorkflowServiceRequest): CompletableFuture<WorkflowServiceResponse>
}

data class WorkflowServiceRequest(val inputs: Set<String>, val params: Map<String, ParamValue>)
data class WorkflowServiceResponse(val outputs: List<Output>)
data class Output(val name: String, val id: String)
