local conveyor_1 = require("conveyor_interfaces/1")
local conveyor_2 = require("conveyor_interfaces/2")
local conveyor_3 = require("conveyor_interfaces/3")
local conveyor_4 = require("conveyor_interfaces/4")

local function split (inputstr, sep)
    if sep == nil then
       sep = "%s"
    end
    local t={}
    for str in string.gmatch(inputstr, "([^"..sep.."]+)") do
       table.insert(t, str)
    end
    return t
 end

local function close_all()
    conveyor_1.closeDepot()
    conveyor_4.closeDepot()
end

local function stop_all()
    conveyor_1.stop()
    conveyor_2.stop()
    conveyor_3.stop()
    conveyor_4.stop()    
end

local function start_all()
    conveyor_1.start()
    conveyor_2.start()
    conveyor_3.start()
    conveyor_4.start()    
end

local function move_right_all()
    conveyor_1.move_right()
    conveyor_2.move_right()
    conveyor_3.move_right()
    conveyor_4.move_right()
end

local function move_left_all()
    conveyor_1.move_left()
    conveyor_2.move_left()
    conveyor_3.move_left()
    conveyor_4.move_left()
end

local function controller(msg)
    local locations = split(msg, "#TOKEN#")
    local sSource = locations[1]
    local nSource = tonumber(string.match(locations[1], "%d+"))
    local sDestiny = locations[2]
    local nDestiny = tonumber(string.match(locations[2], "%d+"))

    -- This works
    if sSource == "Source" then
        move_right_all()
        conveyor_1.openDepot()
        start_all()
        local conveyor = require("conveyor_interfaces/"..nDestiny)

        while (conveyor.getSensor() == false) 
        do
            os.sleep(0.05)
            conveyor_1.closeDepot()
        end
        stop_all()
    elseif sDestiny == "Storage" then
        move_right_all()
        start_all()
        conveyor_4.openDepot()

        while (conveyor_4.getSensor() == false) 
        do
            os.sleep(0.05)
        end
        os.sleep(1)
        conveyor_4.closeDepot()
    else
        if nSource > nDestiny then
            move_left_all()
        else
            move_right_all()
        end
        start_all()
        local conveyor = require("conveyor_interfaces/"..nDestiny)
        while (conveyor.getSensor() == false) 
        do
            os.sleep(0.05)
        end
    end
    stop_all()

    return "done"
end      

return {close_all = close_all, start_all = start_all, stop_all = stop_all, controller = controller}
