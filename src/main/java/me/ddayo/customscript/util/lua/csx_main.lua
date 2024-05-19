or_invoke = function(v)
    if type(v) == "function" then return v() else return v end
end

chain_table = function(base, with)
    local mt = getmetatable(base) or {}
    setmetatable(base, {
        __index = function(t, k)
            local tr
            if type(mt.__index) == 'table' then tr = mt.__index[k] else tr = mt.__index(t, k) end
            return tr or with[k]
        end
    })
end

load_script = function(dir)
    dir = base_dir .. '/' .. dir
    return loadfile(dir)()
end

local share_global_list = {}
share_global = function(name)
    share_global_list[name] = _G[name]
end

load_core_api = function(sc, name)
    load(sc, name, "t")()
end

local core_lib = {}
load_core = function(sc, name)
    core_lib[name] = load(sc, name, "t")()
    if type(core_lib[name]) ~= 'function' then error("NIL recv") end
end

local core_lib_chain = {}
load_core_extension = function(sc, name)
    if core_lib_chain[name] == nil then core_lib_chain[name] = List:new() end
    core_lib_chain[name]:push(load(sc, name, "t")())
end

load_scx = function(sc, name, ev_hook)
    local ev = {
        next = next,
        pairs = pairs,
        select = select,
        tonumber = tonumber,
        tostring = tostring,
        type = type,
        xpcall = xpcall,
        pcall = pcall,
        coroutine = {
            create = coroutine.create,
            resume = coroutine.resume,
            yield = yield_evt,
        },
        string = {
            byte = string.byte,
            char = string.char,
            find = string.find,
            format = string.format,
            gmatch = string.gmatch,
            gsub = string.gsub,
            len = string.len,
            lower = string.lower,
            match = string.match,
            rep = string.rep,
            reverse = string.reverse,
            sub = string.sub,
            upper = string.upper,
        },
        table = {
            insert = table.insert,
            maxn = table.maxn,
            remove = table.remove,
            sort = table.sort
        },
        math = {
            abs = math.abs,
            acos = math.acos,
            asin = math.asin,
            atan = math.atan,
            ceil = math.ceil,
            cos = math.cos,
            deg = math.deg,
            exp = math.exp,
            floor = math.floor,
            fmod = math.fmod,
            huge = math.huge,
            log = math.log,
            max = math.max,
            min = math.min,
            modf = math.modf,
            pi = math.pi,
            rad = math.rad,
            random = math.random,
            sin = math.sin,
            sqrt = math.sqrt,
            tan = math.tan
        },
        os = {
            clock = os.clock,
            date = os.date,
            difftime = os.difftime,
            time = os.time
        }
    }
    for k, v in pairs(share_global_list) do
        ev[k] = v
    end
    for k,v in pairs(core_lib) do
        ev[k] = v(ev)
    end
    for k,v in pairs(core_lib_chain) do
        for i=1,v.size do
            chain_table(ev[k], v[i](ev, ev[k]))
        end
    end

    local efn, em = load(sc, name, "t", ev)
    if efn == nil then
        print('Cannot load ' .. name .. ' because ' .. em)
    end

    ev_hook(ev)

    ev.csx:push_stack({
        name = "begin",
        fn = efn
    })
end
