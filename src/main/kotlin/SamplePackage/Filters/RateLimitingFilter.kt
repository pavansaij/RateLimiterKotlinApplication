package SamplePackage.Filters

import SamplePackage.RateLimitBucketManager.BucketManager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

import javax.servlet.*
import javax.servlet.http.HttpServletResponse

@Component
class RateLimitingFilter : Filter {

    @Autowired
    lateinit var bucketManager: BucketManager

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        var consumptionProbe = this.bucketManager.getBucket().tryConsumeAndReturnRemaining(1)
        val resp = response as HttpServletResponse

        if (consumptionProbe.isConsumed) {
            resp.addHeader("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
            return chain!!.doFilter(request, response)
        }

        resp.status = HttpStatus.TOO_MANY_REQUESTS.value()
        resp.addHeader("X-Rate-Limit-Remaining", consumptionProbe.remainingTokens.toString())
        resp.addHeader("X-Rate-Limit-Retry-After-Millisecs", TimeUnit.NANOSECONDS.toMillis(consumptionProbe.nanosToWaitForRefill).toString())
        resp.writer.write("Too many requests")
    }
}