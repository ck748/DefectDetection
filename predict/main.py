import base64
import json
import cv2
import numpy as np
import torch
from flask import Flask, request

# 在导入YOLO之前，注册YOLOv10模型类并配置torch加载设置
from ultralytics.nn.tasks import DetectionModel

# 创建YOLOv10DetectionModel别名（兼容性处理）
class YOLOv10DetectionModel(DetectionModel):
    pass

# 将类注册到模块
import sys
sys.modules['ultralytics.nn.tasks'].YOLOv10DetectionModel = YOLOv10DetectionModel

# 注册为安全的全局类（针对PyTorch 2.6+）
try:
    torch.serialization.add_safe_globals([YOLOv10DetectionModel])
except AttributeError:
    # 旧版本PyTorch没有这个方法
    pass

# 现在可以安全导入YOLO
from ultralytics import YOLO

app = Flask(__name__)

@app.route('/test', methods=['get'])
def test():
    return "200"

@app.route('/detect', methods=['post'])
def predict():
    try:
        img_base = request.form.get("imgurl")
        if not img_base:
            return json.dumps({"error": "图片数据为空"}, ensure_ascii=False), 400
        
        print(f"[调试] 收到图片Base64长度: {len(img_base)}")
        
        img_bt = base64.b64decode(img_base)
        image_np = np.frombuffer(img_bt, dtype=np.uint8)
        image_np2 = cv2.imdecode(image_np, cv2.IMREAD_COLOR)
        
        if image_np2 is None:
            return json.dumps({"error": "图片解码失败"}, ensure_ascii=False), 400
        
        print(f"[调试] 图片尺寸: {image_np2.shape}")
        
        # 不需要RGB转BGR，cv2.imdecode已经是BGR格式
        image = image_np2

        # 使用predict方法而不是直接调用，降低置信度阈值以检测更多目标
        predicts = model.predict(source=image, conf=0.25, verbose=False)
        
        num_detections = len(predicts[0].boxes) if predicts and len(predicts) > 0 else 0
        print(f"[调试] 检测结果: {num_detections} 个目标")
        
        # 打印每个检测框的详细信息
        if num_detections > 0:
            for idx, box in enumerate(predicts[0].boxes):
                print(f"[调试] 目标{idx+1}: 类别={box.cls.item()}, 置信度={box.conf.item():.3f}, 坐标={box.xywh.cpu().numpy().tolist()}")

        x = predicts[0].plot()
        data = cv2.imencode('.jpg', x)[1]
        image_bytes = data.tobytes()
        image_base4 = base64.b64encode(image_bytes).decode('utf8')
        result_list = []
        for box in predicts[0].boxes:
            result_list.append({"cls": box.cls.item(), "conf": box.conf.item(), "xywh": box.xywh.cpu().numpy().tolist()})

        mes = {}
        defections = []
        cate = {0.0: "划痕", 1.0: "裂痕",2.0: "划痕",3.0: "划痕",4.0: "裂痕",5.0: "裂痕",}
        for i in range(len(result_list)):
            res = {}

            res["l"] = int(result_list[i].get("xywh")[0][2] * 1000) / 1000
            res["h"] = int(result_list[i].get("xywh")[0][3] * 1000) / 1000
            res["x"] = int(result_list[i].get("xywh")[0][0] * 1000) / 1000
            res["y"] = int(result_list[i].get("xywh")[0][1] * 1000) / 1000
            res["score"] = result_list[i].get("conf")
            res["category"] = cate.get(result_list[i].get("cls"), "未知")
            defections.append(res)
        mes['imgBase64'] = image_base4
        mes['defections'] = defections
        
        print(f"[调试] 返回结果: 发现 {len(defections)} 个缺陷")
        return json.dumps(mes, ensure_ascii=False)
    except Exception as e:
        import traceback
        error_msg = f"错误: {str(e)}\n{traceback.format_exc()}"
        print(f"[错误] {error_msg}")
        return json.dumps({"error": str(e)}, ensure_ascii=False), 500

if __name__ == '__main__':
    model = YOLO('v10best.pt')
    app.run(port=8090, host='0.0.0.0')

    