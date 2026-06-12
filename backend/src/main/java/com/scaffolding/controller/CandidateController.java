package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Candidate;
import com.scaffolding.service.CandidateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/candidate")
@Api(tags = "候选人管理")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @PostMapping
    @ApiOperation("新增候选人")
    public Result<Candidate> save(@RequestBody Candidate candidate) {
        try {
            candidate.setCreateTime(LocalDateTime.now());
            candidate.setUpdateTime(LocalDateTime.now());
            if (candidate.getInterviewStatus() == null) {
                candidate.setInterviewStatus("pending");
            }
            if (candidate.getStatus() == null) {
                candidate.setStatus("active");
            }
            if (candidate.getIsInRisk() == null) {
                candidate.setIsInRisk(0);
            }
            if (candidate.getAbsentCount() == null) {
                candidate.setAbsentCount(0);
            }
            candidateService.save(candidate);
            return Result.success("新增成功", candidate);
        } catch (Exception e) {
            log.error("新增候选人失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新候选人")
    public Result<Candidate> update(@PathVariable Long id, @RequestBody Candidate candidate) {
        try {
            candidate.setId(id);
            candidate.setUpdateTime(LocalDateTime.now());
            candidateService.updateById(candidate);
            return Result.success("更新成功", candidate);
        } catch (Exception e) {
            log.error("更新候选人失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除候选人")
    public Result<?> delete(@PathVariable Long id) {
        try {
            candidateService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除候选人失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询候选人")
    public Result<Candidate> getById(@PathVariable Long id) {
        Candidate candidate = candidateService.getById(id);
        if (candidate == null) {
            return Result.error("候选人不存在");
        }
        return Result.success(candidate);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询候选人")
    public Result<PageResult<Candidate>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String interviewStatus,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer isInRisk) {
        Page<Candidate> page = candidateService.pageQuery(current, size, candidateName, interviewStatus, position, isInRisk);
        
        PageResult<Candidate> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @PostMapping("/{id}/pass")
    @ApiOperation("面试通过")
    public Result<?> passInterview(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer score = params.get("score") != null ? Integer.valueOf(params.get("score").toString()) : null;
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;
            candidateService.passInterview(id, score, remark);
            return Result.success("面试通过");
        } catch (Exception e) {
            log.error("面试通过操作失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/fail")
    @ApiOperation("面试未通过")
    public Result<?> failInterview(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;
            candidateService.failInterview(id, remark);
            return Result.success("已标记未通过");
        } catch (Exception e) {
            log.error("面试未通过操作失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
