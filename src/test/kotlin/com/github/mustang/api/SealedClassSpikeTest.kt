package com.github.mustang.api

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class SealedClassSpikeTest {
    val mapper = newObjectMapper()

    @Test
    fun roundTripCurrentSeeds() {
        verifyRoundTrip(CurrentSeeds)
    }

    @Test
    fun roundTripResultColumnSeeds() {
        verifyRoundTrip(ResultColumnSeeds("previousResult", listOfNotNull("Source IP", "Dest IP")))
    }

    @Test
    fun roundTripStringParam() {
        verifyRoundTrip(StringParam("Hello"))
    }

    @Test
    fun roundTripSeedsParam() {
        verifyRoundTrip(SeedParam(CurrentSeeds))
    }

    private fun verifyRoundTrip(seeds: Seeds) {
        val jsonValue = mapper.writeValueAsString(seeds)
        val deSer = mapper.readValue(jsonValue, Seeds::class.java)
        assertEquals(seeds, deSer)
    }

    private fun verifyRoundTrip(paramValue: ParamValue) {
        val jsonValue = mapper.writeValueAsString(paramValue)
        val deSer = mapper.readValue(jsonValue, ParamValue::class.java)
        assertEquals(paramValue, deSer)
    }
}
