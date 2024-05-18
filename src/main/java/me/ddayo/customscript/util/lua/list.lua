local _List = {}
_List.__index = _List

function _List:push(v)
    self.size = self.size + 1
    self[self.size] = v
end

function _List:last()
    return self[self.size]
end

function _List:pop()
    if self.size == nil or self.size == 0 then
        error("Cannot pop")
    end
    self[self.size] = nil
    self.size = self.size - 1
end

function _List:clear()
    for i = 1, self.size do
        self[i] = nil
    end
    self.size = 0
end

List = {}
function List:new(o)
    o = o or {size = 0}
    setmetatable(o, _List)
    return o
end
