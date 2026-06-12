package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Team;
import com.scaffolding.mapper.TeamMapper;
import com.scaffolding.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Override
    public Page<Team> pageQuery(Long current, Long size, String teamName, String workShift) {
        Page<Team> page = new Page<>(current, size);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(teamName)) {
            wrapper.like(Team::getTeamName, teamName);
        }
        if (StringUtils.hasText(workShift)) {
            wrapper.eq(Team::getWorkShift, workShift);
        }

        wrapper.orderByDesc(Team::getCreateTime);
        return this.page(page, wrapper);
    }
}
