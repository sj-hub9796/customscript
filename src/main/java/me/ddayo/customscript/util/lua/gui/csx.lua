local _csx = {}
_csx.__index = _csx

local renderer = java.import("me.ddayo.customscript.util.lua.RenderableLuaEngine.LuaRenderer")


local csx = {}
function csx:new(ev, o)
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