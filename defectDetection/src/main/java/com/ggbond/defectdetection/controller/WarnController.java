package com.ggbond.defectdetection.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.dto.ChartsDto;
import com.ggbond.defectdetection.dto.WarningsDto;
import com.ggbond.defectdetection.pojo.Manager;
import com.ggbond.defectdetection.pojo.Warnings;
import com.ggbond.defectdetection.service.ManagerService;
import com.ggbond.defectdetection.service.WarningsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: 19461
 * Date: 2024/3/3
 */
@RestController
@RequestMapping("/detectInfo")

public class WarnController {

    @Autowired
    ManagerService managerService;

    @Autowired
    WarningsService warningsService;
    @GetMapping("/warnings/load")
    public Result loadInfoHandler(HttpSession session){

        Object userObj = session.getAttribute("user");
        Integer managerId = null;
        if (userObj instanceof Integer) {
            managerId = (Integer) userObj;
        } else if (userObj instanceof Long) {
            managerId = Math.toIntExact((Long) userObj);
        }

        Manager manager = null;
        if (managerId != null) {
            manager = managerService.getById(managerId);
        }
        if (manager == null) {
            List<Manager> list = managerService.list();
            manager = list.isEmpty() ? new Manager() : list.get(0);
        }

        WarningsDto warningsDto=new WarningsDto();
        warningsDto.generateFromManager(manager);

        warningsDto.setWarningsSum(Math.toIntExact(warningsService.count()));

        List<Warnings> warningsList=warningsService.list();
        warningsDto.setWarningsList(warningsList);

        LambdaQueryWrapper<Warnings> lqw=new LambdaQueryWrapper<>();

        lqw.between(Warnings::getCreateTime, LocalDateTime.now().minusDays(1),LocalDateTime.now());

        warningsDto.setOneDayWarningsSum(Math.toIntExact(warningsService.count(lqw)));

        Map<Integer,Integer> warnMap=new HashMap<>();

        Integer[] type={1,2,3};

        for (Integer integer : type) {
            lqw.clear();
            lqw.eq(Warnings::getLevel,integer);
            warnMap.put(integer, Math.toIntExact(warningsService.count(lqw)));
        }
        ChartsDto<Map> chartsDto=new ChartsDto<>("告警各级别数量图","柱状图",0,warnMap);

        warningsDto.setChartsDto(chartsDto);

        return Result.success("加载成功",warningsDto);
    }

    @GetMapping("/warnings/info")
    public Result getWarnsInfo(Integer page,Integer pageSize){

        if(page==null||pageSize==null){
            List<Warnings> warningsList=warningsService.list();
            return Result.success("获取警告列表成功", warningsList);
        }else{
            IPage<Warnings> pg=new Page<>(page,pageSize);
            List<Warnings> warningsList=warningsService.list(pg);
            return  Result.success("获取警告列表成功", warningsList);
        }
    }

    @PutMapping("/warnings/set")
    public Result setWarningsHandler(HttpSession session,@RequestBody Manager manager){

        Object userObj = session.getAttribute("user");
        Integer managerId = null;
        if (userObj instanceof Integer) {
            managerId = (Integer) userObj;
        } else if (userObj instanceof Long) {
            managerId = Math.toIntExact((Long) userObj);
        }

        if (managerId != null) {
            manager.setId(managerId);
        } else if (manager.getId() == null) {
            List<Manager> list = managerService.list();
            if (!list.isEmpty()) {
                manager.setId(list.get(0).getId());
            }
        }

        if(manager.getId() != null && managerService.updateById(manager)){
            return Result.success("设置成功");
        }else{
            return Result.fail("设置失败");
        }
    }
}