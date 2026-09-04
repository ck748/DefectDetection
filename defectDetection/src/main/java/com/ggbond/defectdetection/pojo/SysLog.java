package com.ggbond.defectdetection.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Author: 19461
 * Date: 2024/2/15
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SysLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("time")
    @JsonProperty("time")
    private LocalDateTime opTime;

    @JsonProperty("op")
    private String operation;


    @JsonProperty("label")
    private Integer operator;

    private Integer operatorType;  //1 操作员 0 管理人员 2 api

    private String target;

    @TableField("resource_type")
    @JsonProperty("resourceType")
    private String resourceType;

    @TableField("client_ip")
    @JsonProperty("clientIp")
    private String clientIp;

    @TableField("status")
    @JsonProperty("status")
    private String status;

    @TableField("details")
    @JsonProperty("details")
    private String details;

    @TableField(exist = false)
    private String mainRole;

    @TableField(exist = false)
    @JsonProperty("operatorName")
    private String operatorName;

    @TableField(exist = false)
    private Integer totals=0;

    public SysLog(Integer id, LocalDateTime opTime, String operation, Integer operator, Integer operatorType, String target, String mainRole, Integer totals) {
        this.id = id;
        this.opTime = opTime;
        this.operation = operation;
        this.operator = operator;
        this.operatorType = operatorType;
        this.target = target;
        this.mainRole = mainRole;
        this.totals = totals;
    }

    public void setOperatorType(Integer operatorType){
        if(operatorType==1){
            mainRole="操作员";
        }else if(operatorType==0){
            mainRole="管理人员";
        }else if(operatorType==2){
            mainRole="第三方api";
        }
        this.operatorType=operatorType;
    }
}