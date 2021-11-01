# RateLimiterKotlinApplication
This a sample application which is built to test rate limiting using Bucket4J. This uses hazelcast to achieve distributed solution for a multi clustered application.

###
Required
- Simple RateLimiting (per time instance)
- Distributed Rate Limiting
- Change Limits on the fly
- Asynchronous Rate-Limiting

Good to have
- Concurrent RateLimiting
- Queue the throttled requests
- Multiple Rate-Limiting Algorithms

###
Hazel-Cast Docker run command

docker run -it --network hazelcast-network --rm -e HZ_CLUSTERNAME=rate-limiter -p 5701:5701 hazelcast/hazelcast:4.2.2

docker run --name rate-limiter -d -e JAVA_OPTS="-Dhazelcast.config=/opt/hazelcast/hazelcast.xml -Dhazelcast.ip=`ip route get 8.8.8.8 | awk '{print $NF; exit}'` -Dhazelcast.port=5701" -ti fsamir/hazelcast-bucket4j:3.9.2

