package SamplePackage

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.client.RestTemplate


@RunWith(SpringJUnit4ClassRunner::class)
class MultiClusterRateLimitTest {
    private val restTemplate = RestTemplate()

    private companion object {
        const val SLEEP_SEC_URL = "/sleepSecs?delaySecs=%d"
        const val UPDATE_CAPACITY = "/bucketManger/updateCapacity?capacity=%d"
    }

    @Before
    fun bringUpApplications() {
        val sampleApp1 = SpringApplicationBuilder(Application::class.java)
            .properties(
                "server.port=8081",
                "server.contextPath=/UserService",
                "SOA.ControllerFactory.enforceProxyCreation=true"
            )
        sampleApp1.run()

        val sampleApp2 = SpringApplicationBuilder(Application::class.java)
            .properties(
                "server.port=8082",
                "server.contextPath=/ProjectService",
                "SOA.ControllerFactory.enforceProxyCreation=true"
            )
        sampleApp2.run()
    }

    @Test
    fun testSleepSecsRateLimiting() {
        for (i in 1..4) {
            val response = restTemplate.exchange(
                String.format(MultiClusterRateLimitTest.SLEEP_SEC_URL, 1),
                HttpMethod.GET, null, String::class.java
            )

            if (i == 4) {
                Assertions.assertTrue(response.statusCode.value() == 429)
                continue
            }

            Assertions.assertTrue(response.statusCode.value() == 200)
        }
    }
}