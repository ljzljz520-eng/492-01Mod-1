package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Candidate;
import com.scaffolding.entity.TrialAssignment;
import com.scaffolding.entity.TrialRecord;
import com.scaffolding.mapper.TrialRecordMapper;
import com.scaffolding.service.CandidateService;
import com.scaffolding.service.TrialAssignmentService;
import com.scaffolding.service.TrialRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TrialRecordServiceImpl extends ServiceImpl<TrialRecordMapper, TrialRecord> implements TrialRecordService {

    @Autowired
    private TrialAssignmentService trialAssignmentService;

    @Autowired
    private CandidateService candidateService;

    @Override
    public Page<TrialRecord> pageQuery(Long current, Long size, Long candidateId, Long teamId, String canConvert, String startDate, String endDate) {
        Page<TrialRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<TrialRecord> wrapper = new LambdaQueryWrapper<>();

        if (candidateId != null) {
            wrapper.eq(TrialRecord::getCandidateId, candidateId);
        }
        if (teamId != null) {
            wrapper.eq(TrialRecord::getTeamId, teamId);
        }
        if (StringUtils.hasText(canConvert)) {
            wrapper.eq(TrialRecord::getCanConvert, canConvert);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(TrialRecord::getTrialDate, LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(TrialRecord::getTrialDate, LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        wrapper.orderByDesc(TrialRecord::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrialRecord recordTrial(TrialRecord record) {
        TrialAssignment assignment = trialAssignmentService.getById(record.getAssignmentId());
        if (assignment == null) {
            throw new RuntimeException("试岗安排不存在");
        }

        record.setCandidateId(assignment.getCandidateId());
        record.setCandidateName(assignment.getCandidateName());
        record.setTeamId(assignment.getTeamId());
        record.setTeamName(assignment.getTeamName());
        record.setTrialDate(assignment.getTrialDate());

        if (record.getIsArrived() != null && record.getIsArrived() == 1) {
            if (record.getArriveTime() == null) {
                record.setArriveTime(LocalDateTime.now());
            }
        }

        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        this.save(record);

        if ("yes".equals(record.getCanConvert())) {
            assignment.setStatus("completed");
            Candidate candidate = candidateService.getById(assignment.getCandidateId());
            if (candidate != null) {
                candidate.setStatus("hired");
                candidate.setUpdateTime(LocalDateTime.now());
                candidateService.updateById(candidate);
            }
        } else if ("no".equals(record.getCanConvert())) {
            assignment.setStatus("completed");
        } else {
            assignment.setStatus("in_progress");
        }
        assignment.setUpdateTime(LocalDateTime.now());
        trialAssignmentService.updateById(assignment);

        return record;
    }

    @Override
    public TrialRecord getByAssignmentId(Long assignmentId) {
        LambdaQueryWrapper<TrialRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TrialRecord::getAssignmentId, assignmentId);
        wrapper.orderByDesc(TrialRecord::getCreateTime);
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }
}
