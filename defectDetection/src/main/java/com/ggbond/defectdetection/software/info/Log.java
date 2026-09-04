package com.ggbond.defectdetection.software.info;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ggbond.defectdetection.common.Result;
import com.ggbond.defectdetection.pojo.*;
import com.ggbond.defectdetection.service.ApiService;
import com.ggbond.defectdetection.service.SysLogService;
import com.ggbond.defectdetection.software.common.CommonResource;
import com.ggbond.defectdetection.software.common.ConfigProperties;
import com.ggbond.defectdetection.software.common.SysStatus;
import com.ggbond.defectdetection.software.face.MainInterface;
import com.ggbond.defectdetection.util.SseUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于日志的处理,包括监听操作,记录变化,输出日志信息
 * <p>
 * Author: 19461
 * Date: 2024/2/16
 */
@Data
@Aspect
@Component
@Slf4j
public class Log {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ApiService apiService;

    private static SysLogService logService;

    private static MainInterface mainInterface;

    private static SseUtil sseUtil;

    @Getter
    private static Map<Class,String> classToName=new HashMap<>();

    @Getter
    private static Map<Integer,String> typeToName=new HashMap<>();



    public Log(){

    }

    //定义切点
    @Pointcut("@annotation(LogPoint)")
    public void logPointAnnotation(){}

    @Pointcut("execution(* com.ggbond.defectdetection.software.face.common.OpPanel.*Button(..))")
    public void logPointMethod(){}

