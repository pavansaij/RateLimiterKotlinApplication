package SamplePackage

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
class SampleController {

    @RequestMapping(value = ["sleepSecs"], headers = ["Accept=application/json"], method = [RequestMethod.GET],
            consumes = [MediaType.ALL_VALUE])
    fun sleepSecs(@RequestParam("delaySecs") delaySecs: Int) : Int{
        Thread.sleep(delaySecs*1000.toLong());
        return delaySecs;
    }
}