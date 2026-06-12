import request from '@/utils/request'

export const trialApi = {
  arrangeTrial(data) {
    return request({
      url: '/trial-assignment/arrange',
      method: 'post',
      data
    })
  },

  cancelTrial(id, reason) {
    return request({
      url: `/trial-assignment/${id}/cancel`,
      method: 'post',
      data: { reason }
    })
  },

  markAbsent(id) {
    return request({
      url: `/trial-assignment/${id}/absent`,
      method: 'post'
    })
  },

  getAssignmentById(id) {
    return request({
      url: `/trial-assignment/${id}`,
      method: 'get'
    })
  },

  assignmentPage(params) {
    return request({
      url: '/trial-assignment/page',
      method: 'get',
      params
    })
  },

  recordTrial(data) {
    return request({
      url: '/trial-record',
      method: 'post',
      data
    })
  },

  getRecordById(id) {
    return request({
      url: `/trial-record/${id}`,
      method: 'get'
    })
  },

  getRecordByAssignmentId(assignmentId) {
    return request({
      url: `/trial-record/assignment/${assignmentId}`,
      method: 'get'
    })
  },

  recordPage(params) {
    return request({
      url: '/trial-record/page',
      method: 'get',
      params
    })
  }
}