    @Around("logPointAnnotation()||logPointMethod()")
    public Object AfterRunLog(ProceedingJoinPoint joinPoint) throws Exception {

            //获取主体的类
            MethodSignature signature=(MethodSignature) joinPoint.getSignature();
            Method method=signature.getMethod();
            LogPoint logPoint=method.getAnnotation(LogPoint.class);


        Class role=logPoint.mainRole();
            Class target=logPoint.target();
            Object[] args=joinPoint.getArgs();

            Object res = null;
            try {
               res =joinPoint.proceed();
            } catch (Throwable e) {
                res=Result.fail("未知错误");
                e.printStackTrace();
            }

            Result r= (Result) res;

            SysLog sysLog=new SysLog();
            OpEnum op=logPoint.value();

            sysLog.setOperation(op.getName());
            sysLog.setOpTime(LocalDateTime.now());

            int operatorType = 0;
            int id=-1;

            if(role== Manager.class){  //管理员
                if (args.length > 0 && args[0] instanceof HttpSession) {
                    HttpSession session= (HttpSession) args[0];
                    Object userObj = session.getAttribute("user");
                    if (userObj instanceof Integer) {
                        id = (Integer) userObj;
                    }
                }
            }else if(role== Operator.class){
                id= CommonResource.getOperatorId();
                operatorType=1;
            }else if(role==Api.class){
                id=-1;
                Parameter[] parameters = method.getParameters();
                for(int i=0;i<parameters.length;i++){
                    Parameter param=parameters[i];
                    if(param.getType()==Api.class){
                        Api api= (Api) joinPoint.getArgs()[i];
                        LambdaQueryWrapper<Api> lqw=new LambdaQueryWrapper<>();
                        lqw.eq(Api::getApiKey,api.getApiKey());
                        api=apiService.getOne(lqw);
                        if (api != null) {
                            id=api.getId();
                        }
                        break;
                    }
                }
                operatorType=2;
            }else{
                throw new Exception("日志记录对象错误");
            }

            sysLog.setOperator(id);
            sysLog.setOperatorType(operatorType);
            sysLog.setTarget(classToName.get(target));

            // 1. 真实客户端IP提取
            String clientIp = "127.0.0.1";
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    clientIp = getClientIp(attrs.getRequest());
                }
            } catch (Exception ex) {
                log.warn("解析客户端IP异常: {}", ex.getMessage());
            }
            sysLog.setClientIp(clientIp);

            // 2. 真实资源模块分类
            String resType = classToName.get(target);
            if (resType == null) {
                resType = classToName.get(role);
            }
            sysLog.setResourceType(resType != null ? resType : "常规业务");

            // 3. 真实执行状态判定
            boolean isSuccess = r != null && r.getCode() == Result.SUCCESS_CODE;
            sysLog.setStatus(isSuccess ? "成功" : "失败");

            // 4. 真实操作详情生成（记录实际参数、变更内容）
            StringBuilder detailsBuilder = new StringBuilder();
            if (target == Manager.class && op == OpEnum.Login) {
                for (Object arg : args) {
                    if (arg instanceof Manager) {
                        detailsBuilder.append("用户登录: 账号[").append(((Manager) arg).getAccount()).append("] ");
                        detailsBuilder.append(isSuccess ? "验证成功" : "密码错误或用户不存在");
                    }
                }
            } else if (target == Api.class) {
                if (op == OpEnum.Add) {
                    for (Object arg : args) {
                        if (arg instanceof Map) {
                            Map map = (Map) arg;
                            detailsBuilder.append("批量生成密钥: 数量=").append(map.get("num"))
                                    .append(", 有效期=").append(map.get("validityPeriod")).append("天")
                                    .append(", 权限等级=").append(map.get("permissionLevel"));
                        }
                    }
                } else if (op == OpEnum.Update) {
                    for (Object arg : args) {
                        if (arg instanceof Api) {
                            Api a = (Api) arg;
                            detailsBuilder.append("更新API密钥: ID=").append(a.getId())
                                    .append(", 状态=").append(a.getStatus() != null && a.getStatus() == 1 ? "启用" : "禁用")
                                    .append(", 权限等级=").append(a.getPermissionLevel());
                        }
                    }
                } else if (op == OpEnum.Delete) {
                    for (Object arg : args) {
                        if (arg instanceof List) {
                            detailsBuilder.append("批量删除API密钥: ID列表=").append(arg);
                        }
                    }
                }
            } else if (target == Operator.class) {
                if (op == OpEnum.Add) {
                    for (Object arg : args) {
                        if (arg instanceof Operator) {
                            Operator opObj = (Operator) arg;
                            detailsBuilder.append("添加操作员: 姓名=").append(opObj.getName() != null ? opObj.getName() : "-")
                                    .append(", 工号=").append(opObj.getJobId() != null ? opObj.getJobId() : "-");
                        }
                    }
                } else if (op == OpEnum.Update) {
                    for (Object arg : args) {
                        if (arg instanceof Operator) {
                            Operator opObj = (Operator) arg;
                            detailsBuilder.append("更新操作员: 工号=").append(opObj.getJobId() != null ? opObj.getJobId() : "-")
                                    .append(", 姓名=").append(opObj.getName() != null ? opObj.getName() : "-");
                        }
                    }
                } else if (op == OpEnum.Delete) {
                    for (Object arg : args) {
                        if (arg instanceof List) {
                            detailsBuilder.append("删除操作员: ID列表=").append(arg);
                        }
                    }
                }
            }

            if (detailsBuilder.length() == 0) {
                if (r != null && r.getMessage() != null) {
                    detailsBuilder.append(r.getMessage());
                } else {
                    detailsBuilder.append(op.getName()).append(classToName.get(target) != null ? classToName.get(target) : "");
                }
            }
            sysLog.setDetails(detailsBuilder.toString());

            handlerLog(sysLog);

            log.info("📝 实时系统审计日志: {}", sysLog);

            return res;
    }

    //加载日志
    @PostConstruct
    public void init(){

        //加载beans
        logService=applicationContext.getBean(SysLogService.class);
        // GUI组件可能不存在，需要检查
        try {
            mainInterface = applicationContext.getBean(MainInterface.class);
        } catch (Exception e) {
            log.info("服务器模式: GUI组件不可用");
            mainInterface = null;
        }
        sseUtil=applicationContext.getBean(SseUtil.class);

        //加载class与名称映射表
        classToName.put(Api.class,"API密钥");
        classToName.put(Device.class,"产线设备");
        classToName.put(Operator.class,"操作人员");
        classToName.put(WorkOrder.class,"工单排产");
        classToName.put(SysStatus.class,"系统状态");
        classToName.put(ConfigProperties.class,"系统配置");
        classToName.put(Warnings.class,"告警配置");
        classToName.put(DetectLog.class,"质检检测");
        classToName.put(Manager.class,"系统用户");

        typeToName.put(0,"管理员");
        typeToName.put(1,"操作人员");
        typeToName.put(2,"第三方api");

    }
    //监听器,监听操作并自动记录
    public static void writeLog(SysLog sysLog){
        logService.save(sysLog);
    }
    //输出日志信息
    public static void outputLog(SysLog sysLog){
        // 只在GUI可用时更新界面
        if(mainInterface != null) {
            mainInterface.getOperationAndOutputInterface().updateOutput(sysLog, null);
        }
        sseUtil.sendMessageToAll(String.valueOf(Result.LOG_CODE),sysLog);
    }

    private static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            try {
                // 动态获取本机真实网卡局域网IP
                java.net.InetAddress local = java.net.InetAddress.getLocalHost();
                if (local != null && local.getHostAddress() != null && !local.getHostAddress().startsWith("127.")) {
                    return local.getHostAddress();
                }
            } catch (Exception ignored) {}
        }
        return ip != null ? ip : "127.0.0.1";
    }

    public static void handlerLog(SysLog sysLog){
        if(sysLog.getMainRole()==null){
            sysLog.setMainRole(typeToName.get(sysLog.getOperatorType()).concat(sysLog.getOperator()+""));
        }
        writeLog(sysLog);
        outputLog(sysLog);
    }

    public static void outputMessage(String message){

    }
}