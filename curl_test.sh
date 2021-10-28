PAYLOAD=$1
test -z "$PAYLOAD" && exit 1

curl -is -H 'Content-Type: application/json' -X PUT http://localhost:8000/getWeekSchedule -d @test/kotlin/Resources/$PAYLOAD.json
echo