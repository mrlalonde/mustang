package com.github.mustang.dsl

import com.github.mustang.api.ResultColumnSeeds
import com.github.mustang.api.StringParam
import com.github.mustang.api.WorkflowDocument
import com.github.mustang.api.newObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class QueryFlowDslTest {
    @Test
    fun workflowOneBranch() {
        val dsl = queryFlowDsl {
            input("Seed") {
                withField("Seed", "IP")
            }
            start {
                query(id = "geoLocateIP") {
                }
                call(id = "sql-service") {
                    params["sql"] = StringParam("SELECT IP, COUNTRY, COUNT(1) AS TOTAL GROUP BY (IP, COUNTRY)")
                }
            }
            output("geoLocateIP")
            output("sql-service")
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
    "inputs" : [ "geoLocateIP" ]
  } ],
  "outputs" : [ "geoLocateIP", "sql-service" ]
}"""
        assertEquals(
            newObjectMapper().readValue(expectedString, WorkflowDocument::class.java), document,
            " $expectedString \n\nmismatch\n\n ${newObjectMapper().writeValueAsString(document)}"
        )
    }

    @Test(expected = IllegalStateException::class)
    fun canStartAlreadyStarted() {
        queryFlowDsl {
            start { }
            start { }
        }
    }

    @Test
    fun workflowNameClash() {
        val dsl = queryFlowDsl {
            input("Seed") {
                withField("Seed", "IP")
                withField("Other Field", "STRING")
            }
            start {

                query(id = "findFriends") {
                }
                query(id = "findFriends") {
                    seeds = ResultColumnSeeds(resultName = "findFriends", columnNames = listOf("Friend"))
                }
                query(id = "findFriends") {
                    seeds = ResultColumnSeeds(resultName = "findFriends-1", columnNames = listOf("Friend"))
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
    },
     {
       "name" : "Other Field",
       "type" : "STRING"
     }]
  } ],
  "params" : [ ],
  "nodes" : [ {
    "serviceId" : "queryService",
    "name" : "findFriends",
    "params" : {
      "SEEDS" : {
        "seeds" : {
          "type" : "CURRENT"
        },
        "type" : "SEEDS"
      },
      "QUERY_ID" : {
        "value" : "findFriends",
        "type" : "STRING"
      }
    },
    "inputs" : [ ]
  }, {
    "serviceId" : "queryService",
    "name" : "findFriends-1",
    "params" : {
      "SEEDS" : {
        "seeds" : {
          "resultName" : "findFriends",
          "columnNames" : [ "Friend" ],
          "type" : "RESULT_COLUMNS"
        },
        "type" : "SEEDS"
      },
      "QUERY_ID" : {
        "value" : "findFriends",
        "type" : "STRING"
      }
    },
    "inputs" : [ "findFriends" ]
  }, {
    "serviceId" : "queryService",
    "name" : "findFriends-2",
    "params" : {
      "SEEDS" : {
        "seeds" : {
          "resultName" : "findFriends-1",
          "columnNames" : [ "Friend" ],
          "type" : "RESULT_COLUMNS"
        },
        "type" : "SEEDS"
      },
      "QUERY_ID" : {
        "value" : "findFriends",
        "type" : "STRING"
      }
    },
    "inputs" : [ "findFriends-1" ]
  } ],
  "outputs" : [ "findFriends-2" ]
} """
        assertEquals(
            newObjectMapper().readValue(expectedString, WorkflowDocument::class.java), document,
            " $expectedString \n\nmismatch\n\n ${newObjectMapper().writeValueAsString(document)}"
        )
    }

    @Test
    fun forkJoin() {
        val dsl = queryFlowDsl {
            input("Seed") {
                withField("IP", "IP")
            }
            start {
                query("events") {
                }
                fork {
                    branch {
                        query("geo") {
                            seeds = ResultColumnSeeds("events", listOf("Active IP"))
                        }
                    }
                }
                join {
                    leftKeys = listOf("Active IP")
                    rightKeys = listOf("IP")
                }
            }
        }

        val document = dsl.build()
        val expectedString = """
 {
  "inputs" : [ {
    "name" : "Seed",
    "fields" : [ {
      "name" : "IP",
      "type" : "IP"
    }]
  } ],
  "params" : [ ],
  "nodes" : [ {
    "serviceId" : "queryService",
    "name" : "events",
    "params" : {
      "SEEDS" : {
        "seeds" : {
          "type" : "CURRENT"
        },
        "type" : "SEEDS"
      },
      "QUERY_ID" : {
        "value" : "events",
        "type" : "STRING"
      }
    },
    "inputs" : [ ]
  }, {
    "serviceId" : "queryService",
    "name" : "geo",
    "params" : {
      "SEEDS" : {
        "seeds" : {
          "resultName" : "events",
          "columnNames" : [ "Active IP" ],
          "type" : "RESULT_COLUMNS"
        },
        "type" : "SEEDS"
      },
      "QUERY_ID" : {
        "value" : "geo",
        "type" : "STRING"
      }
    },
    "inputs" : [ "events" ]
  }, {
    "serviceId" : "join-service",
    "name" : "join",
    "params" : {
      "leftKeys" : {
        "values" : ["Active IP"],
        "type" : "STRING_LIST"
      },
      "rightKeys" : {
        "values" : ["IP"],
        "type" : "STRING_LIST"
      }
    },
    "inputs" : [ "events", "geo" ]
  } ],
  "outputs" : [ "join" ]
} """
        assertEquals(
            newObjectMapper().readValue(expectedString, WorkflowDocument::class.java), document,
            " $expectedString \n\nmismatch\n\n ${newObjectMapper().writeValueAsString(document)}"
        )
    }
}