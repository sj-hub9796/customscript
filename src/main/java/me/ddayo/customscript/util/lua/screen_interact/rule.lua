local _rule = {}
_rule.__index = _rule

function _rule:on_mouse_click(area)
    local pressed = false
    self.ev.csx:register_event('on_mouse_release', function(p, btn)
        if btn == 0 then
            pressed = pressed or area:is_in(p)
        end
    end)
    return function() return pressed end
end

local rule = {}
function rule:new(ev, o)
    o = o or {}
    setmetatable(o, _rule)
    return o
end

return function(ev)
    return rule:new(ev)
end
