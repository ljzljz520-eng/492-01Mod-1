package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Team;

public interface TeamService extends IService<Team> {

    Page<Team> pageQuery(Long current, Long size, String teamName, String workShift);
}
