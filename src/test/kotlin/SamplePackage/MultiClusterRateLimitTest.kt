package SamplePackage

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate


@RunWith(SpringJUnit4ClassRunner::class)
class MultiClusterRateLimitTest {
    private val restTemplate = RestTemplate()

    private companion object {
        const val SLEEP_SEC_URL = "/sleepSecs?delaySecs=%d"
        const val UPDATE_CAPACITY = "/bucketManger/updateCapacity?capacity=%d"
    }

    private fun performGetCall(url: String) : Int{
        return try {
            var response = restTemplate.exchange(
                url,
                HttpMethod.GET, null, kotlin.String::class.java
            )

            response.statusCode.value()
        } catch (e: HttpClientErrorException.TooManyRequests) {
            HttpStatus.TOO_MANY_REQUESTS.value()
        }
    }

    @Test
    fun testSleepSecsRateLimiting() {
        for (i in 1..2) {
            var url1 = String.format("http://localhost:8081"+MultiClusterRateLimitTest.SLEEP_SEC_URL, 1)
            var resp1 = performGetCall(url1)

            Assertions.assertTrue(resp1 == 200)

            var url2 = String.format("http://localhost:8082"+MultiClusterRateLimitTest.SLEEP_SEC_URL, 1)
            var resp2 = performGetCall(url2)

            if (i == 2) {
                Assertions.assertTrue(resp2 == 429)
                continue
            }

            Assertions.assertTrue(resp2 == 200)
        }
    }
}