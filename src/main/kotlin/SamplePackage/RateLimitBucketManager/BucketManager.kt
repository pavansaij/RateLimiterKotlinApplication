package SamplePackage.RateLimitBucketManager

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import io.github.bucket4j.*

import io.github.bucket4j.grid.GridBucketState
import io.github.bucket4j.grid.RecoveryStrategy
import io.github.bucket4j.grid.hazelcast.Hazelcast
import io.github.bucket4j.grid.hazelcast.HazelcastBucketBuilder

import java.time.Duration


class BucketManager(capacity: Int = 2) {
    private val hazelCastInstance = createHazelCastInstance()

    private var bucket = createHazelCastBucket(capacity)

    private fun createHazelCastInstance(): HazelcastInstance {
        val clientConfig = ClientConfig()
        clientConfig.clusterName = "rate-limiter"
        return HazelcastClient.newHazelcastClient(clientConfig)
    }

    private fun createHazelCastBucket(capacity: Int): Bucket {
        val map: IMap<String, GridBucketState> = this.hazelCastInstance.getMap("bucket-map")

        return Bucket4j.extension<HazelcastBucketBuilder, Hazelcast>(Hazelcast::class.java).builder()
            .addLimit(Bandwidth.classic(capacity.toLong(), Refill.intervally(1, Duration.ofSeconds(10))))
            .build(map, "rate-limit",RecoveryStrategy.RECONSTRUCT)
    }

    /*
    Creates a local Rate limiting Bucket
     */
    private fun createBucketWithCapacity(capacity: Int): Bucket {
        val limit = Bandwidth.classic(capacity.toLong(), Refill.intervally(1, Duration.ofSeconds(10)))
        return Bucket4j.builder().addLimit(limit).build()
    }

    fun getBucket(): Bucket {
        return this.bucket
    }

    fun resetBucket(capacity: Int) {
        val newConfiguration = Bucket4j.configurationBuilder()
            .addLimit(Bandwidth.classic(capacity.toLong(), Refill.intervally(1, Duration.ofSeconds(10))))
            .build()
        this.bucket.replaceConfiguration(newConfiguration, TokensInheritanceStrategy.PROPORTIONALLY)
    }
}