local _point = {}
_point.__index = _point

function _point:new(x, y, o)
    o = o or {x=x or 0, y=y or 0}
    return o
end

local point = {}
setmetatable(point, _point)

local _area = {}
_area.__index = _area

function _area:new(o)
    o = o or List:new(o)
    chain_table(o, self)
    return o
end

function _area:is_in(p)
    if self.size < 3 then error("To check area, size must bigger then 3") end
    local is_in = false
    local j = self.size

    for i=1,self.size do
        if (self[i].y > p.y) ~= (self[j].y > p.y) and (p.x < (self[j].x - self[i].x) * (p.y - self[i].y) / (self[j].y - self[i].y) + self[i].x) then
            is_in = not is_in
        end
        j = i
    end
    return is_in
end

function _area:rect_area(p1, p2)
    local ps = self:new()
    ps:push(_point:new(math.min(p1.x, p2.x), math.min(p1.y, p2.y)))
    ps:push(_point:new(math.max(p1.x, p2.x), math.min(p1.y, p2.y)))
    ps:push(_point:new(math.max(p1.x, p2.x), math.max(p1.y, p2.y)))
    ps:push(_point:new(math.min(p1.x, p2.x), math.max(p1.y, p2.y)))
    return ps
end

local area = {}
setmetatable(area, _area)

local _cmath = {
    point = point,
    area = area
}
_cmath.__index = _cmath

cmath = {}
setmetatable(cmath, _cmath)
