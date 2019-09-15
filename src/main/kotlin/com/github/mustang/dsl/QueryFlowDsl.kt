package com.github.mustang.dsl

import com.github.mustang.api.*
import java.util.*
import kotlin.collections.ArrayList

class QueryFlowDsl(
    private val nodes: MutableList<WorkflowDocument.Node> = ArrayList(),
    private var started: Boolean = false
) {
    fun start(init: Branch.() -> Unit): Branch {
        if (started) throw IllegalStateException("Already started!")

        started = true

        val branch = Branch()
        branch.init()
        nodes.addAll(branch.build())
        return branch
    }

    fun build(): WorkflowDocument {
        return WorkflowDocument(
            inputs = listOf(
                WorkflowDocument.Input(
                    "Seed",
                    listOf(WorkflowDocument.Field("Seed", "IP"))
                )
            ),
            params = emptyList(),
            nodes = this.nodes,
            outputs = setOf("My Query")
        )
    }
}

fun queryFlowDsl(init: QueryFlowDsl.() -> Unit): QueryFlowDsl {
    val dsl = QueryFlowDsl()
    dsl.init()
    return dsl
}

class Branch {
    private val stackState: Stack<String> = Stack()
    private val nodes: MutableList<WorkflowDocument.Node> = ArrayList()

    fun query(id: String, init: Query.() -> Unit): Query {
        val query = Query(id = id, previous = previous())

        query.init()
        nodes.add(query.toNode())
        return query
    }

    fun call(id: String, init: ServiceCall.() -> Unit): ServiceCall {
        val serviceCall = ServiceCall(id)
        serviceCall.init()
        nodes.add(serviceCall.toNode())
        return serviceCall
    }

    private fun previous() = if (stackState.isEmpty()) "" else stackState.pop()

    fun build(): List<WorkflowDocument.Node> = nodes
}

interface NodeFactory {
    fun toNode(): WorkflowDocument.Node
}

data class Query(val id: String, var seeds: Seeds = CurrentSeeds, val previous: String) : NodeFactory {
    override fun toNode(): WorkflowDocument.Node {
        return WorkflowDocument.Node(
            "queryService", id, mapOf("SEEDS" to SeedParam(seeds)),
            inputs = setOf(previous)
        )
    }
}

data class ServiceCall(
    val id: String,
    var inputs: Set<String> = emptySet(),
    var parameters: Map<String, ParamValue> = emptyMap()
) : NodeFactory {
    override fun toNode(): WorkflowDocument.Node {
        return WorkflowDocument.Node(id, id, parameters, inputs)
    }

}
