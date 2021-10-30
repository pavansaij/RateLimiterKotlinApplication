package SamplePackage

import SamplePackage.RateLimitBucketManager.BucketManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
    @Bean
    fun bucketManager(): BucketManager {
        return BucketManager(3)
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
