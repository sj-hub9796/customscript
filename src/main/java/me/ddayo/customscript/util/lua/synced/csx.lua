local si = java.import("me.ddayo.customscript.util.lua.FromServer")

from_server = function(v)
    return si:get(v)
end

from_server_n = function(v)
    return si:get_number(v)
end
