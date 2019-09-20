package com.github.mustang.exec

import com.github.dexecutor.core.DefaultDexecutor
import com.github.dexecutor.core.Dexecutor
import com.github.dexecutor.core.DexecutorConfig
import com.github.dexecutor.core.ExecutionListener
import com.github.dexecutor.core.task.Task
import com.github.dexecutor.core.task.TaskProvider
import com.github.mustang.api.*
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.stream.Collectors

class WorkflowDag(private val workflow: Workflow, serviceRegistry: ServiceRegistry) {
    private val config: DexecutorConfig<String, WorkflowServiceResponse> =
        DexecutorConfig(Executors.newFixedThreadPool(2), WorkflowTaskProvider(workflow, serviceRegistry))

    fun buildDag(): Dexecutor<String, WorkflowServiceResponse> {
        val dexecutor = DefaultDexecutor(config)
        workflow.nodesWithoutInputs().forEach { dexecutor.addIndependent(it.name) }
        workflow.nodesWithInputs().forEach { node ->
            node.inputs.forEach { dexecutor.addDependency(it, node.name) }
        }

        return dexecutor
    }

    fun outputs() : List<Output> {
        return config.dexecutorState.processedNodes.stream()
            .filter { workflow.isOutput(it.value)}
            .flatMap { it.result.outputs.stream() }
            .collect(Collectors.toList())
    }

    class OutputCollector : ExecutionListener<String, WorkflowServiceResponse> {
        override fun onError(task: Task<String, WorkflowServiceResponse>?, exception: Exception?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSuccess(task: Task<String, WorkflowServiceResponse>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}

class WorkflowTaskProvider(private val workflow: Workflow, private val serviceRegistry: ServiceRegistry) :
    TaskProvider<String, WorkflowServiceResponse> {
    override fun provideTask(taskId: String?): Task<String, WorkflowServiceResponse> {
        if (taskId == null) throw java.lang.IllegalArgumentException("task id can't be null")

        val workflowNode = workflow.nodeById(taskId)
        val workflowService = serviceRegistry.getServiceByName(workflowNode.serviceId)
        return WorkflowTask(workflowNode, workflowService)
    }

}

class WorkflowTask(private val workflowNode: WorkflowDocument.Node, private val workflowService: WorkflowService) :
    Task<String, WorkflowServiceResponse>() {
    override fun execute(): WorkflowServiceResponse {

        val result = workflowService.call(WorkflowServiceRequest(workflowNode.inputs, workflowNode.params)).get()
        return result
    }

}

class Workflow(private val workflowDocument: WorkflowDocument) {
    private val map: Map<String, WorkflowDocument.Node> = workflowDocument.nodes.stream()
        .collect(
            Collectors.toMap(
                { node: WorkflowDocument.Node -> node.name },
                { node: WorkflowDocument.Node -> node })
        )

    private val outputNames : Set<String> = HashSet(workflowDocument.outputs)

    fun nodeById(taskId: String): WorkflowDocument.Node {
        if (!map.containsKey(taskId)) throw IllegalArgumentException("Unknown task id $taskId")

        return map.getOrDefault(taskId, WorkflowDocument.Node("d", "", emptyMap(), emptySet()))
    }

    fun nodesWithInputs(): List<WorkflowDocument.Node> {
        return workflowDocument.nodes.stream()
            .filter { it.inputs.isNotEmpty() }
            .collect(Collectors.toList())
    }

    fun nodesWithoutInputs(): List<WorkflowDocument.Node> {
        return workflowDocument.nodes.stream()
            .filter { it.inputs.isEmpty() }
            .collect(Collectors.toList())
    }

    fun isOutput(value: String?) = outputNames.contains(value)
}