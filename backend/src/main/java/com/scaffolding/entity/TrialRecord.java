package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trial_record")
public class TrialRecord extends BaseEntity {

    private Long assignmentId;

    private Long candidateId;

    private String candidateName;

    private Long teamId;

    private String teamName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate trialDate;

    private Integer isArrived;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime arriveTime;

    private Integer safetyTraining;

    private String trainingContent;

    private Integer performanceScore;

    private String performanceRemark;

    private String canConvert;

    private String convertRemark;

    private Long recordUserId;

    private String recordUserName;
}
