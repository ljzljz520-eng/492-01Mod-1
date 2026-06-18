import request from '@/utils/request'

export const candidateApi = {
  save(data) {
    return request({
      url: '/candidate',
      method: 'post',
      data
    })
  },

  update(id, data) {
    return request({
      url: `/candidate/${id}`,
      method: 'put',
      data
    })
  },

  delete(id) {
    return request({
      url: `/candidate/${id}`,
      method: 'delete'
    })
  },

  getById(id) {
    return request({
      url: `/candidate/${id}`,
      method: 'get'
    })
  },

  page(params) {
    return request({
      url: '/candidate/page',
      method: 'get',
      params
    })
  },

  passInterview(id, score, remark) {
    return request({
      url: `/candidate/${id}/pass`,
      method: 'post',
      data: { score, remark }
    })
  },

  failInterview(id, remark) {
    return request({
      url: `/candidate/${id}/fail`,
      method: 'post',
      data: { remark }
    })
  },

  listAvailableForTrial() {
    return request({
      url: '/candidate/available-for-trial',
      method: 'get'
    })
  }
}
