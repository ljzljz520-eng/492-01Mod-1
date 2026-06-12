package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Candidate;
import com.scaffolding.entity.RiskList;
import com.scaffolding.mapper.RiskListMapper;
import com.scaffolding.service.CandidateService;
import com.scaffolding.service.RiskListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class RiskListServiceImpl extends ServiceImpl<RiskListMapper, RiskList> implements RiskListService {

    @Autowired
    private CandidateService candidateService;

    @Override
    public Page<RiskList> pageQuery(Long current, Long size, String candidateName, String status) {
        Page<RiskList> page = new Page<>(current, size);
        LambdaQueryWrapper<RiskList> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(candidateName)) {
            wrapper.like(RiskList::getCandidateName, candidateName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(RiskList::getStatus, status);
        }

        wrapper.orderByDesc(RiskList::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskList addToRiskList(Long candidateId, String reason, Long operatorId, String operatorName) {
        if (isInRiskList(candidateId)) {
            throw new RuntimeException("候选人已在风险名单中");
        }

        Candidate candidate = candidateService.getById(candidateId);
        if (candidate == null) {
            throw new RuntimeException("候选人不存在");
        }

        RiskList riskList = new RiskList();
        riskList.setCandidateId(candidateId);
        riskList.setCandidateName(candidate.getCandidateName());
        riskList.setPhone(candidate.getPhone());
        riskList.setAbsentCount(candidate.getAbsentCount());
        riskList.setReason(reason);
        riskList.setOperatorId(operatorId);
        riskList.setOperatorName(operatorName);
        riskList.setStatus("active");
        riskList.setCreateTime(LocalDateTime.now());
        riskList.setUpdateTime(LocalDateTime.now());
        this.save(riskList);

        candidate.setIsInRisk(1);
        candidate.setUpdateTime(LocalDateTime.now());
        candidateService.updateById(candidate);

        return riskList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromRiskList(Long id, String removeReason, Long operatorId) {
        RiskList riskList = this.getById(id);
        if (riskList == null) {
            throw new RuntimeException("风险记录不存在");
        }
        if ("removed".equals(riskList.getStatus())) {
            throw new RuntimeException("该记录已移除");
        }

        riskList.setStatus("removed");
        riskList.setRemoveReason(removeReason);
        riskList.setRemoveTime(LocalDateTime.now());
        riskList.setUpdateTime(LocalDateTime.now());
        this.updateById(riskList);

        Candidate candidate = candidateService.getById(riskList.getCandidateId());
        if (candidate != null) {
            candidate.setIsInRisk(0);
            candidate.setUpdateTime(LocalDateTime.now());
            candidateService.updateById(candidate);
        }
    }

    @Override
    public boolean isInRiskList(Long candidateId) {
        LambdaQueryWrapper<RiskList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskList::getCandidateId, candidateId);
        wrapper.eq(RiskList::getStatus, "active");
        return this.count(wrapper) > 0;
    }
}
