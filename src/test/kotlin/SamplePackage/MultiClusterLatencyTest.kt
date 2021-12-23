package SamplePackage

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@ExtendWith(SpringExtension::class)
class MultiClusterLatencyTest {
    private val restTemplate = RestTemplate()

    private companion object {
        const val SLEEP_SEC_URL = "/sleepSecs?delaySecs=%d"
        const val UPDATE_CAPACITY = "/bucketManger/updateCapacity?capacity=%d"
        const val PUT_VALUE = "/bucketManger/putSampleVal?key=%s&value=%d"
        const val GET_VALUE = "/bucketManger/getSampleVal?key=%s"
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

    private fun getLongResp(url: String) : Long? {
        var response = restTemplate.exchange(url, HttpMethod.GET, null, kotlin.String::class.java)
        return response.body?.toLong()
    }

    private fun performGetCallHeaders(url: String) : HttpHeaders{

        var response = restTemplate.exchange(
            url,
            HttpMethod.GET, null, kotlin.String::class.java
        )

        return response.headers
    }

    private fun performGetCallWithTimeTaken(url: String) : Long{
        val start = System.currentTimeMillis()
        try {
            var response = restTemplate.exchange(
                url,
                HttpMethod.GET, null, kotlin.String::class.java
            )

            response.statusCode.value()
        } catch (e: HttpClientErrorException.TooManyRequests) {
            HttpStatus.TOO_MANY_REQUESTS.value()
        }

        return (System.currentTimeMillis() - start)
    }

    @Test
    fun testSleepSecsRateLimiting() {
        for (i in 1..2) {
            var url1 = String.format("http://localhost:8081$SLEEP_SEC_URL", 1)
            var resp1 = performGetCall(url1)

            Assertions.assertTrue(resp1 == 200)

            var url2 = String.format("http://localhost:8082$SLEEP_SEC_URL", 1)
            var resp2 = performGetCall(url2)

            if (i == 2) {
                Assertions.assertTrue(resp2 == 429)
                continue
            }

            Assertions.assertTrue(resp2 == 200)
        }
    }

    @Test
    fun testRateLimitingLatency() {
        var newRateLimit = 50

        var updateUrl = String.format("http://localhost:8081$UPDATE_CAPACITY", newRateLimit)
        performGetCallHeaders(updateUrl)

        val start = System.currentTimeMillis()
        var respTimes = mutableListOf<String>()

        for (i in 1..25) {
            var url1 = String.format("http://localhost:8081$SLEEP_SEC_URL", 1)
            var respHeaders1 = performGetCallHeaders(url1)
            respHeaders1["X-Bucket-Fetch"]?.get(0)?.let { respTimes.add(it) }

            var url2 = String.format("http://localhost:8082$SLEEP_SEC_URL", 1)
            var respHeaders2 = performGetCallHeaders(url2)
            respHeaders2["X-Bucket-Fetch"]?.get(0)?.let { respTimes.add(it) }
        }

        var timeTaken = (System.currentTimeMillis() - start)
        println(timeTaken)
    }

    @Test
    fun testRateLimitingLatencySingleInstance() {
        var newRateLimit = 50

        var updateUrl = String.format("http://localhost:8080$UPDATE_CAPACITY", newRateLimit)
        performGetCallHeaders(updateUrl)

        val start = System.currentTimeMillis()
        var respTimes = mutableListOf<String>()

        for (i in 1..50) {
            var url1 = String.format("http://localhost:8080$SLEEP_SEC_URL", 1)
            var respHeaders1 = performGetCallHeaders(url1)
            respHeaders1["X-Bucket-Fetch"]?.get(0)?.let { respTimes.add(it) }
        }

        var timeTaken = (System.currentTimeMillis() - start)
        println(timeTaken)
    }

    @Test
    fun testHazelCastSampleMapLatency() {
        val start = System.currentTimeMillis()
        var latencyValues = mutableListOf<Long>()

        for (i in 1..50) {
            var putUrl = String.format("http://localhost:8080$PUT_VALUE", "key$i", i)
            getLongResp(putUrl)?.let { latencyValues.add(it) }
            var getUrl = String.format("http://localhost:8080$GET_VALUE", "key$i")
            getLongResp(getUrl)?.let { latencyValues.add(it) }
        }

        var timeTaken = (System.currentTimeMillis() - start)
        print(timeTaken)
    }

    @Test
    fun getAverageRateLimitingLatency() {
        var newRateLimit = 100

        var updateUrl = String.format("http://localhost:8081$UPDATE_CAPACITY", newRateLimit)
        performGetCall(updateUrl)

        var latencyValues = mutableListOf<Long>()
        val start = System.currentTimeMillis()

        for (i in 1..50) {
            var url1 = String.format("http://localhost:8081$SLEEP_SEC_URL", 1)
            latencyValues.add(performGetCallWithTimeTaken(url1))

            var url2 = String.format("http://localhost:8082$SLEEP_SEC_URL", 1)
            latencyValues.add(performGetCallWithTimeTaken(url2))
        }

        var timeTaken = (System.currentTimeMillis() - start)
        latencyValues.sort()
        print(latencyValues)
    }
}