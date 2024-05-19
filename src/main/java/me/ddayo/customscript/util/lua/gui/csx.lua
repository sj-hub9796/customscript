local _csx = {}
_csx.__index = _csx

_csx.rule = load_script("gui/rule.lua")

local renderer = java.import("me.ddayo.customscript.util.lua.RenderableLuaEngine.LuaRenderer")


local csx = {}
function csx:new(ev, o)
    chain_table(o.rule, _csx.rule(ev))

    o.renderer = renderer()
    local push_stack = o.push_stack
    o.push_stack = function(self, frame)
        o.renderer:pushStack()
        push_stack(self, frame)
        o.renderer:popStack()
    end

    return o
end

return function(ev, o)
    return csx:new(ev, o)
end