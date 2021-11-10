package SamplePackage

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest(@Autowired private val restTemplate: TestRestTemplate) {

    private companion object {
        const val SLEEP_SEC_URL = "/sleepSecs?delaySecs=%d"
        const val UPDATE_CAPACITY = "/bucketManger/updateCapacity?capacity=%d"
    }

    @Test
    fun testSleepSecsRateLimiting() {
        for (i in 1..4) {
            val response = restTemplate.exchange(
                String.format(SLEEP_SEC_URL, 1),
                HttpMethod.GET, null, String::class.java
            )

            if (i == 4) {
                assertTrue(response.statusCode.value() == 429)
                continue
            }

            assertTrue(response.statusCode.value() == 200)
        }
    }

    @Test
    fun testRateLimitingDynamicRateDecrease() {
        for (i in 1..4) {
            if (i == 1) {
                val bucketUpdateResp = restTemplate.exchange(String.format(UPDATE_CAPACITY, 2),
                    HttpMethod.GET, null, String::class.java)
                Assertions.assertEquals("1", bucketUpdateResp.body)
            }

            val response = restTemplate.exchange(String.format(SLEEP_SEC_URL, 1),
                HttpMethod.GET, null, String::class.java)
        }
    }

    @Test
    fun testRateLimitingDynamicRateIncrease() {
        var consumed = 0
        val configuredLimit = 3
        val newLimit = 6

        for (i in 1..4) {
            if (i == 3) {
                val bucketUpdateResp = restTemplate.exchange(String.format(UPDATE_CAPACITY, newLimit),
                    HttpMethod.GET, null, String::class.java)

                val expectedNewLimit = (newLimit/configuredLimit)*(configuredLimit-consumed)

                assertEquals( expectedNewLimit.toString(), bucketUpdateResp.body)
            }

            val response = restTemplate.exchange(String.format(SLEEP_SEC_URL, 1),
                HttpMethod.GET, null, String::class.java)

            consumed++

            assertTrue(response.statusCode.value() == 200)
        }
    }
}
