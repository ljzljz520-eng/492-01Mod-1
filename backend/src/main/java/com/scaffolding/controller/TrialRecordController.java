package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.TrialRecord;
import com.scaffolding.service.TrialRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/trial-record")
@Api(tags = "试岗记录管理")
public class TrialRecordController {

    @Autowired
    private TrialRecordService trialRecordService;

    @PostMapping
    @ApiOperation("记录试岗情况")
    public Result<TrialRecord> recordTrial(@RequestBody TrialRecord record) {
        try {
            TrialRecord result = trialRecordService.recordTrial(record);
            return Result.success("记录成功", result);
        } catch (Exception e) {
            log.error("记录试岗情况失败", e);
            return Result.error("记录失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询试岗记录")
    public Result<TrialRecord> getById(@PathVariable Long id) {
        TrialRecord record = trialRecordService.getById(id);
        if (record == null) {
            return Result.error("试岗记录不存在");
        }
        return Result.success(record);
    }

    @GetMapping("/assignment/{assignmentId}")
    @ApiOperation("根据安排ID查询试岗记录")
    public Result<TrialRecord> getByAssignmentId(@PathVariable Long assignmentId) {
        TrialRecord record = trialRecordService.getByAssignmentId(assignmentId);
        return Result.success(record);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询试岗记录")
    public Result<PageResult<TrialRecord>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String canConvert,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<TrialRecord> page = trialRecordService.pageQuery(current, size, candidateId, teamId, canConvert, startDate, endDate);
        
        PageResult<TrialRecord> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
