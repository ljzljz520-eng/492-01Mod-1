package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Candidate;
import com.scaffolding.mapper.CandidateMapper;
import com.scaffolding.service.CandidateService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class CandidateServiceImpl extends ServiceImpl<CandidateMapper, Candidate> implements CandidateService {

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
}
