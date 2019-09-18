package com.github.mustang.dsl

import com.github.mustang.api.*
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@DslMarker
annotation class QueryFlow

@QueryFlow
class QueryFlowDsl(
    private val nodes: MutableList<WorkflowDocument.Node> = ArrayList(),
    private var started: Boolean = false
) {
    private val dslContext = DslContext()
    private val inputs: MutableList<Input> = ArrayList()
    private val outputs: MutableSet<String> = HashSet()
    private val mainBranchContext = BranchContext(dslContext)

    fun input(name: String, init: Input.() -> Unit): Input {
        val input = Input(name)
        input.init()
        inputs.add(input)
        return input
    }

    fun start(init: Branch.() -> Unit): Branch {
        if (started) throw IllegalStateException("Already started!")

        started = true

        val branch = Branch(mainBranchContext)
        branch.init()
        nodes.addAll(branch.build())
        return branch
    }

    fun output(name: String) : Output {
        outputs += name
        return Output(name)
    }

    fun build(): WorkflowDocument {
        if (outputs.isEmpty() && !mainBranchOutput().isEmpty())
            outputs.add(mainBranchOutput())

        return WorkflowDocument(
            inputs = buildInputs(),
            params = emptyList(),
            nodes = this.nodes,
            outputs = outputs
        )
    }

    private fun mainBranchOutput(): String = mainBranchContext.previousOutput()

    private fun buildInputs(): List<WorkflowDocument.Input> = inputs.stream()
        .map(Input::toWorkflowDocumentInput)
        .collect(Collectors.toList())
}

fun queryFlowDsl(init: QueryFlowDsl.() -> Unit): QueryFlowDsl {
    val dsl = QueryFlowDsl()
    dsl.init()
    return dsl
}

@QueryFlow
class Input(private val name: String) {
    private val fields: MutableList<WorkflowDocument.Field> = ArrayList()

    fun withField(fieldName: String, fieldType: String) {
        fields.add(WorkflowDocument.Field(fieldName, fieldType))
    }

    fun toWorkflowDocumentInput(): WorkflowDocument.Input {
        return WorkflowDocument.Input(name, fields)
    }
}

@QueryFlow
class Branch(private val branchContext: BranchContext) {
    private val nodes: MutableList<WorkflowDocument.Node> = ArrayList()
    private var fork: Fork? = null

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

    fun fork(init: Fork.() -> Unit) {
        fork = Fork(branchContext, nodes)
        fork?.init()
    }

    fun join(init: Join.() -> Unit) {
        val join = Join(fork, branchContext)
        join.init()
        join.postInit()
        nodes += join.build()
    }

    fun build(): List<WorkflowDocument.Node> = nodes
    internal fun output(): String = branchContext.previousOutput()
}

@QueryFlow
class Fork(val parentContext: BranchContext, val nodes: MutableList<WorkflowDocument.Node>) {
    private val branches: MutableList<Branch> = ArrayList()
    private var lastOutput: String = ""

    fun branch(init: Branch.() -> Unit) {
        val branch = Branch(parentContext.newChildContext())
        branch.init()
        branches += branch
        nodes.addAll(branch.build())
        lastOutput = branch.output()
    }

    internal fun output(): String = lastOutput
}

@QueryFlow
class Join(val fork: Fork?, val branchContext: BranchContext) {
    var leftInput: String = ""
    var rightInput: String = ""

    var leftKeys: List<String> = emptyList()
    var rightKeys: List<String> = emptyList()
    private var name = ""

    fun postInit() {
        computeInputs()
        name = branchContext.registerNewName("join")
        branchContext.pushOutput(name)
    }

    fun build(): WorkflowDocument.Node {
        return WorkflowDocument.Node(
            serviceId = "join-service",
            name = name,
            params = mapOf(
                "leftKeys" to StringListParam(leftKeys),
                "rightKeys" to StringListParam(rightKeys)
            ),
            inputs = setOf(leftInput, rightInput))
    }

    private fun computeInputs() {
        if (leftInput.isEmpty())
            leftInput = branchContext.previousOutput()

        if (rightInput.isEmpty() && fork is Fork)
            rightInput = fork.output()

        if (leftInput.isEmpty() || rightInput.isEmpty())
            throw IllegalStateException("left/right inputs are not both defined inferred: $leftInput/$rightInput")
    }

}

class DslContext {
    private val registeredNames: MutableMap<String, Int> = HashMap()

    fun newName(candidateName: String): String {
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

    fun previousOutput(): String = if (stackState.isEmpty()) "" else stackState.peek()

    fun popPreviousOutput(): String = if (stackState.isEmpty()) "" else stackState.pop()

    fun newChildContext(): BranchContext {
        val child = BranchContext(dslContext)
        for (output in stackState) {
            child.stackState.push(output)
        }
        return child
    }
}

interface NodeFactory {
    fun toNode(): WorkflowDocument.Node
}

abstract class BranchStep(val serviceId: String, private val branchContext: BranchContext) : NodeFactory {
    private var inputName: String = ""
    protected var name: String = ""
        set(value) {
            field = branchContext.registerNewName(value)
        }
    val params: MutableMap<String, ParamValue> = HashMap()

    final override fun toNode(): WorkflowDocument.Node {
        postInit()
        if (inputName.isEmpty())
            inputName = branchContext.popPreviousOutput()

        val inputs = if (inputName.isEmpty()) emptySet() else setOf(inputName)

        branchContext.pushOutput(name)
        return WorkflowDocument.Node(serviceId, name, params, inputs)
    }

    protected abstract fun postInit()
}

@QueryFlow
data class Query(
    val id: String, var seeds: Seeds = CurrentSeeds,
    private val branchContext: BranchContext
) : BranchStep("queryService", branchContext) {
    override fun postInit() {
        params[SEEDS_PARAM_NAME] = SeedParam(seeds)
        params["QUERY_ID"] = StringParam(id)
        super.name = id
    }
}

@QueryFlow
data class Output(val name: String) {

}

@QueryFlow
data class ServiceCall(
    val id: String,
    val branchContext: BranchContext
) : BranchStep(serviceId = id, branchContext = branchContext) {
    override fun postInit() {
        if (name.isEmpty()) name = serviceId
    }

}
