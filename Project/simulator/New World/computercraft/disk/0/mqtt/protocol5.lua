--[[

Here is a MQTT v5.0 protocol implementation

MQTT v5.0 documentation (DOC):
	http://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html

]]

-- module table
local protocol5 = {}

-- load required stuff
local type = type
local error = error
local assert = assert
local ipairs = ipairs
local require = require
local tostring = tostring
local setmetatable = setmetatable

local table = require("table")
local unpack = table.unpack or unpack
local tbl_sort = table.sort

local string = require("string")
local str_char = string.char
local fmt = string.format

local const = require("mqtt.const")
local const_v50 = const.v50

local tools = require("mqtt.tools")
local sortedpairs = tools.sortedpairs

local bit = require("bit32")
local bor = bit.bor
local band = bit.band
local lshift = bit.lshift
local rshift = bit.rshift

local protocol = require("mqtt.protocol")
local make_uint8 = protocol.make_uint8
local make_uint16 = protocol.make_uint16
local make_uint32 = protocol.make_uint32
local make_string = protocol.make_string
local make_var_length = protocol.make_var_length
local parse_var_length = protocol.parse_var_length
local make_uint8_0_or_1 = protocol.make_uint8_0_or_1
local make_uint16_nonzero = protocol.make_uint16_nonzero
local make_var_length_nonzero = protocol.make_var_length_nonzero
local parse_string = protocol.parse_string
local parse_uint8 = protocol.parse_uint8
local parse_uint8_0_or_1 = protocol.parse_uint8_0_or_1
local parse_uint16 = protocol.parse_uint16
local parse_uint16_nonzero = protocol.parse_uint16_nonzero
local parse_uint32 = protocol.parse_uint32
local parse_var_length_nonzero = protocol.parse_var_length_nonzero
local make_header = protocol.make_header
local check_qos = protocol.check_qos
local check_packet_id = protocol.check_packet_id
local combine = protocol.combine
local packet_type = protocol.packet_type
local packet_mt = protocol.packet_mt
local connack_packet_mt = protocol.connack_packet_mt
local start_parse_packet = protocol.start_parse_packet
local parse_packet_connect_input = protocol.parse_packet_connect_input

-- Returns true if given value is a valid Retain Handling option, DOC: 3.8.3.1 Subscription Options
local function check_retain_handling(val)
	return (val == 0) or (val == 1) or (val == 2)
end

-- Create Connect Flags data, DOC: 3.1.2.3 Connect Flags
local function make_connect_flags(args)
	local byte = 0 -- bit 0 should be zero
	-- DOC: 3.1.2.4 Clean Start
	if args.clean ~= nil then
		assert(type(args.clean) == "boolean", "expecting .clean to be a boolean")
		if args.clean then
			byte = bor(byte, lshift(1, 1))
		end
	end
	-- DOC: 3.1.2.5 Will Flag
	if args.will ~= nil then
		-- check required args are presented
		assert(type(args.will) == "table", "expecting .will to be a table")
		assert(type(args.will.payload) == "string", "expecting .will.payload to be a string")
		assert(type(args.will.topic) == "string", "expecting .will.topic to be a string")
		assert(type(args.will.qos) == "number", "expecting .will.qos to be a number")
		assert(check_qos(args.will.qos), "expecting .will.qos to be a valid QoS value")
		assert(type(args.will.retain) == "boolean", "expecting .will.retain to be a boolean")
		if args.will.properties ~= nil then
			assert(type(args.will.properties) == "table", "expecting .will.properties to be a table")
		end
		if args.will.user_properties ~= nil then
			assert(type(args.will.user_properties) == "table", "expecting .will.user_properties to be a table")
		end
		-- will flag should be set to 1
		byte = bor(byte, lshift(1, 2))
		-- DOC: 3.1.2.6 Will QoS
		byte = bor(byte, lshift(args.will.qos, 3))
		-- DOC: 3.1.2.7 Will Retain
		if args.will.retain then
			byte = bor(byte, lshift(1, 5))
		end
	end
	-- DOC: 3.1.2.8 User Name Flag
	if args.username ~= nil then
		assert(type(args.username) == "string", "expecting .username to be a string")
		byte = bor(byte, lshift(1, 7))
	end
	-- DOC: 3.1.2.9 Password Flag
	if args.password ~= nil then
		assert(type(args.password) == "string", "expecting .password to be a string")
		assert(args.username, "the .username is required to set .password")
		byte = bor(byte, lshift(1, 6))
	end
	return make_uint8(byte)
end

