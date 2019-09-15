package com.github.mustang.api

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.type.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test


sealed class Seeds
object CurrentSeeds : Seeds()
data class ResultColumnSeeds(val resultName: String, val columnNames: List<String>) : Seeds()

class SealedClassSpikeTest {


    @Test
    fun roundTrip() {
        val seeds = CurrentSeeds;
        val mapper = jacksonObjectMapper()
        mapper.registerModule(MyModule())
        mapper.registerModule(KotlinModule())

        println(jacksonObjectMapper().writeValueAsString(seeds))


    }
}

class MyModule: Module() {
    override fun getModuleName() = "MyModule"

    override fun version() = Version(0, 0,1, "","com.github.mrlalon",
        "mustang")

    override fun setupModule(ctx: SetupContext?) {
    }

}

class MyDeserializers: Deserializers {
    override fun findCollectionLikeDeserializer(
        p0: CollectionLikeType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?,
        p3: TypeDeserializer?,
        p4: JsonDeserializer<*>?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findMapDeserializer(
        p0: MapType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?,
        p3: KeyDeserializer?,
        p4: TypeDeserializer?,
        p5: JsonDeserializer<*>?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findBeanDeserializer(
        p0: JavaType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findTreeNodeDeserializer(
        p0: Class<out JsonNode>?,
        p1: DeserializationConfig?,
        p2: BeanDescription?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findReferenceDeserializer(
        p0: ReferenceType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?,
        p3: TypeDeserializer?,
        p4: JsonDeserializer<*>?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findMapLikeDeserializer(
        p0: MapLikeType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?,
        p3: KeyDeserializer?,
        p4: TypeDeserializer?,
        p5: JsonDeserializer<*>?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findEnumDeserializer(
        p0: Class<*>?,
        p1: DeserializationConfig?,
        p2: BeanDescription?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findArrayDeserializer(
        p0: ArrayType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?,
        p3: TypeDeserializer?,
        p4: JsonDeserializer<*>?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findCollectionDeserializer(
        p0: CollectionType?,
        p1: DeserializationConfig?,
        p2: BeanDescription?,
        p3: TypeDeserializer?,
        p4: JsonDeserializer<*>?
    ): JsonDeserializer<*> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
