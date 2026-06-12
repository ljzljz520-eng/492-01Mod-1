package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.TrialAssignment;
import com.scaffolding.service.TrialAssignmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/trial-assignment")
@Api(tags = "试岗安排管理")
public class TrialAssignmentController {

    @Autowired
    private TrialAssignmentService trialAssignmentService;

    @PostMapping("/arrange")
    @ApiOperation("安排试岗")
    public Result<TrialAssignment> arrangeTrial(@RequestBody TrialAssignment assignment) {
        try {
            TrialAssignment result = trialAssignmentService.arrangeTrial(assignment);
            return Result.success("安排成功", result);
        } catch (Exception e) {
            log.error("安排试岗失败", e);
            return Result.error("安排失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @ApiOperation("取消试岗")
    public Result<?> cancelTrial(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String reason = params.get("reason") != null ? params.get("reason").toString() : "";
            trialAssignmentService.cancelTrial(id, reason);
            return Result.success("取消成功");
        } catch (Exception e) {
            log.error("取消试岗失败", e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/absent")
    @ApiOperation("标记未到岗")
    public Result<?> markAbsent(@PathVariable Long id) {
        try {
            trialAssignmentService.markAbsent(id);
            return Result.success("已标记未到岗");
        } catch (Exception e) {
            log.error("标记未到岗失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询试岗安排")
    public Result<TrialAssignment> getById(@PathVariable Long id) {
        TrialAssignment assignment = trialAssignmentService.getById(id);
        if (assignment == null) {
            return Result.error("试岗安排不存在");
        }
        return Result.success(assignment);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询试岗安排")
    public Result<PageResult<TrialAssignment>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<TrialAssignment> page = trialAssignmentService.pageQuery(current, size, candidateId, teamId, status, startDate, endDate);
        
        PageResult<TrialAssignment> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
