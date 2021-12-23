package SamplePackage.RateLimitBucketManager

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.ClientConnectionStrategyConfig
import com.hazelcast.client.config.ClientNetworkConfig
import com.hazelcast.config.Config
import com.hazelcast.config.SerializationConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import io.github.bucket4j.*
import io.github.bucket4j.distributed.AsyncBucketProxy
import io.github.bucket4j.distributed.proxy.ClientSideConfig
import io.github.bucket4j.distributed.proxy.optimization.Optimizations
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager
import java.time.Duration


class BucketManager(capacity: Int = 2) {

    private val hazelCastInstance = createHazelCastInstance()

    private var bucket = createHazelCastBucket(capacity)

    var sampleMap = createHazelCastMap()

    private fun createHazelCastInstance(): HazelcastInstance {
        val configDev = Config()
        configDev.clusterName = "development"

        return Hazelcast.newHazelcastInstance(configDev)
    }

    private fun createHazelCastInstanceFromUrl(): HazelcastInstance {
        var networkConfig = ClientNetworkConfig()
        networkConfig.addresses = listOf("prasadj-dev-aps1.workspaces.corp.win.ia55.net:5701")

        val config = Config()
        val serializationConfig: SerializationConfig = config.serializationConfig
        HazelcastProxyManager.addCustomSerializers(serializationConfig, 1000)

        var clientConfig = ClientConfig()
        clientConfig.connectionStrategyConfig.reconnectMode = ClientConnectionStrategyConfig.ReconnectMode.ASYNC
        clientConfig.networkConfig = networkConfig

        return HazelcastClient.newHazelcastClient(clientConfig)
    }

    private fun createHazelCastMap(): IMap<String, Int> {
        return hazelCastInstance.getMap("sample")
    }

    private fun createHazelCastBucket(capacity: Int): AsyncBucketProxy? {
        val map: IMap<String, ByteArray> = this.hazelCastInstance.getMap("bucket-map")

        val proxyManager: HazelcastProxyManager<String> = HazelcastProxyManager(map, ClientSideConfig.getDefault())

        val configuration = BucketConfiguration.builder()
            .addLimit(Bandwidth.classic(capacity.toLong(), Refill.intervally(1, Duration.ofSeconds(20))))
            .build()

        return proxyManager.asAsync().builder()
            .withOptimization(Optimizations.batching())
            .build("rate-limit", configuration)
    }

    fun getBucket(): AsyncBucketProxy? {
        return this.bucket
    }

    fun resetBucket(capacity: Int) {
        val newConfiguration = BucketConfiguration.builder()
            .addLimit(Bandwidth.classic(capacity.toLong(), Refill.intervally(2, Duration.ofSeconds(20))))
            .build()
        this.bucket?.replaceConfiguration(newConfiguration, TokensInheritanceStrategy.PROPORTIONALLY)
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