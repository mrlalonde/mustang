package com.github.mustang.stubs

import com.github.dexecutor.core.ExecutionConfig
import com.github.dexecutor.core.task.ExecutionResults
import com.github.mustang.api.Output
import com.github.mustang.api.StringParam
import com.github.mustang.api.WorkflowServiceResponse
import com.github.mustang.dsl.queryFlowDsl
import com.github.mustang.exec.Workflow
import com.github.mustang.exec.WorkflowDag
import org.junit.Test
import kotlin.test.assertEquals

class StubsTest {
    @Test
    fun testIt() {
        val workflow = queryFlowDsl {
            input("seeds") {
                withField("IP", "STRING")
            }
            start {
                query("query") {
                }
                call("sql-service"){
                    params["sql"] = StringParam("SELECT * FROM TABLE")
                }
            }

        }.build()

        val dag = WorkflowDag(Workflow(workflow), stubRegistry)
        val dexecutor = dag.buildDag()
        val erroredResults: ExecutionResults<String, WorkflowServiceResponse> = dexecutor.execute(ExecutionConfig.TERMINATING)

        assertEquals(0, erroredResults.all.size)
        assertEquals(1, dag.outputs().size)
        assertEquals(listOf(Output("sql-service", "2")), dag.outputs())

    }
}