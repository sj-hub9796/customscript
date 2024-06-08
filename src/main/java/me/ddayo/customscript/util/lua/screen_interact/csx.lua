local _csx = {}
_csx.__index = _csx

_csx.rule = load_script("screen_interact/rule.lua")

local csx = {}
function csx:new(ev, o)
    chain_table(o.rule, _csx.rule(ev))

    return o
end

return function(ev, o)
    return csx:new(ev, o)
end