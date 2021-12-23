package SamplePackage.Filters

import SamplePackage.RateLimitBucketManager.BucketManager
import io.github.bucket4j.ConsumptionProbe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RateLimitingFilter : Filter {

    @Autowired
    lateinit var bucketManager: BucketManager

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {

        val path = (request as HttpServletRequest).requestURI

        if (!path.startsWith("/sleepSecs")) {
            return chain!!.doFilter(request, response)
        }

        var asyncConsumptionProbe = this.bucketManager.getBucket()?.tryConsumeAndReturnRemaining(1)

        if (asyncConsumptionProbe != null && asyncConsumptionProbe.isCompletedExceptionally) {
            return chain!!.doFilter(request, response)
        }

        var consumptionProbe = asyncConsumptionProbe?.get()
        val resp = response as HttpServletResponse

        if (consumptionProbe != null && consumptionProbe.isConsumed) {
            //resp.addHeader("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
            return chain!!.doFilter(request, response)
        }

        resp.status = HttpStatus.TOO_MANY_REQUESTS.value()

        if (consumptionProbe != null) {
            resp.addHeader("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
            resp.addHeader("X-Rate-Limit-Retry-After-Millisecs", TimeUnit.NANOSECONDS.toMillis(consumptionProbe.nanosToWaitForRefill).toString())
        }

        resp.writer.write("Too many requests")
    }
}