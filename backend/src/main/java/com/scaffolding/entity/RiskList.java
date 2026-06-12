package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_list")
public class RiskList extends BaseEntity {

    private Long candidateId;

    private String candidateName;

    private String phone;

    private Integer absentCount;

    private String reason;

    private Long operatorId;

    private String operatorName;

    private String status;

    private String removeReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime removeTime;
}
