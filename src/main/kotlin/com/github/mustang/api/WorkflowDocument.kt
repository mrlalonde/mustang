package com.github.mustang.api

data class WorkflowDocument(val inputs: List<Input>, val params: List<Param>, val nodes: List<Node>,
                            val outputs: Set<String>) {


    data class Node(val serviceId: String, val name: String, val params: Map<String, Any>,
                    val inputs: Set<String>)

    data class Input(val name: String, val fields: List<Field>)

    data class Field(val name: String, val type: String)

    data class Param(val name: String, val type: String);
}
