package com.ggbond.defectdetection.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.Api;
import com.ggbond.defectdetection.pojo.Manager;
import com.ggbond.defectdetection.pojo.Operator;
import com.ggbond.defectdetection.service.ManagerService;
import com.ggbond.defectdetection.service.OperatorService;
import com.ggbond.defectdetection.software.info.LogPoint;
import com.ggbond.defectdetection.software.info.OpEnum;
import com.ggbond.defectdetection.util.EncryptionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Author: 19461
 * Date: 2024/2/24
 */
@Slf4j
@RestController
@RequestMapping("/sysManage")
public class OperatorController {

    @Autowired
    OperatorService operatorService;

    @Autowired
    ManagerService managerService;

    @GetMapping("/key/info")
    public Result getOperatorInfoHandler(Integer page, Integer pageSize) {
        if (page == null) page = 1;
        if (pageSize == null) pageSize = 10;

        IPage<Manager> managerIPage = new Page<>(page, pageSize);
        managerService.page(managerIPage);
        List<Manager> managerList = managerIPage.getRecords();
        int totalPages = Math.toIntExact(managerService.count());

        List<Operator> operatorList = new java.util.ArrayList<>();
        if (managerList != null) {
            for (int i = 0; i < managerList.size(); i++) {
                Manager manager = managerList.get(i);
                Operator operator = new Operator();

                // 序号保持 manager 的 id
                operator.setId(manager.getId());
                // 姓名换成 manager 的 account 字段
                operator.setName(manager.getAccount());
                // 工号变成 100 + 序号数字 (例如 id=1 则为 1001)
                operator.setJobId(1000 + (manager.getId() != null ? manager.getId() : (i + 1)));
                // 密码就是 manager 表中的密码
                operator.setLoginPwd(manager.getPwd());
                operator.setOpPwd(manager.getPwd());

                // 时间与备注保持默认/基本信息
                operator.setCreateTime(LocalDateTime.now());
                operator.setRemark("管理员账户转换");
                operator.totals = totalPages;

                operatorList.add(operator);
            }
            return Result.success("获取成功", operatorList);
        } else {
            return Result.fail("获取失败,请稍后再试");
        }
    }

    @PostMapping("/key/add")
    @LogPoint(value = OpEnum.Add, mainRole = Manager.class, target = Manager.class)
    public Result addOperatorHandler(HttpSession httpSession,
                                     @RequestBody Operator operator
                                     ){
        if (operator.getName() == null || operator.getName().trim().isEmpty()) {
            return Result.fail("姓名(账号)不能为空");
        }

        // 检查 manager 表中 account 是否重复
        LambdaQueryWrapper<Manager> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Manager::getAccount, operator.getName().trim());
        if (managerService.count(lqw) > 0) {
            return Result.fail("账号已存在，请更换姓名/账号");
        }

        Manager manager = new Manager();
        // 姓名作为 account
        manager.setAccount(operator.getName().trim());
        manager.setName(operator.getName().trim());

        // 密码保存（优先取 loginPwd 或 opPwd）
        String rawPwd = operator.getLoginPwd() != null ? operator.getLoginPwd() : operator.getOpPwd();
        if (rawPwd != null && !rawPwd.isEmpty()) {
            // manager 默认 md5 存储，若已是 md5 或明文均妥善处理
            if (rawPwd.length() == 32) {
                manager.setPwd(rawPwd);
            } else {
                manager.setPwd(DigestUtils.md5DigestAsHex(rawPwd.getBytes()));
            }
        } else {
            // 默认密码 123456
            manager.setPwd(DigestUtils.md5DigestAsHex("123456".getBytes()));
        }

        manager.setPhone("13800138000");
        manager.setEmail(operator.getName() + "@example.com");
        manager.setOnline(false);
        manager.setWarningsOpen(false);

        boolean saved = managerService.save(manager);
        if (saved) {
            operator.setId(manager.getId());
            operator.setJobId(1000 + manager.getId());
            return Result.success("添加成功", operator);
        } else {
            return Result.fail("添加失败，请稍后再试");
        }
    }

    @PutMapping("/key/batchPwd")
    @LogPoint(value = OpEnum.Update, mainRole = Manager.class,target = Operator.class)
    public Result setBatchPwdHandler(HttpSession session,
                                     @RequestParam(required = false,defaultValue = "1") Integer page,Integer pageSize,
                                     @RequestParam(required = false) String loginPwd){

        if(loginPwd!=null){
            loginPwd=EncryptionUtil.encrypt(loginPwd);
        }

        IPage<Operator> operatorIPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Operator> lqw=new LambdaQueryWrapper<>();

        Operator operator=new Operator();
        operator.setLoginPwd(EncryptionUtil.encrypt(loginPwd));
        boolean res = operatorService.update(operator, lqw);

        if(!res){
            List<Operator> operatorList=operatorService.list(operatorIPage);
            return Result.success("修改成功",operatorList);
        }else{
            return Result.fail("修改失败");
        }
    }

    @PutMapping("/key/update")
    @LogPoint(value = OpEnum.Update, mainRole = Manager.class,target = Operator.class)
    public Result updateOperatorInfoHandler(HttpSession httpSession,
                                            @RequestBody Operator operator
                                            ){

        log.info(operator.toString());
        LambdaQueryWrapper<Operator> lqw=new LambdaQueryWrapper<>();
        lqw.exists("select login_pwd from operator where login_pwd is not null");

        boolean res=operatorService.exists(lqw);

        if(!res){ //存在非空密码
            if(operator.getLoginPwd()!=null){
                return Result.fail("修改失败,请检查登入密码是否为空");
            }
        }else{
            if(operator.getLoginPwd()==null){
                return Result.fail("修改失败,请检查登入密码是否为空");
            }
        }

        if(operator.getLoginPwd()!=null){
            operator.setLoginPwd(EncryptionUtil.encrypt(operator.getLoginPwd()));
        }
        if(operator.getOpPwd()!=null){
            operator.setOpPwd(EncryptionUtil.encrypt(operator.getOpPwd()));
        }

        res = operatorService.updateById(operator);
        operator=operatorService.getById(operator.getId());

        if(res){
            return Result.success("修改成功",operator);
        }else{
            return Result.fail("修改失败,请稍后再试");
        }
    }

    @DeleteMapping("/key/delete")
    @LogPoint(value = OpEnum.Delete, mainRole = Manager.class,target = Api.class)
    public Result deleteOperator(HttpSession session,@RequestBody List<Integer> ids){
        if(ids==null){
            return null;
        }
        boolean res = operatorService.removeBatchByIds(ids);
        if(res){
            return Result.success("删除成功");
        }else{
            return Result.fail("删除失败,请稍后再试");
        }
    }
}