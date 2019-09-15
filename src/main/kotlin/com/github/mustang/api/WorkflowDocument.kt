package com.github.mustang.api

const val SEEDS_PARAM_NAME = "SEEDS"

data class WorkflowDocument(val inputs: List<Input>, val params: List<ConfigVariable>, val nodes: List<Node>,
                            val outputs: Set<String>) {


    data class Node(val serviceId: String, val name: String, val params: Map<String, ParamValue>,
                    val inputs: Set<String>)

    data class Input(val name: String, val fields: List<Field>)

    data class Field(val name: String, val type: String)

    data class ConfigVariable(val name: String, val type: String);
}

sealed class ParamValue;
data class SeedParam(val seeds: Seeds): ParamValue()
data class StringParam(val value: String): ParamValue()


sealed class Seeds
object CurrentSeeds : Seeds()
data class ResultColumnSeeds(val resultName: String, val columnNames: List<String>) : Seeds()
