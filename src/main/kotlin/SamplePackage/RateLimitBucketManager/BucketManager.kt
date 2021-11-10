package SamplePackage.RateLimitBucketManager

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.ClientConnectionStrategyConfig
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.internal.util.IterableUtil.limit
import com.hazelcast.map.IMap
import io.github.bucket4j.*
import io.github.bucket4j.grid.GridBucketState
import io.github.bucket4j.grid.RecoveryStrategy
import io.github.bucket4j.grid.hazelcast.Hazelcast
import java.time.Duration


class BucketManager(capacity: Int = 2) {

    private val hazelCastInstance = createHazelCastInstance()

    private var bucket = createHazelCastBucket(capacity)

    private fun createHazelCastInstance(): HazelcastInstance {
        var clientConfig = ClientConfig()
        clientConfig.clusterName = "rate-limiter"
        clientConfig.connectionStrategyConfig.reconnectMode = ClientConnectionStrategyConfig.ReconnectMode.ASYNC

        return HazelcastClient.newHazelcastClient(clientConfig)
    }

    private fun createHazelCastBucket(capacity: Int): Bucket {
        val map: IMap<String, GridBucketState> = this.hazelCastInstance.getMap("bucket-map")

        val limit = Bandwidth.classic(capacity.toLong(), Refill.intervally(1, Duration.ofSeconds(20)))
        return Bucket4j.extension(Hazelcast::class.java).builder().addLimit(limit)
            .build(map, "rate-limit", RecoveryStrategy.RECONSTRUCT)
    }

    fun getBucket(): Bucket {
        return this.bucket
    }

    fun resetBucket(capacity: Int) {
        val newConfiguration = Bucket4j.configurationBuilder()
            .addLimit(Bandwidth.classic(capacity.toLong(), Refill.intervally(2, Duration.ofSeconds(20))))
            .build()
        this.bucket.replaceConfiguration(newConfiguration, TokensInheritanceStrategy.PROPORTIONALLY)
    }

    companion object {


        /*
            Creates a local Rate limiting Bucket
        */
        fun getLocalBucket(capacity: Int) : Bucket{
            val limit = Bandwidth.classic(capacity.toLong(), Refill.intervally(1, Duration.ofSeconds(20)))
            return Bucket4j.builder().addLimit(limit).build()
        }

    }
}