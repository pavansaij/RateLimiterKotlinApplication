package SamplePackage

import SamplePackage.RateLimitBucketManager.BucketManager
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
    fun updateCapacity(@RequestParam("capacity") capacity: Int) : Int{
        bucketManager.resetBucket(capacity)
        return bucketManager.getBucket().availableTokens.toInt()
    }

    @RequestMapping(value = ["bucketManger/getAvailableCapacity"], headers = ["Accept=application/json"], method = [RequestMethod.GET],
        consumes = [MediaType.ALL_VALUE])
    fun getAvailableCapacity() : Int{
        return bucketManager.getBucket().availableTokens.toInt()
    }
}