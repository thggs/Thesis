local station_controller = require("station_controller")

local ws = assert(http.websocket("ws://192.168.1.207:1880/station_4"))

print("Connected")

parallel.waitForAny(
    function()
        while true do
            local msg = ws.receive()
            if msg == "Skill_B" then
                print("Received:", msg)
                station_controller.execute_piston()
                ws.send("done")
            end
        end
    end,

    function()
        repeat
            local _, key = os.pullEvent("key")
        until key == keys.q
        print("Disconnecting...")
        ws.close()
    end
)