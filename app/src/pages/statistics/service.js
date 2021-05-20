import request from '@/utils/request';

export async function loadDetail(params) {
  return request('/api/stock/statisticsDetailView', {
    params,
  });
}
export async function loadCategory(params) {
  return request('/api/stock/statisticsCategoryView', {
    params,
  });
}
