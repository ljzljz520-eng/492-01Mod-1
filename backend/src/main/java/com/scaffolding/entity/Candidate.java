package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("candidate")
public class Candidate extends BaseEntity {

    private String candidateName;

    private String gender;

    private Integer age;

    private String phone;

    private String idCard;

    private String position;

    private String interviewStatus;

    private Integer interviewScore;

    private String interviewRemark;

    private Integer isInRisk;

    private Integer absentCount;

    private String status;
}
