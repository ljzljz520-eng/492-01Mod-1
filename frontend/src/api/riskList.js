import request from '@/utils/request'

export const riskListApi = {
  addToRiskList(candidateId, reason, operatorId, operatorName) {
    return request({
      url: '/risk-list/add',
      method: 'post',
      data: { candidateId, reason, operatorId, operatorName }
    })
  },

  removeFromRiskList(id, removeReason, operatorId) {
    return request({
      url: `/risk-list/${id}/remove`,
      method: 'post',
      data: { removeReason, operatorId }
    })
  },

  getById(id) {
    return request({
      url: `/risk-list/${id}`,
      method: 'get'
    })
  },

  page(params) {
    return request({
      url: '/risk-list/page',
      method: 'get',
      params
    })
  },

  checkInRiskList(candidateId) {
    return request({
      url: `/risk-list/check/${candidateId}`,
      method: 'get'
    })
  }
}
