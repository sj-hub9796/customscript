get_protected = function(rv)
    local ev = {}
    ev.__index = ev
    setmetatable(ev, {
        __index = function(t, k)
            local a = rawget(t, k) or rv[k]
            return a
        end,
        __newindex = function(t, k, v)
            -- if rv[k] ~= nil then error("You cannot modify `" .. k .. "` :(") end
            rawset(t, k, v)
        end
    })
    return ev
end

loadScript = function(dir)
    return loadfile(baseDir .. dir)
end

or_invoke = function(v)
    if type(v) == "function" then return v() else return v end
end

core_lib = {}
load_core_api = function(sc, name)
    load(sc, name, "t")()
end

load_core = function(sc, name)
    core_lib[name] = load(sc, name, "t")()
    if type(core_lib[name]) ~= 'function' then error("NIL recv") end
end

load_scx = function(sc, name)
    local ev
    local nev = {
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
    for k,v in pairs(core_lib) do
        nev[k] = v(nev)
        -- rawset(nev[k], "ev", ev)
    end

    ev = get_protected(nev)

    local efn, em = load(sc, name, "t", ev)
    if efn == nil then
        print('Cannot load ' .. name .. ' because ' .. em)
    end

    ev.csx:push_stack({
        name = "begin",
        fn = efn
    })
end