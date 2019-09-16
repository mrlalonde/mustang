package com.github.mustang.api

import org.junit.Test
import kotlin.test.assertEquals

class WorkflowDocumentTest {
    private val objectMapper = newObjectMapper()

    @Test
     fun roundTrip() {
        val wd = WorkflowDocument(
            inputs = listOf(WorkflowDocument.Input("Seed",
                listOf(WorkflowDocument.Field("Seed", "IP")))),
            params = listOf(WorkflowDocument.ConfigVariable("DateRange", "DATE_RANGE")),
            nodes = listOf(WorkflowDocument.Node(serviceId = "queryService", name = "My Query",
                inputs = emptySet(),
                params = mapOf(SEEDS_PARAM_NAME to SeedParam(CurrentSeeds), "queryId" to StringParam("my.query")))),
            outputs = setOf("My Query"))

        val output = objectMapper.writeValueAsString(wd)

        val deserialized = objectMapper.readValue(output, WorkflowDocument::class.java)
        assertEquals(wd, deserialized)
    }
}