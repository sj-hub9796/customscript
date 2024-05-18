local lm = java.import('org.apache.logging.log4j.LogManager')

local _log = {}
_log.__index = _log

local get_logger = function(self)
    if self.ev == nil or self.ev.csx == nil then return lm:getLogger("lua") end
    local state = self.ev.csx:get_current_name()
    return lm:getLogger(state)
end

function _log:info(msg)
    get_logger(self):info(msg)
end

function _log:debug(msg)
    get_logger(self):debug(msg)
end

function _log:error(msg)
    get_logger(self):error(msg)
end

function _log:warn(msg)
    get_logger(self):warn(msg)
end

local log = {}
function log:new(ev, o)
    o = o or { ev = ev }
    setmetatable(o, _log)
    return o
end

logger = log:new(nil)

return function(ev)
    return log:new(ev)
end