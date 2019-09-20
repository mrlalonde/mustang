package com.github.mustang.exec

import com.github.dexecutor.core.ExecutionEngine
import com.github.dexecutor.core.ExecutionListener
import com.github.dexecutor.core.task.ExecutionResult
import com.github.dexecutor.core.task.Task
import com.github.mustang.api.WorkflowServiceResponse
import java.util.concurrent.CompletableFuture

/**
 * Idea here would be to prototype an engine that doesn't block on task execution.
 * Could even explore coroutines!
 */
class AsyncExecutionEngine: ExecutionEngine<String, CompletableFuture<WorkflowServiceResponse>> {
    override fun isDistributed(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun submit(p0: Task<String, CompletableFuture<WorkflowServiceResponse>>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setExecutionListener(p0: ExecutionListener<String, CompletableFuture<WorkflowServiceResponse>>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processResult(): ExecutionResult<String, CompletableFuture<WorkflowServiceResponse>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isAnyTaskInError(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}