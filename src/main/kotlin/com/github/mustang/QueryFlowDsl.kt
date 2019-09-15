package com.github.mustang

import com.github.mustang.api.WorkflowDocument
import java.util.*
import kotlin.collections.ArrayList

class QueryFlowDsl(
    private val nodes: List<WorkflowDocument.Node> = ArrayList(),
    private val stackState: Stack<WorkflowDocument.Node> = Stack()
) {

    fun query(id: String, init: Query.() -> Unit): Query {
        val query = Query(id)
        query.init()
        return query
    }

    fun transform(id: String, init: Transform.() -> Unit): Transform {
        val transform = Transform(id)
        transform.init()
        return transform
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
            nodes = listOf(
                WorkflowDocument.Node(
                    serviceId = "my.query", name = "My Query",
                    inputs = emptySet(),
                    params = mapOf<String, Any>("seeds" to "CURRENT_SEEDS")
                )
            ),
            outputs = setOf("My Query")
        )
    }
}

fun queryFlowDsl(init: QueryFlowDsl.() -> Unit): QueryFlowDsl {
    val dsl = QueryFlowDsl()
    dsl.init()
    return dsl
}


data class Query(val id: String, var seeds: Seeds = CurrentSeeds) {

}


data class Transform(
    val id: String,
    var inputs: Set<String> = emptySet(),
    var parameters: Map<String, Any> = emptyMap()
)

val dsl = queryFlowDsl {
    query(id = "my.query") {}
    query(id = "my.second.query") {
        seeds = ResultColumnSeeds("my.query", listOf("Output IP"))
    }
    transform("summarize") {
        parameters = mapOf("sql" to "SELECT IP, COMPANY FROM %s GROUP BY IP, COMPANY, COUNT(1) AS TOTAL")
    }
}

sealed class Seeds
object CurrentSeeds : Seeds()
data class ResultColumnSeeds(val resultName: String, val columnNames: List<String>) : Seeds()
