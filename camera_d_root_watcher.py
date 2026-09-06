"""
Windows 本地 D 盘根目录相机抓拍自动上传脚本
功能：
1. 只监听 D 盘根目录（D:\\），不扫描任何子文件夹，轻量无感、零卡顿。
2. 发现相机抓拍新图片（.jpg / .png / .jpeg），秒级自动上传至服务器接口。
3. 服务器接口自动将图片存入你在前端页面选择的【监听目录】，并完成数据库入库。
4. 前端【摄像头目录监听】大屏毫秒级自动渲染呈现该图片。
"""

import os
import time
import requests

# =================== 配置区域 ===================
# 1. 本地监听路径：固定锁定 D 盘根目录
WATCH_DIR = r"D:\\"

# 2. 支持的图片格式
IMAGE_EXTS = {".jpg", ".jpeg", ".png"}

# 3. 服务器上传接口地址（已配置为现场真实的 192.168.1.3:8081）
UPLOAD_URL = "http://192.168.1.3:8081/cameraWatch/upload"

# 4. 轮询间隔（秒）：0.8秒检查一次根目录，反应迅速且不占 CPU
POLL_INTERVAL = 0.8
# ===============================================

def scan_root_images(directory):
    """仅列出根目录下的图片文件，绝不递归遍历子目录"""
    images = set()
    try:
        # os.scandir 只读取当前目录一级，性能极高
        with os.scandir(directory) as entries:
            for entry in entries:
                if entry.is_file():
                    ext = os.path.splitext(entry.name)[1].lower()
                    if ext in IMAGE_EXTS:
                        images.add(entry.name)
    except Exception as e:
        print(f"⚠️ 读取目录异常: {e}")
    return images

def upload_image(file_path, file_name):
    """调用系统现成的上传接口将图片发送给服务器"""
    try:
        # 等待文件完全写入完成（防止小米相机正在写入时读取导致损坏）
        prev_size = -1
        for _ in range(5):
            if not os.path.exists(file_path):
                return False
            curr_size = os.path.getsize(file_path)
            if curr_size > 0 and curr_size == prev_size:
                break
            prev_size = curr_size
            time.sleep(0.2)

        with open(file_path, 'rb') as f:
            files = {'image': (file_name, f, 'image/jpeg')}
            data = {'fileName': file_name}
            print(f"🚀 [正在上传] -> {file_name} ...")
            resp = requests.post(UPLOAD_URL, files=files, data=data, timeout=5)

            if resp.status_code == 200:
                res_json = resp.json()
                if res_json.get('code') in [200, 1, '200']:
                    print(f"✅ [上传成功] -> {file_name} 已存入服务器并在前端渲染！")
                    return True
                else:
                    print(f"❌ [上传被拒绝] -> {res_json.get('message') or res_json.get('msg')}")
            else:
                print(f"❌ [请求失败] -> HTTP {resp.status_code}")
    except Exception as ex:
        print(f"❌ [上传异常] -> {ex}")
    return False

def main():
    print("=" * 60)
    print(f"🔍 小米相机 D 盘根目录实时监听与上传中枢已启动")
    print(f"📁 监听目标: {WATCH_DIR} (仅限单层，不碰任何子文件夹)")
    print(f"🌐 目标服务器接口: {UPLOAD_URL}")
    print("=" * 60)

    # 启动时先记录当前根目录下已存在的文件，避免历史文件被重复上传
    processed_files = scan_root_images(WATCH_DIR)
    print(f"ℹ️ 当前 D 盘根目录已有图片: {len(processed_files)} 张（已标记，忽略历史图片）")
    print("🎯 等待相机产生新抓拍照片...")

    while True:
        try:
            current_files = scan_root_images(WATCH_DIR)
            new_files = current_files - processed_files

            for file_name in new_files:
                full_path = os.path.join(WATCH_DIR, file_name)
                # 触发自动上传
                success = upload_image(full_path, file_name)
                # 无论成功或失败均记录，防止重复上传刷屏
                processed_files.add(file_name)

            time.sleep(POLL_INTERVAL)
        except KeyboardInterrupt:
            print("\n🛑 监听已退出。")
            break
        except Exception as e:
            print(f"⚠️ 轮询错误: {e}")
            time.sleep(2)

if __name__ == "__main__":
    main()
