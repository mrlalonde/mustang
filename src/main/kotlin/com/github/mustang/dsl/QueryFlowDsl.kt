package com.github.mustang.dsl

import com.github.mustang.api.*
import java.lang.IllegalArgumentException
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class QueryFlowDsl(
    private val nodes: MutableList<WorkflowDocument.Node> = ArrayList(),
    private var started: Boolean = false
) {
    private val dslContext = DslContext()
    private val inputs:  MutableList<Input> = ArrayList()

    fun input(name: String, init: Input.() -> Unit) : Input {
        val input = Input(name)
        input.init()
        inputs.add(input)
        return input
    }

    fun start(init: Branch.() -> Unit): Branch {
        if (started) throw IllegalStateException("Already started!")

        started = true

        val branch = Branch(BranchContext(dslContext))
        branch.init()
        nodes.addAll(branch.build())
        return branch
    }

    fun build(): WorkflowDocument {
        return WorkflowDocument(
            inputs = buildInputs(),
            params = emptyList(),
            nodes = this.nodes,
            outputs = setOf("My Query")
        )
    }

    private fun buildInputs(): List<WorkflowDocument.Input> = inputs.stream()
        .map(Input::toWorkflowDocumentInput)
        .collect(Collectors.toList())
}

fun queryFlowDsl(init: QueryFlowDsl.() -> Unit): QueryFlowDsl {
    val dsl = QueryFlowDsl()
    dsl.init()
    return dsl
}

class Input(private val name: String) {
    private val fields: MutableList<WorkflowDocument.Field> = ArrayList()

    fun withField(fieldName: String, fieldType: String) {
        fields.add(WorkflowDocument.Field(fieldName, fieldType))
    }

    fun toWorkflowDocumentInput() : WorkflowDocument.Input {
        return WorkflowDocument.Input(name, fields)
    }
}

class Branch(private val branchContext: BranchContext) {
    private val nodes: MutableList<WorkflowDocument.Node> = ArrayList()

    fun query(id: String, init: Query.() -> Unit): Query {
        val query = Query(id = id, branchContext = branchContext)

        query.init()
        nodes.add(query.toNode())
        return query
    }

    fun call(id: String, init: ServiceCall.() -> Unit): ServiceCall {
        val serviceCall = ServiceCall(id, branchContext)
        serviceCall.init()
        nodes.add(serviceCall.toNode())
        return serviceCall
    }

    fun build(): List<WorkflowDocument.Node> = nodes
}

class DslContext {
    private val registeredNames: MutableMap<String, Int> = HashMap()

    fun newName(candidateName: String) : String {
        val numberOfCopies = registeredNames.getOrDefault(candidateName, 0)
        registeredNames[candidateName] = numberOfCopies + 1
        return if (numberOfCopies == 0) candidateName else "$candidateName-$numberOfCopies"
    }
}

class BranchContext(private val dslContext: DslContext) {
    private val stackState: Stack<String> = Stack()

    fun registerNewName(candidateName: String) = dslContext.newName(candidateName)
    
    fun pushOutput(output: String) {
        stackState.push(output)
    }

    fun previousOutput(): String = if (stackState.isEmpty()) "" else stackState.pop()
}

interface NodeFactory {
    fun toNode(): WorkflowDocument.Node
}

abstract class BranchStep(val serviceId: String, private val branchContext: BranchContext): NodeFactory{
    private var inputName: String = ""
    protected var name: String = ""
    set(value) {
        field = branchContext.registerNewName(value)
    }
    val params: MutableMap<String, ParamValue> = HashMap()

    final override fun toNode(): WorkflowDocument.Node {
        postInit()
        if (inputName.isEmpty())
            inputName = branchContext.previousOutput()

        val inputs = if (inputName.isEmpty()) emptySet() else setOf(inputName)

        branchContext.pushOutput(name)
        return WorkflowDocument.Node(serviceId, name, params, inputs)
    }

    protected abstract fun postInit()
}

data class Query(val id: String, var seeds: Seeds = CurrentSeeds,
                 private val branchContext: BranchContext) : BranchStep("queryService", branchContext) {
    override fun postInit() {
        params[SEEDS_PARAM_NAME] = SeedParam(seeds)
        params["QUERY_ID"] = StringParam(id)
        super.name = id
    }
}

data class ServiceCall(
    val id: String,
    val branchContext: BranchContext
) : BranchStep(serviceId = id, branchContext = branchContext) {
    override fun postInit() {
        if (name.isEmpty()) name = serviceId
    }

}
