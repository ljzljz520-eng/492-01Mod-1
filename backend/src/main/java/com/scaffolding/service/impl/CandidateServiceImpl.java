package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Candidate;
import com.scaffolding.entity.TrialAssignment;
import com.scaffolding.mapper.CandidateMapper;
import com.scaffolding.service.CandidateService;
import com.scaffolding.service.RiskListService;
import com.scaffolding.service.TrialAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateServiceImpl extends ServiceImpl<CandidateMapper, Candidate> implements CandidateService {

    @Autowired
    private RiskListService riskListService;

    @Autowired
    private TrialAssignmentService trialAssignmentService;

    @Override
    public Page<Candidate> pageQuery(Long current, Long size, String candidateName, String interviewStatus, String position, Integer isInRisk) {
        Page<Candidate> page = new Page<>(current, size);
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(candidateName)) {
            wrapper.like(Candidate::getCandidateName, candidateName);
        }
        if (StringUtils.hasText(interviewStatus)) {
            wrapper.eq(Candidate::getInterviewStatus, interviewStatus);
        }
        if (StringUtils.hasText(position)) {
            wrapper.like(Candidate::getPosition, position);
        }
        if (isInRisk != null) {
            wrapper.eq(Candidate::getIsInRisk, isInRisk);
        }

        wrapper.orderByDesc(Candidate::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void passInterview(Long id, Integer score, String remark) {
        Candidate candidate = this.getById(id);
        if (candidate == null) {
            throw new RuntimeException("候选人不存在");
        }
        candidate.setInterviewStatus("passed");
        candidate.setInterviewScore(score);
        candidate.setInterviewRemark(remark);
        candidate.setUpdateTime(LocalDateTime.now());
        this.updateById(candidate);
    }

    @Override
    public void failInterview(Long id, String remark) {
        Candidate candidate = this.getById(id);
        if (candidate == null) {
            throw new RuntimeException("候选人不存在");
        }
        candidate.setInterviewStatus("failed");
        candidate.setInterviewRemark(remark);
        candidate.setStatus("rejected");
        candidate.setUpdateTime(LocalDateTime.now());
        this.updateById(candidate);
    }

    @Override
    public List<Candidate> listAvailableForTrial() {
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Candidate::getInterviewStatus, "passed");
        wrapper.eq(Candidate::getIsInRisk, 0);
        wrapper.ne(Candidate::getStatus, "hired");
        wrapper.orderByDesc(Candidate::getCreateTime);
        List<Candidate> candidates = this.list(wrapper);

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> candidateIds = candidates.stream().map(Candidate::getId).collect(Collectors.toList());
        LambdaQueryWrapper<TrialAssignment> assignmentWrapper = new LambdaQueryWrapper<>();
        assignmentWrapper.in(TrialAssignment::getCandidateId, candidateIds);
        assignmentWrapper.in(TrialAssignment::getStatus, "pending", "in_progress");
        List<TrialAssignment> activeAssignments = trialAssignmentService.list(assignmentWrapper);
        List<Long> busyCandidateIds = activeAssignments.stream()
                .map(TrialAssignment::getCandidateId)
                .distinct()
                .collect(Collectors.toList());

        return candidates.stream()
                .filter(c -> !busyCandidateIds.contains(c.getId()))
                .collect(Collectors.toList());
    }
}
