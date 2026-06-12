package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Candidate;
import com.scaffolding.entity.Team;
import com.scaffolding.entity.TrialAssignment;
import com.scaffolding.mapper.TrialAssignmentMapper;
import com.scaffolding.service.CandidateService;
import com.scaffolding.service.RiskListService;
import com.scaffolding.service.TeamService;
import com.scaffolding.service.TrialAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TrialAssignmentServiceImpl extends ServiceImpl<TrialAssignmentMapper, TrialAssignment> implements TrialAssignmentService {

    @Autowired
    private TeamService teamService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private RiskListService riskListService;

    @Override
    public Page<TrialAssignment> pageQuery(Long current, Long size, Long candidateId, Long teamId, String status, String startDate, String endDate) {
        Page<TrialAssignment> page = new Page<>(current, size);
        LambdaQueryWrapper<TrialAssignment> wrapper = new LambdaQueryWrapper<>();

        if (candidateId != null) {
            wrapper.eq(TrialAssignment::getCandidateId, candidateId);
        }
        if (teamId != null) {
            wrapper.eq(TrialAssignment::getTeamId, teamId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(TrialAssignment::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(TrialAssignment::getTrialDate, LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(TrialAssignment::getTrialDate, LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        wrapper.orderByDesc(TrialAssignment::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrialAssignment arrangeTrial(TrialAssignment assignment) {
        Candidate candidate = candidateService.getById(assignment.getCandidateId());
        if (candidate == null) {
            throw new RuntimeException("候选人不存在");
        }
        if (!"passed".equals(candidate.getInterviewStatus())) {
            throw new RuntimeException("候选人未通过面试，不能安排试岗");
        }
        if (riskListService.isInRiskList(assignment.getCandidateId())) {
            throw new RuntimeException("候选人在风险名单中，不能安排试岗");
        }

        Team team = teamService.getById(assignment.getTeamId());
        if (team == null) {
            throw new RuntimeException("班组不存在");
        }
        if (team.getUsedQuota() >= team.getQuota()) {
            throw new RuntimeException("班组名额已满");
        }

        assignment.setCandidateName(candidate.getCandidateName());
        assignment.setTeamName(team.getTeamName());
        assignment.setStatus("pending");
        assignment.setCreateTime(LocalDateTime.now());
        assignment.setUpdateTime(LocalDateTime.now());
        this.save(assignment);

        team.setUsedQuota(team.getUsedQuota() + 1);
        team.setUpdateTime(LocalDateTime.now());
        teamService.updateById(team);

        return assignment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTrial(Long id, String reason) {
        TrialAssignment assignment = this.getById(id);
        if (assignment == null) {
            throw new RuntimeException("试岗安排不存在");
        }
        if ("completed".equals(assignment.getStatus()) || "cancelled".equals(assignment.getStatus()) || "absent".equals(assignment.getStatus())) {
            throw new RuntimeException("试岗安排已完成或已取消，不能重复操作");
        }

        assignment.setStatus("cancelled");
        assignment.setRemark(reason);
        assignment.setUpdateTime(LocalDateTime.now());
        this.updateById(assignment);

        Team team = teamService.getById(assignment.getTeamId());
        if (team != null && team.getUsedQuota() > 0) {
            team.setUsedQuota(team.getUsedQuota() - 1);
            team.setUpdateTime(LocalDateTime.now());
            teamService.updateById(team);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAbsent(Long id) {
        TrialAssignment assignment = this.getById(id);
        if (assignment == null) {
            throw new RuntimeException("试岗安排不存在");
        }
        if (!"pending".equals(assignment.getStatus()) && !"in_progress".equals(assignment.getStatus())) {
            throw new RuntimeException("试岗安排状态不允许标记未到岗");
        }

        assignment.setStatus("absent");
        assignment.setUpdateTime(LocalDateTime.now());
        this.updateById(assignment);

        Candidate candidate = candidateService.getById(assignment.getCandidateId());
        if (candidate != null) {
            int newAbsentCount = candidate.getAbsentCount() == null ? 1 : candidate.getAbsentCount() + 1;
            candidate.setAbsentCount(newAbsentCount);
            candidate.setUpdateTime(LocalDateTime.now());
            candidateService.updateById(candidate);

            if (newAbsentCount >= 3) {
                riskListService.addToRiskList(candidate.getId(), "累计" + newAbsentCount + "次未到岗", 1L, "系统自动");
                candidate.setIsInRisk(1);
                candidate.setUpdateTime(LocalDateTime.now());
                candidateService.updateById(candidate);
            }
        }

        Team team = teamService.getById(assignment.getTeamId());
        if (team != null && team.getUsedQuota() > 0) {
            team.setUsedQuota(team.getUsedQuota() - 1);
            team.setUpdateTime(LocalDateTime.now());
            teamService.updateById(team);
        }
    }
}
