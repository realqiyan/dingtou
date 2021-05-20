import request from '@/utils/request';

export async function queryStockById(params) {
  return request('/api/stock/queryById', {
    params,
  });
}

export async function queryStockOrderById(params){
  return request('/api/order/queryStockOrderByStockId', {
    params,
  });
}

export async function updateStock(params) {
  return request('/api/stock/update', {
    method: 'POST',
    data: { ...params, method: 'post' },
});
}
