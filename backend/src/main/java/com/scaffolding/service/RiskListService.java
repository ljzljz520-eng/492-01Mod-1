package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.RiskList;

public interface RiskListService extends IService<RiskList> {

    Page<RiskList> pageQuery(Long current, Long size, String candidateName, String status);

    RiskList addToRiskList(Long candidateId, String reason, Long operatorId, String operatorName);

    void removeFromRiskList(Long id, String removeReason, Long operatorId);

    boolean isInRiskList(Long candidateId);
}
