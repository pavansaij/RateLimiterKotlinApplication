package SamplePackage

import SamplePackage.RateLimitBucketManager.BucketManager
import io.github.bucket4j.Bucket
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class BucketManagerController {
    @Autowired
    lateinit var bucketManager: BucketManager

    @RequestMapping(value = ["bucketManger/updateCapacity"], headers = ["Accept=application/json"], method = [RequestMethod.GET],
        consumes = [MediaType.ALL_VALUE])
    fun updateCapacity(@RequestParam("capacity") capacity: Int) : Long? {
        bucketManager.resetBucket(capacity)
        return bucketManager.getBucket()?.availableTokens?.get()
    }

    @RequestMapping(value = ["bucketManger/getAvailableCapacity"], headers = ["Accept=application/json"], method = [RequestMethod.GET],
        consumes = [MediaType.ALL_VALUE])
    fun getAvailableCapacity() : Long? {
        return bucketManager.getBucket()?.availableTokens?.get()
    }

    @RequestMapping(value = ["bucketManger/getSampleVal"], headers = ["Accept=application/json"], method = [RequestMethod.GET],
        consumes = [MediaType.ALL_VALUE])
    fun getSampleVal(@RequestParam("key") key: String) : Long {
        val start = System.currentTimeMillis()
        bucketManager.sampleMap[key]!!
        return (System.currentTimeMillis() - start)
    }

    @RequestMapping(value = ["bucketManger/putSampleVal"], headers = ["Accept=application/json"], method = [RequestMethod.GET],
        consumes = [MediaType.ALL_VALUE])
    fun putSampleVal(@RequestParam("key") key: String, @RequestParam("value") value: Int) : Long {
        val start = System.currentTimeMillis()
        bucketManager.sampleMap[key] = value
        return (System.currentTimeMillis() - start)
    }
}