package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.RiskList;
import com.scaffolding.service.RiskListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/risk-list")
@Api(tags = "风险名单管理")
public class RiskListController {

    @Autowired
    private RiskListService riskListService;

    @PostMapping("/add")
    @ApiOperation("加入风险名单")
    public Result<RiskList> addToRiskList(@RequestBody Map<String, Object> params) {
        try {
            Long candidateId = Long.valueOf(params.get("candidateId").toString());
            String reason = params.get("reason") != null ? params.get("reason").toString() : "";
            Long operatorId = params.get("operatorId") != null ? Long.valueOf(params.get("operatorId").toString()) : 1L;
            String operatorName = params.get("operatorName") != null ? params.get("operatorName").toString() : "管理员";
            RiskList result = riskListService.addToRiskList(candidateId, reason, operatorId, operatorName);
            return Result.success("加入成功", result);
        } catch (Exception e) {
            log.error("加入风险名单失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/remove")
    @ApiOperation("移除风险名单")
    public Result<?> removeFromRiskList(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String removeReason = params.get("removeReason") != null ? params.get("removeReason").toString() : "";
            Long operatorId = params.get("operatorId") != null ? Long.valueOf(params.get("operatorId").toString()) : 1L;
            riskListService.removeFromRiskList(id, removeReason, operatorId);
            return Result.success("移除成功");
        } catch (Exception e) {
            log.error("移除风险名单失败", e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询风险记录")
    public Result<RiskList> getById(@PathVariable Long id) {
        RiskList riskList = riskListService.getById(id);
        if (riskList == null) {
            return Result.error("风险记录不存在");
        }
        return Result.success(riskList);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询风险名单")
    public Result<PageResult<RiskList>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String status) {
        Page<RiskList> page = riskListService.pageQuery(current, size, candidateName, status);
        
        PageResult<RiskList> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @GetMapping("/check/{candidateId}")
    @ApiOperation("检查候选人是否在风险名单中")
    public Result<Boolean> checkInRiskList(@PathVariable Long candidateId) {
        boolean inRisk = riskListService.isInRiskList(candidateId);
        return Result.success(inRisk);
    }
}
