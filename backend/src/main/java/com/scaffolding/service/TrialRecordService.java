package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.TrialRecord;

public interface TrialRecordService extends IService<TrialRecord> {

    Page<TrialRecord> pageQuery(Long current, Long size, Long candidateId, Long teamId, String canConvert, String startDate, String endDate);

    TrialRecord recordTrial(TrialRecord record);

    TrialRecord getByAssignmentId(Long assignmentId);
}
