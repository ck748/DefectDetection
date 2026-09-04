#!/usr/bin/env python
# coding: utf-8
import cv2 as cv
import threading
from time import sleep
from dofbot_config import *
import ipywidgets as widgets
from IPython.display import display
from color_sorting import color_sorting
import Arm_Lib

# +++++++++++++++初始化机械臂位置+++++++++++++++
Arm = Arm_Lib.Arm_Device()
joints_0 = [90, 135, 0, 45, 90, 30]
Arm.Arm_serial_servo_write6_array(joints_0, 1000)

# +++++++++++++++创建实例,初始化参数+++++++++++++++
# 创建实例
sorting = color_sorting()
# 初始化模式
model = 'General'
# 颜色HSV阈值
color_hsv = {"red": ((0, 43, 46), (10, 255, 255)),
             "green": ((35, 43, 46), (77, 255, 255)),
             "blue": ((100, 43, 46), (124, 255, 255)),
             "yellow": ((26, 43, 46), (34, 255, 255))}
# HSV参数路径
HSV_path = "/root/dofbot_ws/src/dofbot_color_sorting/HSV_config.txt"
# 读取HSV配置文件,更新HSV值
try:
    read_HSV(HSV_path, color_hsv)
except Exception:
    print("Read HSV_config Error!!!")

# +++++++++++++++创建控件+++++++++++++++
# 创建控件布局
button_layout = widgets.Layout(width='200px', height='70px', align_self='center')
# 输出打印
output = widgets.Output()
# 退出按钮
exit_button = widgets.Button(description='Exit', button_style='danger', layout=button_layout)
# 图像控件
imgbox = widgets.Image(format='jpg', height=480, width=640, layout=widgets.Layout(align_self='center'))
# 垂直放置
controls_box = widgets.VBox([imgbox, exit_button], layout=widgets.Layout(align_self='center'))


# ['auto','flex-start', 'flex-end', 'center', 'baseline', 'stretch', 'inherit',  'initial', 'unset']

# +++++++++++++++控制按钮+++++++++++++++
def exit_button_Callback(value):
    global model
    model = 'Exit'


#    with output: print(model)
exit_button.on_click(exit_button_Callback)


# +++++++++++++++主程序+++++++++++++++
def camera():
    # 打开摄像头
    capture = cv.VideoCapture(0)
    # 当摄像头正常打开的情况下循环执行
    while capture.isOpened():
        try:
            # 读取相机的每一帧
            _, img = capture.read()
            # 统一图像大小
            img = cv.resize(img, (640, 480))
            # 获得运动信息
            img = sorting.Sorting_grap(img, color_hsv)
            if model == 'Exit':
                cv.destroyAllWindows()
                capture.release()
                break
            # 添加文字
            imgbox.value = cv.imencode('.jpg', img)[1].tobytes()
        except KeyboardInterrupt:
            capture.release()


# +++++++++++++++启动+++++++++++++++
# display(controls_box,output)
threading.Thread(target=camera, ).start()