local lm = java.import('org.apache.logging.log4j.LogManager')

local _log = {}
function _log:new(ev, o)
    o = o or { ev = ev }
    setmetatable(o, self)
    self.__index = self
    return o
end

local get_logger = function(self)
    if self.ev.csx == nil then return lm:getLogger("lua") end
    return lm:getLogger(self.ev.csx:get_current_name())
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

local log = get_protected(_log)
return function(ev)
    return log:new(ev)
end