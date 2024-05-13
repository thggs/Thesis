local side = "back"

local function start()
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.pink))
end

local function stop()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.pink))
end

local function move_right()
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.magenta))
end

local function move_left()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.magenta))
end

local function getSensor()
    local bool = rs.testBundledInput(side, colors.combine(colors.purple))
    return bool
end

return {start = start, stop = stop, move_left = move_left, move_right = move_right, getSensor = getSensor}