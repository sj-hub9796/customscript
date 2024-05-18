local _List = { }

function _List:new(o)
    o = o or { size = 0 }
    setmetatable(o, self)
    self.__index = self
    return o
end

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

List = get_protected(_List)