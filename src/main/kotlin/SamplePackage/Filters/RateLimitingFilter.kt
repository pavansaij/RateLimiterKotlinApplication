package SamplePackage.Filters

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j

import org.springframework.stereotype.Component;

import java.time.Duration
import javax.servlet.*
import javax.servlet.http.HttpServletResponse

@Component
class RateLimitingFilter : Filter {

    lateinit var rateLimitingBucket : Bucket

    private fun createIpRateLimitBucket(): Bucket {
        val limit = Bandwidth.simple(2, Duration.ofMinutes(1))
        return Bucket4j.builder().addLimit(limit).build()
    }

    override fun init(filterConfig: FilterConfig) {
        rateLimitingBucket = createIpRateLimitBucket()
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (this.rateLimitingBucket.tryConsume(1)) {
            return chain!!.doFilter(request, response)
        }

        val resp = response as HttpServletResponse
        resp.status = 429
        resp.writer.write("Too many requests")
    }
}