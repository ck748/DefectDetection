import struct

########################
# VisionMaster Send Event Script - Send detection results to AUBO robot
# handleMessage return value will be sent via TCP port 30002 to AUBO controller
# AUBO requires: return function(api) ... end format, ending with \r\n\r\n
########################

def getInputParam():
    """
    Define input params from VM flow's send data module
    :return: dict, key=param name (max 32 chars), value=type (string/int/float/byte)
    """
    params = {}
    params['result'] = 'string'    # Detection result: OK or NG
    params['pos_x'] = 'float'      # Defect X position (mm)
    params['pos_y'] = 'float'      # Defect Y position (mm)
    return params


def handleMessage(list):
    """
    Process input params and assemble AUBO Lua script to send via TCP 30002
    :param list: input param list, order matches getInputParam definition
    :return: bytes - AUBO Lua script ending with \\r\\n\\r\\n
    """
    result = str(list[0])   # OK or NG
    pos_x = list[1]          # float
    pos_y = list[2]          # float

    if result == 'NG':
        # NG: toggle DO00 to signal defect detected
        lua_script = (
            "return function(api)\r\n"
            "    local _ENV = require('aubo').sched.select_robot(1)\r\n"
            "    setDO(0, true)\r\n"
            "end\r\n"
            "\r\n"
        )
    else:
        # OK: toggle DO00 off
        lua_script = (
            "return function(api)\r\n"
            "    local _ENV = require('aubo').sched.select_robot(1)\r\n"
            "    setDO(0, false)\r\n"
            "end\r\n"
            "\r\n"
        )

    return lua_script.encode('utf-8')
