package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Team;
import com.scaffolding.service.TeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/team")
@Api(tags = "班组管理")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping
    @ApiOperation("新增班组")
    public Result<Team> save(@RequestBody Team team) {
        try {
            team.setCreateTime(LocalDateTime.now());
            team.setUpdateTime(LocalDateTime.now());
            if (team.getUsedQuota() == null) {
                team.setUsedQuota(0);
            }
            teamService.save(team);
            return Result.success("新增成功", team);
        } catch (Exception e) {
            log.error("新增班组失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新班组")
    public Result<Team> update(@PathVariable Long id, @RequestBody Team team) {
        try {
            team.setId(id);
            team.setUpdateTime(LocalDateTime.now());
            teamService.updateById(team);
            return Result.success("更新成功", team);
        } catch (Exception e) {
            log.error("更新班组失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除班组")
    public Result<?> delete(@PathVariable Long id) {
        try {
            teamService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除班组失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询班组")
    public Result<Team> getById(@PathVariable Long id) {
        Team team = teamService.getById(id);
        if (team == null) {
            return Result.error("班组不存在");
        }
        return Result.success(team);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询班组")
    public Result<PageResult<Team>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String workShift) {
        Page<Team> page = teamService.pageQuery(current, size, teamName, workShift);
        
        PageResult<Team> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有班组列表")
    public Result<?> list() {
        try {
            return Result.success(teamService.list());
        } catch (Exception e) {
            log.error("查询班组列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
