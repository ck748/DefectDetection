#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import socket
import time
import sys
import struct


AGV_SERIAL_PORT = "COM9"
AGV_BAUDRATE = 9600
SERIAL_TIMEOUT = 2


GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
CYAN = "\033[96m"
RESET = "\033[0m"
BOLD = "\033[1m"


def print_header(title):
    print(f"\n{BOLD}{CYAN}  {title}{RESET}")


def print_ok(msg):
    print(f"  {GREEN}[PASS]{RESET} {msg}")


def print_fail(msg):
    print(f"  {RED}[FAIL]{RESET} {msg}")


def print_info(msg):
    print(f"  {YELLOW}[INFO]{RESET} {msg}")


def test_agv():
    print_header("1. AGV 小车测试 ")
    print_info("检测可用串口...")
    try:
        import serial.tools.list_ports
        ports = list(serial.tools.list_ports.comports())
        if ports:
            print_ok(f"发现 {len(ports)} 个串口:")
            for p in ports:
                print(f"       - {p.device}: {p.description}")
        else:
            print_fail("未发现任何串口")
            return False
    except ImportError:
        print_info("pyserial 未安装，跳过串口列表 (pip install pyserial)")
    except Exception as e:
        print_fail(f"串口检测异常: {e}")

    print_info(f"尝试连接串口 {AGV_SERIAL_PORT} @ {AGV_BAUDRATE}...")
    try:
        import serial
        ser = serial.Serial(AGV_SERIAL_PORT, AGV_BAUDRATE, timeout=SERIAL_TIMEOUT)
        print_ok(f"串口 {AGV_SERIAL_PORT} 打开成功")

        query = bytes([0xA5, 0x5A, 0x06, 0x26, 0xFF])
        checksum = sum(query) & 0xFF
        frame = query + bytes([checksum])
        print_info("发送状态查询指令...")
        ser.write(frame)
        time.sleep(0.5)

        # 读取响应
        if ser.in_waiting > 0:
            data = ser.read(ser.in_waiting)
            print_ok(f"收到响应 {len(data)} 字节: {data.hex().upper()}")

            # 尝试解析状态帧 (0x25)
            if len(data) >= 10:
                # 查找 0xA5 0x5A 帧头
                for i in range(len(data) - 1):
                    if data[i] == 0xA5 and data[i+1] == 0x5A:
                        cmd = data[i+2] if i+2 < len(data) else 0
                        if cmd == 0x25:
                            print_ok("解析到状态帧 (0x25)")
                            # 解析电池电量 (偏移量根据实际协议)
                            if len(data) > i + 10:
                                battery = data[i+4] if i+4 < len(data) else 0
                                speed = data[i+5] if i+5 < len(data) else 0
                                print_info(f"  电池电量: {battery}%")
                                print_info(f"  实时速度: {speed}")
                        break
        else:
            print_fail("未收到 AGV 响应（检查串口接线和 AGV 电源）")

        ser.close()
        print_ok("串口已关闭")
        return True

    except ImportError:
        print_fail("pyserial 未安装: pip install pyserial")
        return False
    except serial.SerialException as e:
        print_fail(f"串口连接失败: {e}")
        print_info(f"请检查: 1) 串口名称是否正确  2) AGV 是否上电  3) 串口是否被占用")
        return False
    except Exception as e:
        print_fail(f"AGV 测试异常: {e}")
        return False


def main():
    print(f"\n{BOLD}{CYAN}")
    print(" AGV小车连通性测试")
    print(f"{RESET}")
    print(f"  测试时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")

    results = {}
    results['AGV'] = test_agv()

    print_header("测试汇总")
    all_pass = True
    for name, ok in results.items():
        status = f"{GREEN}正常{RESET}" if ok else f"{RED}异常{RESET}"
        icon = "[OK]" if ok else "[NG]"
        print(f"  {icon} {name}: {status}")
        if not ok:
            all_pass = False
    print()
    if all_pass:
        print(f"  {GREEN}{BOLD}AGV设备通信正常！{RESET}")
    else:
        print(f"  {YELLOW}{BOLD}AGV设备异常，请检查上述失败项{RESET}")
    print()


if __name__ == "__main__":
    main()
