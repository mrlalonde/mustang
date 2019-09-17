package com.github.mustang.api

import org.junit.Test
import java.util.stream.Collectors
import kotlin.test.assertEquals

class ParamValueTest {
    @Test
    fun subClassesShouldHaveDistinctTypes() {
        val paramValueExamples = listOf(SeedParam(CurrentSeeds), StringParam("foobar"))
        assertEquals(ParamValue::class.sealedSubclasses.size, paramValueExamples.size, "Example is not exhaustive!")

        val distinctTypes =  paramValueExamples.stream()
            .map { it -> it.type }
            .collect(Collectors.toSet())

        assertEquals(paramValueExamples.size, distinctTypes.size)
    }
}