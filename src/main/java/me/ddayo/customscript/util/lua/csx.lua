local _csx = {}

function _csx:push_stack(frame)
    if frame == nil then
        error("FRAME NIL")
    end
    if frame.name == nil then
        error("NAME NIL")
    end
    if frame.fn == nil then
        print('fn nil: ' .. frame.name)
        return
    end

    self.stack:push({ name = frame.name, evh = {} })
    frame.fn()
    self.stack:pop()
end

-- create dump of input variable 'o'
function _csx:dump(o, d)
    if type(o) == 'table' then
        local s = '{ '
        for k, v in pairs(o) do
            if d == nil then
                s = s .. '\n  '
            end
            if type(k) ~= 'number' then
                k = '"' .. k .. '"'
            end
            s = s .. '[' .. k .. '] = ' .. self:dump(v, 1) .. ','
        end
        if d == nil then
            s = s .. '\n'
        end
        return s .. '} '
    else
        return tostring(o)
    end
end

-- create dump of stack strace
function _csx:dump_stack()
    return self:dump(self.stack)
end


-- get current name of stack
function _csx:get_current_name()
    local r = '|' .. self.stack[1].name
    for x = 2, self.stack.size do
        r = r .. ' > ' .. self.stack[x].name
    end
    return r
end

-- register event on current stack
function _csx:register_event(type, callback)
    if self.stack:last().evh[type] == nil then
        self.stack:last().evh[type] = List:new()
    end
    self.stack:last().evh[type]:push(callback)
end

-- invoke event of entire stack
function _csx:invoke_event(ev_name, ...)
    for i = 1, self.stack.size do
        local v = self.stack[i].evh[ev_name]
        if v ~= nil then
            for j = 1, v.size do
                v[j](...)
            end
        end
    end
end

--[[
    register new yield
    yield_next_name is the name of frame
    rule can be function, which returns boolean value
    callback is the next frame
]]--
function _csx:request_yield_frame(yield_next_name, rule, callback)
    return { name = yield_next_name, rule = rule, fn = callback }
end

function _csx:yield_evt(...)
    local et = coroutine.yield(self.ev, ...)
    if et ~= nil then
        et(self.ev)
    end
end

--[[
    yield the execution with provided frames
]]--
function _csx:yield(...)
    local argc = select("#", ...)
    local argv = { ... }
    --[[
    for i=1,argc do
        csx_internal_yield:push(argv[i])
        print('yield, ' .. tostring(argv[i]) .. '\n')
    end
    ]]--
    while true do
        for i = 1, argc do
            if argv[i].rule() then
                self:push_stack(argv[i])
                return
            end
        end
        self:yield_evt(...)
    end
end

_csx.__index = _csx

local csx = {}
function csx:new(ev, o)
    o = o or { ev = ev, rule = self.rule:new(ev), gui = self.gui:new(ev), stack = List:new() }
    setmetatable(o, _csx)
    return o
end

return function(ev) return csx:new(ev) end
