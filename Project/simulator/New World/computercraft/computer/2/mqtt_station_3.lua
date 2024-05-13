local mqtt = require("mqtt")

local station_controller = require("station_controller")

local keep_alive = 60

local qos = 2

local topic_requests = "station_3_request"
local topic_responses = "station_3_response"

-- create mqtt client
local client = mqtt.client {
    -- NOTE: this broker is not working sometimes; comment username = "..." below if you still want to use it
    -- uri = "test.mosquitto.org",
    uri = "ws://192.168.1.207:9001/mqtt",
    clean = true,
    keep_alive = keep_alive
}

print("created MQTT client", client)

client:on {
    connect = function(connack)
        if connack.rc ~= 0 then
            print("connection to broker failed:", connack:reason_string(), connack)
            return
        end
        print("connected:", connack) -- successful connection

        -- subscribe to station_3 topic
        assert(client:subscribe { topic = topic_requests, qos = qos, callback = function(suback)
            print("subscribed:", topic_requests)            
        end })
    end,

    message = function(msg)
        assert(client:acknowledge(msg))
        local msgPayload = msg.payload
        -- Ignore "done" message
        if msgPayload ~= "done" then
            if msgPayload == "Skill_A" then
                print("received:", msgPayload)

                station_controller.execute()
        
                assert(client:publish {
                    topic = topic_responses,
                    payload = "done",
                    qos = qos
                })
            end
        end
    end,

    error = function(err)
        print("MQTT client error:", err)
    end,

    close = function()
        print("MQTT conn closed")
    end
}


parallel.waitForAny(
    function()
        -- run io loop for client until connection close
        -- please note that in sync mode background PINGREQ's are not available, and automatic reconnects too
        print("running client in synchronous input/output loop")
        mqtt.run_sync(client)
        print("done, synchronous input/output loop is stopped")
    end,
    function()
        while true do
            os.sleep(keep_alive)
            client:send_pingreq()
        end
    end
)