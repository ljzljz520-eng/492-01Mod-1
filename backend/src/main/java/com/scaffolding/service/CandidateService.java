package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Candidate;

public interface CandidateService extends IService<Candidate> {

    Page<Candidate> pageQuery(Long current, Long size, String candidateName, String interviewStatus, String position, Integer isInRisk);

    void passInterview(Long id, Integer score, String remark);

    void failInterview(Long id, String remark);
}
