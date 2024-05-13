local side = "back"

local function startup()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.white))
end

local function execute_piston()
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.white))
    os.sleep(2)
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.white))
end

local function execute_deployer()
    rs.setBundledOutput(side, colors.subtract(rs.getBundledOutput(side), colors.white))
    os.sleep(2)
    rs.setBundledOutput(side, colors.combine(rs.getBundledOutput(side), colors.white))
end

return{startup = startup, execute_piston = execute_piston, execute_deployer = execute_deployer}