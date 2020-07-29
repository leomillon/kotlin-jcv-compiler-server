package com.compiler.server

import com.compiler.server.base.BaseJUnitTest
import com.compiler.server.model.TestStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JCVTestsRunnerTest : BaseJUnitTest() {

    @Test
    fun `should get a successful JCV assertion`() {
        val test = test(
            """
            import com.ekino.oss.jcv.assertion.assertj.JsonCompareAssert.Companion.assertThatJson
            import org.junit.Test

            class Example1Test {

                @Test
                fun `should validate actual JSON against the expected one`() {

                    val actualJson = ""${'"'}
                        {
                          "field_1": "some value",
                          "field_2": "3716a0cf-850e-46c3-bd97-ac1f34437c43",
                          "date": "2011-12-03T10:15:30Z",
                          "other_fields": [{
                            "id": "2",
                            "link": "https://another.url.com/my-base-path/query?param1=true"
                          }, {
                            "id": "1",
                            "link": "https://some.url.com"
                          }]
                        }
                        ""${'"'}.trimIndent()

                    val expectedJson = ""${'"'}
                            {
                              "field_1": "some value",
                              "field_2": "{#uuid#}",
                              "date": "{#date_time_format:iso_instant#}",
                              "other_fields": [{
                                "id": "1",
                                "link": "{#url#}"
                              }, {
                                "id": "2",
                                "link": "{#url_ending:query?param1=true#}"
                              }]
                            }
                            ""${'"'}.trimIndent()

                    assertThatJson(actualJson).isValidAgainst(expectedJson)
                }
            }
            """.trimIndent()
        )
        val testDescription = test.first()
        Assertions.assertTrue(testDescription.status == TestStatus.OK)
    }

    @Test
    fun `should get a failing JCV assertion`() {
        val test = test(
            """
            import com.ekino.oss.jcv.assertion.assertj.JsonCompareAssert.Companion.assertThatJson
            import org.junit.Test

            class Example1Test {

                @Test
                fun `should validate actual JSON against the expected one`() {

                    val actualJson = ""${'"'}
                        {
                          "field_1": "some value",
                          "field_2": "3716a0cf-850e-46c3-bd97-ac1f34437c43",
                          "date": "2011-12-03T10:15:30Z",
                          "other_fields": [{
                            "id": "2",
                            "link": "https://another.url.com/my-base-path/query?param1=true"
                          }, {
                            "id": "1",
                            "link": "https://some.url.com"
                          }]
                        }
                        ""${'"'}.trimIndent()

                    val expectedJson = ""${'"'}
                            {
                              "field_1": "some value 2",
                              "field_2": "{#uuid#}",
                              "date": "{#date_time_format:iso_instant#}",
                              "other_fields": [{
                                "id": "1",
                                "link": "{#url#}"
                              }, {
                                "id": "2",
                                "link": "{#url_ending:query?param1=false#}"
                              }]
                            }
                            ""${'"'}.trimIndent()

                    assertThatJson(actualJson).isValidAgainst(expectedJson)
                }
            }
            """.trimIndent()
        )
        val testDescription = test.first()
        Assertions.assertTrue(testDescription.status == TestStatus.FAIL)
        Assertions.assertTrue(testDescription.comparisonFailure?.fullName == "java.lang.AssertionError")
        Assertions.assertTrue(
            testDescription.comparisonFailure?.message ==
            """
            field_1
            Expected: some value 2
                 got: some value
             ; other_fields[id=2].link: Value should end with 'query?param1=false'
            Expected: {#url_ending:query?param1=false#}
                 got: https://another.url.com/my-base-path/query?param1=true
            
            """.trimIndent()
        )
    }
}