-- Known property names and its identifiers, DOC: 2.2.2.2 Property
local property_pairs = {
	{
		0x01,
		"payload_format_indicator",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x02,
		"message_expiry_interval",
		make = make_uint32,
		parse = parse_uint32,
	},
	{
		0x03,
		"content_type",
		make = make_string,
		parse = parse_string,
	},
	{
		0x08,
		"response_topic",
		make = make_string,
		parse = parse_string,
	},
	{
		0x09,
		"correlation_data",
		make = make_string,
		parse = parse_string,
	},
	{
		0x0B,
		"subscription_identifiers",
		make = function(value) return str_char(make_var_length_nonzero(value)) end,
		parse = parse_var_length_nonzero,
		multiple = true,
	},
	{
		0x11,
		"session_expiry_interval",
		make = make_uint32,
		parse = parse_uint32,
	},
	{
		0x12,
		"assigned_client_identifier",
		make = make_string,
		parse = parse_string,
	},
	{
		0x13,
		"server_keep_alive",
		make = make_uint16,
		parse = parse_uint16,
	},
	{
		0x15,
		"authentication_method",
		make = make_string,
		parse = parse_string,
	},
	{
		0x16,
		"authentication_data",
		make = make_string,
		parse = parse_string,
	},
	{
		0x17,
		"request_problem_information",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x18,
		"will_delay_interval",
		make = make_uint32,
		parse = parse_uint32,
	},
	{
		0x19,
		"request_response_information",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x1A,
		"response_information",
		make = make_string,
		parse = parse_string,
	},
	{
		0x1C,
		"server_reference",
		make = make_string,
		parse = parse_string,
	},
	{
		0x1F,
		"reason_string",
		make = make_string,
		parse = parse_string,
	},
	{
		0x21,
		"receive_maximum",
		make = make_uint16,
		parse = parse_uint16,
	},
	{
		0x22,
		"topic_alias_maximum",
		make = make_uint16,
		parse = parse_uint16,
	},
	{
		0x23,
		"topic_alias",
		make = make_uint16_nonzero,
		parse = parse_uint16_nonzero,
	},
	{
		0x24,
		"maximum_qos",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x25,
		"retain_available",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x26,
		"user_property",  -- NOTE: not implemented intentionally
		make = function(value_) error("not implemented") end,
		parse = function(read_func_) error("not implemented") end,
	},
	{
		0x27,
		"maximum_packet_size",
		make = make_uint32,
		parse = parse_uint32,
	},
	{
		0x28,
		"wildcard_subscription_available",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x29,
		"subscription_identifiers_available",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
	{
		0x2A,
		"shared_subscription_available",
		make = make_uint8_0_or_1,
		parse = parse_uint8_0_or_1,
	},
}

-- properties table with keys in two directions: from name to identifier and back
local properties = {}
-- table with property value make functions
local property_make = {}
-- table with property value parse function
local property_parse = {}
-- table with property multiple flag
local property_multiple = {}
-- fill the properties and property_make tables
for _, prop in ipairs(property_pairs) do
	properties[prop[2]] = prop[1]           -- name ==> identifier
	properties[prop[1]] = prop[2]           -- identifier ==> name
	property_make[prop[1]] = prop.make      -- identifier ==> make function
	property_parse[prop[1]] = prop.parse    -- identifier ==> make function
	property_multiple[prop[1]] = prop.multiple -- identifier ==> multiple flag
end

-- Allowed properties per packet type
local allowed_properties = {
	[packet_type.CONNECT] = {
		[0x11] = true, -- DOC: 3.1.2.11.2 Session Expiry Interval
		[0x21] = true, -- DOC: 3.1.2.11.3 Receive Maximum
		[0x27] = true, -- DOC: 3.1.2.11.4 Maximum Packet Size
		[0x22] = true, -- DOC: 3.1.2.11.5 Topic Alias Maximum
		[0x19] = true, -- DOC: 3.1.2.11.6 Request Response Information
		[0x17] = true, -- DOC: 3.1.2.11.7 Request Problem Information
		[0x26] = true, -- DOC: 3.1.2.11.8 User Property
		[0x15] = true, -- DOC: 3.1.2.11.9 Authentication Method
		[0x16] = true, -- DOC: 3.1.2.11.10 Authentication Data
	},
	[packet_type.CONNACK] = {
		[0x11] = true, -- DOC: 3.2.2.3.2 Session Expiry Interval
		[0x21] = true, -- DOC: 3.2.2.3.3 Receive Maximum
		[0x24] = true, -- DOC: 3.2.2.3.4 Maximum QoS
		[0x25] = true, -- DOC: 3.2.2.3.5 Retain Available
		[0x27] = true, -- DOC: 3.2.2.3.6 Maximum Packet Size
		[0x12] = true, -- DOC: 3.2.2.3.7 Assigned Client Identifier
		[0x22] = true, -- DOC: 3.2.2.3.8 Topic Alias Maximum
		[0x1F] = true, -- DOC: 3.2.2.3.9 Reason String
		[0x26] = true, -- DOC: 3.2.2.3.10 User Property
		[0x28] = true, -- DOC: 3.2.2.3.11 Wildcard Subscription Available
		[0x29] = true, -- DOC: 3.2.2.3.12 Subscription Identifiers Available
		[0x2A] = true, -- DOC: 3.2.2.3.13 Shared Subscription Available
		[0x13] = true, -- DOC: 3.2.2.3.14 Server Keep Alive
		[0x1A] = true, -- DOC: 3.2.2.3.15 Response Information
		[0x1C] = true, -- DOC: 3.2.2.3.16 Server Reference
		[0x15] = true, -- DOC: 3.2.2.3.17 Authentication Method
		[0x16] = true, -- DOC: 3.2.2.3.18 Authentication Data
	},
	[packet_type.PUBLISH] = {
		[0x01] = true, -- DOC: 3.3.2.3.2 Payload Format Indicator
		[0x02] = true, -- DOC: 3.3.2.3.3 Message Expiry Interval
		[0x23] = true, -- DOC: 3.3.2.3.4 Topic Alias
		[0x08] = true, -- DOC: 3.3.2.3.5 Response Topic
		[0x09] = true, -- DOC: 3.3.2.3.6 Correlation Data
		[0x26] = true, -- DOC: 3.3.2.3.7 User Property
		[0x0B] = true, -- DOC: 3.3.2.3.8 Subscription Identifier
		[0x03] = true, -- DOC: 3.3.2.3.9 Content Type
	},
	will = {
		[0x18] = true, -- DOC: 3.1.3.2.2 Will Delay Interval
		[0x01] = true, -- DOC: 3.1.3.2.3 Payload Format Indicator
		[0x02] = true, -- DOC: 3.1.3.2.4 Message Expiry Interval
		[0x03] = true, -- DOC: 3.1.3.2.5 Content Type
		[0x08] = true, -- DOC: 3.1.3.2.6 Response Topic
		[0x09] = true, -- DOC: 3.1.3.2.7 Correlation Data
		[0x26] = true, -- DOC: 3.1.3.2.8 User Property
	},
	[packet_type.PUBACK] = {
		[0x1F] = true, -- DOC: 3.4.2.2.2 Reason String
		[0x26] = true, -- DOC: 3.4.2.2.3 User Property
	},
	[packet_type.PUBREC] = {
		[0x1F] = true, -- DOC: 3.5.2.2.2 Reason String
		[0x26] = true, -- DOC: 3.5.2.2.3 User Property
	},
	[packet_type.PUBREL] = {
		[0x1F] = true, -- DOC: 3.6.2.2.2 Reason String
		[0x26] = true, -- DOC: 3.6.2.2.3 User Property
	},
	[packet_type.PUBCOMP] = {
		[0x1F] = true, -- DOC: 3.7.2.2.2 Reason String
		[0x26] = true, -- DOC: 3.7.2.2.3 User Property
	},
	[packet_type.SUBSCRIBE] = {
		[0x0B] = { multiple = false }, -- DOC: 3.8.2.1.2 Subscription Identifier -- DOC: It is a Protocol Error to include the Subscription Identifier more than once.
		[0x26] = true,         -- DOC: 3.8.2.1.3 User Property
	},
	[packet_type.SUBACK] = {
		[0x1F] = true, -- DOC: 3.9.2.1.2 Reason String
		[0x26] = true, -- DOC: 3.9.2.1.3 User Property
	},
	[packet_type.UNSUBSCRIBE] = {
		[0x26] = true, -- DOC: 3.10.2.1.2 User Property
	},
	[packet_type.UNSUBACK] = {
		[0x1F] = true, -- DOC: 3.11.2.1.2 Reason String
		[0x26] = true, -- DOC: 3.11.2.1.3 User Property
	},
	-- NOTE: PINGREQ (3.12), PINGRESP (3.13) has no properties
	[packet_type.DISCONNECT] = {
		[0x11] = true, -- DOC: 3.14.2.2.2 Session Expiry Interval
		[0x1F] = true, -- DOC: 3.14.2.2.3 Reason String
		[0x26] = true, -- DOC: 3.14.2.2.4 User Property
		[0x1C] = true, -- DOC: 3.14.2.2.5 Server Reference
	},
	[packet_type.AUTH] = {
		[0x15] = true, -- DOC: 3.15.2.2.2 Authentication Method
		[0x16] = true, -- DOC: 3.15.2.2.3 Authentication Data
		[0x1F] = true, -- DOC: 3.15.2.2.4 Reason String
		[0x26] = true, -- DOC: 3.15.2.2.5 User Property
	},
}

-- Create properties field for various control packets, DOC: 2.2.2 Properties
local function make_properties(ptype, args)
	local allowed = assert(allowed_properties[ptype], "invalid packet type to detect allowed properties")
	local props = ""
	local uprop_id = properties.user_property
	-- writing known properties
	if args.properties ~= nil then
		assert(type(args.properties) == "table", "expecting .properties to be a table")
		-- validate all properties and append them to order list
		local order = {}
		for name, value in sortedpairs(args.properties) do
			assert(type(name) == "string", "expecting property name to be a string: " .. tostring(name))
			-- detect property identifier and check it's allowed for that packet type
			local prop_id = assert(properties[name], "unknown property: " .. tostring(name))
			assert(prop_id ~= uprop_id, "user properties should be passed in .user_properties table")
			if not allowed[prop_id] then
				error("property " .. name .. " is not allowed for packet type " .. packet_type[ptype])
			end
			order[#order + 1] = { prop_id, name, value }
		end
		-- sort props in the identifier ascending order
		tbl_sort(order, function(a, b) return a[1] < b[1] end)
		for _, item in ipairs(order) do
			local prop_id, name, value = unpack(item)
			if property_multiple[prop_id] then
				assert(type(value) == "table", "expecting list-table for property with multiple value")
				assert(#value == 1, "only one value for multiple-property supported")
				value = value[1]
			end
			-- make property data
			local ok, val = pcall(property_make[prop_id], value)
			if not ok then
				error("invalid property value: " .. name .. " = " .. tostring(value) .. ": " .. tostring(val))
			end
			local prop = combine(
				str_char(make_var_length(prop_id)),
				val
			)
			-- and append it to props
			if type(props) == "string" then
				props = combine(prop)
			else
				props:append(prop)
			end
		end
	end
	-- writing userproperties
	if args.user_properties ~= nil then
		assert(type(args.user_properties) == "table", "expecting .user_properties to be a table")
		assert(allowed[uprop_id], "user_property is not allowed for packet type " .. ptype)
		local order = {}
		local dups = {}
		if args.user_properties[1] then
			-- at first use array items as they given as {name, value} pairs with stable order
			for i, pair in ipairs(args.user_properties) do
				-- validate types for name and value
				if type(pair) ~= "table" then
					error(fmt("user property at position %d should be {name, value} table", i))
				end
				if type(pair[1]) ~= "string" then
					error(fmt("user property name at position %d should be a string", i))
				end
				if type(pair[2]) ~= "string" then
					error(fmt("user property '%s' value at position %d should be a string", pair[1], i))
				end
				order[i] = pair
				dups[pair[1]] = pair[2]
			end
		end
		-- now add the rest of user properties given as string table keys
		for name, val in sortedpairs(args.user_properties) do
			if type(name) ~= "number" then -- skipping number keys as they already added above
				-- validate types for name and value
				if type(name) ~= "string" then
					error(fmt("user property name '%s' should be a string", name))
				end
				if type(val) ~= "string" then
					error(fmt("user property '%s' value '%s' should be a string", name, val))
				end
				-- check that name+value key already added
				if dups[name] ~= val then
					order[#order + 1] = { name, val }
				end
			end
		end
		for _, pair in ipairs(order) do
			local name = pair[1]
			local value = pair[2]
			-- make user property data
			local prop = combine(
				str_char(make_var_length(uprop_id)),
				make_string(name),
				make_string(value)
			)
			-- and append it to props
			if type(props) == "string" then
				props = combine(prop)
			else
				props:append(prop)
			end
		end
	end
	-- and combine properties with its length field
	return combine(
		str_char(make_var_length(props:len())), -- DOC: 2.2.2.1 Property Length
		props                             -- DOC: 2.2.2.2 Property
	)
end

-- Create CONNECT packet, DOC: 3.1 CONNECT ? Connection Request
local function make_packet_connect(args)
	-- check args
	assert(type(args.id) == "string", "expecting .id to be a string with MQTT client id")
	-- DOC: 3.1.2.10 Keep Alive
	local keep_alive_ival = 0
	if args.keep_alive then
		assert(type(args.keep_alive) == "number")
		keep_alive_ival = args.keep_alive
	end
	-- DOC: 3.1.2.11 CONNECT Properties
	local props = make_properties(packet_type.CONNECT, args)
	-- DOC: 3.1.2 CONNECT Variable Header
	local variable_head