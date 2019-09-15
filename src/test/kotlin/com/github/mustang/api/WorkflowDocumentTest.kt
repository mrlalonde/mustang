package com.github.mustang.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class WorkflowDocumentTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
     fun roundTrip() {
        val wd = WorkflowDocument(
            inputs = listOf(WorkflowDocument.Input("Seed",
                listOf(WorkflowDocument.Field("Seed", "IP")))),
            params = emptyList(),
            nodes = listOf(WorkflowDocument.Node(serviceId = "queryService", name = "My Query",
                inputs = emptySet(),
                params = mapOf<String, Any>("seeds" to "CURRENT_SEEDS", "queryId" to "my.query"))),
            outputs = setOf("My Query"))

        val output = objectMapper.writeValueAsString(wd)

        val deserialized = objectMapper.readValue(output, WorkflowDocument::class.java)
        assertEquals(wd, deserialized)
    }
}