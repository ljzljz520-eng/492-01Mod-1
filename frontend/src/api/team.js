import request from '@/utils/request'

export const teamApi = {
  save(data) {
    return request({
      url: '/team',
      method: 'post',
      data
    })
  },

  update(id, data) {
    return request({
      url: `/team/${id}`,
      method: 'put',
      data
    })
  },

  delete(id) {
    return request({
      url: `/team/${id}`,
      method: 'delete'
    })
  },

  getById(id) {
    return request({
      url: `/team/${id}`,
      method: 'get'
    })
  },

  page(params) {
    return request({
      url: '/team/page',
      method: 'get',
      params
    })
  },

  list() {
    return request({
      url: '/team/list',
      method: 'get'
    })
  }
}
