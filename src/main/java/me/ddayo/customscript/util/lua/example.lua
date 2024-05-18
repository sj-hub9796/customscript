--[[

추가 블럭들
1. logger
2. assign(from const/server)
3. +-*/, string concat
4. event register
]]--

-- csx.rule:on_mouse_click(1, 2)
-- csx:test()
local are = cmath.area:rect_area(cmath.point:new(1, 1), cmath.point:new(2, 2))
log:info(csx:dump(are))
csx:yield(csx:request_yield_frame("click", csx.rule:on_mouse_click(are), function()
    log:info("CLICK!")
end))

log:info("Hello, world")
local k = 0

local dv = (function(t)
    k = k + (t or 1)
    end)

csx:register_event("tick", dv)

local BLK_N2 = csx:request_yield_frame("ns2", csx.rule.never, function()
    log:info("N2")
    log:info(csx:dump())
    log:info(csx:get_current_name())
    end)

local BLK_N3 = csx:request_yield_frame("ns3", csx.rule:wait_sec(10), function()
    log:warn("N3")
    end)

csx:yield(BLK_N2, BLK_N3)

log:info(csx:get_current_name())

csx:yield(csx:request_yield_frame("ns4", csx.rule:wait_sec(10), function()
    log:error("haha")
end))
