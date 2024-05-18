local _gui = { rule = loadScript("csx/gui/rule.lua")() }

function _gui:new(ev, o)
    o = o or { ev = ev, rule = self.rule:new(ev) }
    setmetatable(o, self)
    return o
end

return get_protected(_gui)