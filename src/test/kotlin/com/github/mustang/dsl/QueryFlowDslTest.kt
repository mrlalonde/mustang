package com.github.mustang.dsl

import com.github.mustang.api.StringParam
import com.github.mustang.api.WorkflowDocument
import org.junit.Test

class QueryFlowDslTest {
    @Test
    fun test() {
        val dsl = queryFlowDsl {
            start() {
                query(id = "my-query") {
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
        println(document)
    }

    @Test(expected = IllegalStateException::class)
    fun canStartAlreadyStarted() {
        queryFlowDsl {
            start { }
            start { }
        }
    }
}