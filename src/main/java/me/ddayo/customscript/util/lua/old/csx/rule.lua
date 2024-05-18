local _rule = {}

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

function _rule:new(o)
    o = o or {}
    setmetatable(o, self)
    return o
end

function _rule:always() return true end
function _rule:never() return false end
function _rule:wait_sec(sec)
    local current = os.clock() + sec
    return function()
        return os.clock() > current
    end
end

return get_protected(_rule)