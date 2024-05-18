local _csx = {}
_csx.__index = _csx

_csx.rule = load_script("csx/gui/rule.lua")

local csx = {}
function csx:new(ev, o)
    chain_table(o.rule, _csx.rule(ev))
end

return function(ev, o)
    return csx:new(ev, o)
end