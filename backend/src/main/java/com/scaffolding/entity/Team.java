package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team")
public class Team extends BaseEntity {

    private String teamName;

    private String teamLeader;

    private String teamPhone;

    private Integer quota;

    private Integer usedQuota;

    private String workShift;

    private String description;
}
