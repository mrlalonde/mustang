package com.github.mustang.dsl

import com.github.mustang.api.StringParam
import com.github.mustang.api.WorkflowDocument
import com.github.mustang.api.newObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class QueryFlowDslTest {
    @Test
    fun test() {
        val dsl = queryFlowDsl {
            start() {
                query(id = "geoLocateIP") {
                }
                call(id = "sql-service") {
                    parameters = mapOf(
                        "sql" to
                                StringParam("SELECT IP, COUNTRY, COUNT(1) AS TOTAL GROUP BY (IP, COUNTRY)")
                    )
                }

            }
        }

        val document = dsl.build()
        val expectedString = """
{
  "inputs" : [ {
    "name" : "Seed",
    "fields" : [ {
      "name" : "Seed",
      "type" : "IP"
    } ]
  } ],
  "params" : [ ],
  "nodes" : [ {
    "serviceId" : "queryService",
    "name" : "geoLocateIP",
    "params" : {
      "SEEDS" : {
        "seeds" : {
          "type" : "CURRENT"
        },
        "type" : "SEEDS"
      },
      "QUERY_ID" : {
        "value": "geoLocateIP",
        "type": "STRING"
      }
    },
    "inputs" : [ ]
  }, {
    "serviceId" : "sql-service",
    "name" : "sql-service",
    "params" : {
      "sql" : {
        "value" : "SELECT IP, COUNTRY, COUNT(1) AS TOTAL GROUP BY (IP, COUNTRY)",
        "type" : "STRING"
      }
    },
    "inputs" : [ ]
  } ],
  "outputs" : [ "My Query" ]
}"""
        assertEquals(newObjectMapper().readValue(expectedString, WorkflowDocument::class.java), document,
            " $expectedString \n\nmismatch\n\n ${newObjectMapper().writeValueAsString(document)}")
    }

    @Test(expected = IllegalStateException::class)
    fun canStartAlreadyStarted() {
        queryFlowDsl {
            start { }
            start { }
        }
    }
}