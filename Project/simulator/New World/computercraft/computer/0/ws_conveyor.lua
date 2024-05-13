local controller = require("conveyor_controller")
local url = "ws://192.168.1.207:1880/conveyor"

controller.stop_all()
controller.close_all()

local ws = http.websocket(url)

print("Connected")

parallel.waitForAny(
    function()
        while true do
            local msg = ws.receive()
            controller.controller(msg)
            ws.send("done")
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