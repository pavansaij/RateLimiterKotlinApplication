package SamplePackage

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

    companion object {
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
    fun testRateLimitingDynamicRateIncrease() {
        for (i in 1..3) {
            if (i == 2) {
                val bucketUpdateResp = restTemplate.exchange(String.format(UPDATE_CAPACITY, 6),
                    HttpMethod.GET, null, String::class.java)
                println(bucketUpdateResp.body)
                assertEquals( "2", bucketUpdateResp.body)
            }

            val response = restTemplate.exchange(String.format(SLEEP_SEC_URL, 1),
                HttpMethod.GET, null, String::class.java)

            assertTrue(response.statusCode.value() == 200)
        }
    }
}
