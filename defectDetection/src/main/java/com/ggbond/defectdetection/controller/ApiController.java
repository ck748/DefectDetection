package com.ggbond.defectdetection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.Api;
import com.ggbond.defectdetection.pojo.Manager;
import com.ggbond.defectdetection.service.ApiService;
import com.ggbond.defectdetection.software.info.LogPoint;
import com.ggbond.defectdetection.software.info.OpEnum;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Author: 19461
 * Date: 2024/2/23
 */
@RestController
@Slf4j
@ResponseBody
@RequestMapping("/sysManage")
public class ApiController {

    @Autowired
    ApiService apiService;

    @GetMapping("/api/info")
    public Result getApiInfoHandler(int pageSize,int page){
        try {
            IPage<Api> apiIPage = new Page<>(page, pageSize);
            // 修复：使用page方法而不是list方法
            apiService.page(apiIPage);
            List<Api> apiList = apiIPage.getRecords();
            int totalPages = Math.toIntExact(apiService.count());
            
            if(apiList != null && !apiList.isEmpty()) {
                apiList.forEach(api -> api.totals = totalPages);
                return Result.success("加载成功", apiList);
            } else {
                // 返回空列表而不是失败
                return Result.success("加载成功", new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("获取API信息失败: {}", e.getMessage(), e);
            return Result.fail("数据加载失败: " + e.getMessage());
        }
    }

    @LogPoint(value = OpEnum.Add, mainRole = Manager.class, target = Api.class)
    @PostMapping("/api/add")
    public Result addApiHandler(HttpSession session,
                                @RequestBody Map<String,Object> map){
        try {
            log.info("API添加请求: {}", map.toString());
            
            Integer num = Integer.parseInt(String.valueOf(map.get("num")));
            Integer validityPeriod = Integer.parseInt(Objects.equals(String.valueOf(map.get("validityPeriod")), "") ? "-1" : String.valueOf(map.get("validityPeriod")));
            Integer validityTimes = Integer.parseInt(Objects.equals(String.valueOf(map.get("validityTimes")), "") ? "-1" : String.valueOf(map.get("validityTimes")));
            Integer permissionLevel = Integer.parseInt(String.valueOf(map.get("permissionLevel")));
            String createName = String.valueOf(map.get("createName"));
            
            // 修复：添加status和remark字段的处理
            Integer status = map.get("status") != null ? Integer.parseInt(String.valueOf(map.get("status"))) : 1;
            String remark = map.get("remark") != null ? String.valueOf(map.get("remark")) : "";
            
            String[] apiKeys = ApiKeyGenerator.generateApiKeys(num);
            List<Api> apiList = new LinkedList<>();
            
            for(String apiKey : apiKeys){
                log.info("生成API Key: {}", apiKey);
                Api api = new Api(validityPeriod, validityTimes, permissionLevel, apiKey);
                api.setCreateName(createName);
                api.setStatus(status);  // 设置状态
                api.setRemark(remark);  // 设置备注
                apiList.add(api);
            }
            
            apiService.saveBatch(apiList);
            log.info("成功添加 {} 个API Key", num);
            
            return Result.success("添加成功", apiList);
        } catch (Exception e) {
            log.error("添加API失败: {}", e.getMessage(), e);
            return Result.fail("添加失败: " + e.getMessage());
        }
    }

    @PatchMapping("/api/update")
    @LogPoint(value = OpEnum.Update, mainRole = Manager.class,target = Api.class)
    public Result updateApi(HttpSession session,
                            @RequestBody Api api){


        if(api.getValidityTimes()==0){
            api.setValidityTimes(1000+api.getValidityTimes());
        }
        if(api.getValidityPeriod()==0){
            api.setValidityPeriod(api.getValidityPeriod()+365);
        }

        boolean res=apiService.updateById(api);

        api=apiService.getById(api.getId());

        if(res){
            return Result.success("修改成功",api);
        }else{
            return Result.fail("修改失败,请稍后再试");
        }
    }

    @DeleteMapping("/api/delete")
    @LogPoint(value = OpEnum.Delete, mainRole = Manager.class,target = Api.class)
    public Result deleteApis(HttpSession session,@RequestBody(required = false) ArrayList<Integer> ids){

        if(ids==null){
            return null;
        }
        boolean res=apiService.removeBatchByIds(ids);

        if(res){
            return Result.success("删除成功");
        }else{
            return Result.fail("删除失败,请稍后再试");
        }

    }

    public class ApiKeyGenerator {
        private static final String PREFIX = "ma-";
        private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static String[] generateApiKeys(int count) {
            String[] apiKeys = new String[count];
            Random random = new Random();

            for (int i = 0; i < count; i++) {
                StringBuilder apiKey = new StringBuilder(PREFIX);

                for (int j = 0; j < 10; j++) {
                    int index = random.nextInt(CHARACTERS.length());
                    apiKey.append(CHARACTERS.charAt(index));
                }

                apiKeys[i] = apiKey.toString();
            }

            return apiKeys;
        }

    }

}