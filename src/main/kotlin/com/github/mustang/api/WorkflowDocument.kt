package com.github.mustang.api

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException

const val SEEDS_PARAM_NAME = "SEEDS"

data class WorkflowDocument(
    val inputs: List<Input>, val params: List<ConfigVariable>, val nodes: List<Node>,
    val outputs: Set<String>
) {


    data class Node(
        val serviceId: String, val name: String, val params: Map<String, ParamValue>,
        val inputs: Set<String>
    )

    data class Input(val name: String, val fields: List<Field>)

    data class Field(val name: String, val type: String)

    data class ConfigVariable(val name: String, val type: String)
}

private const val TYPE_NAME = "type";

sealed class ParamValue(val type: ParamValueEnum);
data class SeedParam(val seeds: Seeds) : ParamValue(ParamValueEnum.SEEDS)
data class StringParam(val value: String) : ParamValue(ParamValueEnum.STRING)

enum class ParamValueEnum {
    SEEDS, STRING
}

sealed class Seeds(val type: SeedsEnum)
object CurrentSeeds : Seeds(SeedsEnum.CURRENT)
data class ResultColumnSeeds(val resultName: String, val columnNames: List<String>) : Seeds(SeedsEnum.RESULT_COLUMNS)

enum class SeedsEnum {
    CURRENT, RESULT_COLUMNS
}

class SeedsDeserializer : StdDeserializer<Seeds>(Seeds::class.java) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Seeds {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val typeNode = node.get(TYPE_NAME)

        return when(mapper.treeToValue(typeNode, SeedsEnum::class.java)) {
            SeedsEnum.CURRENT -> CurrentSeeds
            SeedsEnum.RESULT_COLUMNS -> mapper.treeToValue(node, ResultColumnSeeds::class.java)
            else -> throw IOException("Seeds type is missing!")
        }
    }
}

class ParamValueDeserializer : StdDeserializer<ParamValue>(ParamValue::class.java) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): ParamValue {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val typeNode = node.get(TYPE_NAME)
        val deSerType = when(mapper.treeToValue(typeNode, ParamValueEnum::class.java)) {
             ParamValueEnum.SEEDS -> SeedParam::class.java
            ParamValueEnum.STRING -> StringParam::class.java
            else -> throw IOException("ParamValue type is missing")
        }
        return mapper.treeToValue(node, deSerType)
    }
}

fun newObjectMapper(): ObjectMapper {
    val mapper = jacksonObjectMapper()
    val module = SimpleModule()
    module.addDeserializer(Seeds::class.java, SeedsDeserializer())
    module.addDeserializer(ParamValue::class.java, ParamValueDeserializer())
    mapper.registerModule(module)
    return mapper
}
