import request from '@/utils/request';

export async function queryTrade(params) {
  return request('/api/trade/conform', {
    params,
  });
}
export async function buyTrade(params) {
  return request('/api/trade/buy', {
    method: 'POST',
    data: { ...params, method: 'post' },
  });
}
export async function updateTrade(params) {
  return request('/api/calculate/adjust', {
    method: 'POST',
    data: { ...params, method: 'post' },
  });
}
export async function settleTrade(params) {
  return request('/api/trade/settlement', {
    params,
  });
}
export async function addTrade(params) {
  return request('/api/stock/add', {
    method: 'POST',
    data: { ...params, method: 'update' },
  });
}
