local _rule = {}

function _rule:new(ev, o)
    o = o or { ev = ev }
    setmetatable(o, self)
    return o
end

function _rule:on_mouse_click(area)
    local pressed = false
    self.ev.csx:register_event('on_mouse_release', function(p, btn)
        self.ev.log:info(p.x .. ', ' .. p.y)
        if btn == 0 then
            pressed = pressed or area:is_in(p)
        end
    end)
    return function() return pressed end
end

return get_protected(_rule)