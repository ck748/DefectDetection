package com.ggbond.defectdetection.software.image;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ggbond.defectdetection.dto.DetectResDto;
import com.ggbond.defectdetection.pojo.Defection;
import com.ggbond.defectdetection.software.common.ConfigProperties;
import com.ggbond.defectdetection.util.ImgUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 工作的模型类
 * <p>
 * Author: 19461
 * Date: 2024/2/17
 */
@Data
@Slf4j
public class DetectModel {

    private static String head="imgurl";


    static final private Long interval=5000L;

    static private Long lastTime=0L;

    static private Boolean testRes=false;

    private static int id;

    private static int port;

    private static String ip;

    public DetectModel(){

    }


    public void init(){
        id= ConfigProperties.properties.getModelConfig().getId();
        port=ConfigProperties.properties.getModelConfig().getPort();
        ip=ConfigProperties.properties.getModelConfig().getIp();
    }



    public static boolean testHttpConnection() {
        String targetUrl = "http://" + ip + ":" + port + "/test";
        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();

            // 设置连接超时和读取超时
            connection.setConnectTimeout(1000); // 1秒连接超时
            connection.setReadTimeout(1000);    // 1秒读取超时

            connection.setRequestMethod("GET");

            // 设置请求头的编码为UTF-8
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return Objects.equals(response.toString(), "200");
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public DetectResDto detectOne(String imgBase64){

        String targetUrl="http://"+ip+":"+port+"/detect";
        log.info("开始调用Python预测服务: {}", targetUrl);
        //发送给处理端
        Map parames=new HashMap<String,String>();

        parames.put(head,imgBase64);
        log.info("图片Base64长度: {}", imgBase64 != null ? imgBase64.length() : 0);

        String resJson="";
        DetectResDto res=new DetectResDto();

        if(testRes &&lastTime+interval<System.currentTimeMillis()){
            testRes=testHttpConnection();
            lastTime=System.currentTimeMillis();
        }

        //模型预测
        if(true){
            try {
                resJson= HttpUtil.post(targetUrl,parames);
                log.info("收到Python服务响应: {}", resJson != null ? resJson.substring(0, Math.min(200, resJson.length())) : "null");
                //读取结果
                JSONObject jsonObject=JSONObject.parseObject(resJson);

                res.setImgBase64(jsonObject.getString("imgBase64"));

                String defectionsStr=jsonObject.getString("defections");
                List<Defection> defections= JSON.parseArray(defectionsStr,Defection.class);
                res.setDefections(defections);
                log.info("检测完成，发现缺陷数量: {}", defections != null ? defections.size() : 0);
            } catch (Exception e) {
                log.error("调用Python预测服务失败: {}", e.getMessage(), e);
                throw e;  // 重新抛出异常，由上层捕获
            }
        }else{
            // 测试模式已禁用，不应该执行到这里
            log.warn("检测模式异常，返回空结果");
            res.setImgBase64(imgBase64);
            res.setDefections(new ArrayList<>());
        }
        res.setDefectionsSum(res.getDefections().size());

        return res;
    }
}