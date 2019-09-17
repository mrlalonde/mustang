package com.github.mustang.api

import java.util.stream.Collectors
import kotlin.test.Test
import kotlin.test.assertEquals

class SeedsTest {
    @Test
    fun subClassesHaveDistinctTypes() {
        val seedsExample = listOf(CurrentSeeds, ResultColumnSeeds("previous", listOf("IP")))
        assertEquals(Seeds::class.sealedSubclasses.size, seedsExample.size, "Example is not exhaustive!")

        val distinctTypes =  seedsExample.stream()
            .map { it -> it.type }
            .collect(Collectors.toSet())

        assertEquals(seedsExample.size, distinctTypes.size)
    }
}