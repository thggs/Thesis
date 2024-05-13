local side = "back"

local function openDepot()
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.black))
end

local function closeDepot()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.black))
end

local function start()
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.gray))
end

local function stop()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.gray))
end

local function move_right()
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.lightBlue))
end

local function move_left()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.lightBlue))
end

local function getSensor()
    local bool = rs.testBundledInput(side, colors.combine(colors.blue))
    return bool
end

return {openDepot = openDepot, closeDepot = closeDepot, start = start, stop = stop, move_left = move_left, move_right = move_right, getSensor = getSensor}
