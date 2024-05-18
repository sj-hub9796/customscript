local _rule = {}
_rule.__index = _rule

function _rule:nothing_else()
    local v = false
    return function()
        if v then
            return true
        end
        v = true
        return false
    end
end

function _rule:always() return true end
function _rule:never() return false end
function _rule:wait_sec(sec)
    local current = os.clock() + sec
    return function()
        return os.clock() > current
    end
end

local rule = {}
function rule:new(ev)
    local o = { ev = ev }
    setmetatable(o, _rule)
    return o
end

return function(ev)
    return rule:new(ev)
end