package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.TrialAssignment;

public interface TrialAssignmentService extends IService<TrialAssignment> {

    Page<TrialAssignment> pageQuery(Long current, Long size, Long candidateId, Long teamId, String status, String startDate, String endDate);

    TrialAssignment arrangeTrial(TrialAssignment assignment);

    void cancelTrial(Long id, String reason);

    void markAbsent(Long id);
}
