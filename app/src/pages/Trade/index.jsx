import { PlusOutlined } from '@ant-design/icons';
import { Modal, Button, message, Space, Drawer, InputNumber } from 'antd';
import React, { useState, useRef } from 'react';
import {useIntl, FormattedMessage, history, connect} from 'umi';
import { PageContainer, FooterToolbar } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import ReactJson from 'react-json-view';
import TradeForm from './components/TradeForm';
import { queryTrade, settleTrade, addTrade, buyTrade, updateTrade } from './service';
import { CheckOutlined } from '@ant-design/icons';

const { confirm } = Modal;

const handleAdd = async (fields) => {
  const hide = message.loading('创建中...');

  try {
    const result = await addTrade({ ...fields });
    hide();
    if(result.success){
      message.success('创建成功');
      return true;
    } else {
      message.error('创建失败：'+result.message);
      return false;
    }
  } catch (error) {
    hide();
    message.error('创建失败!');
    return false;
  }
};

/**
 * 下单
 * @param selectedRows
 */

const handleBuy = async (selectedRows, callback) => {
  const hide = message.loading('下单中...');
  if (!selectedRows) return true;

  try {
    const result = await buyTrade({
      orders: selectedRows.map((row) => row),
    });

    hide();
    callback(result);
    return result.success;
  } catch (error) {
    hide();
    message.error('下单失败');
    return false;
  }
};

const handleSubmit = async  (selectedRows) => {
  const hide = message.loading('结算中...');
  if (!selectedRows) return true;

  try {
    await settleTrade({});
    hide();
    message.success('结算成功');
    return true;
  } catch (error) {
    hide();
    message.error('结算失败');
    return false;
  }
};

const handleUpdate = async (selectedRow, callback) =>{
  const hide = message.loading('更新中...');
  if (!selectedRow) return true;
  try {
    const result = await updateTrade({
      order: selectedRow,
    });
    
    hide();

    if(result.success){
      message.success('更新成功');
      callback(result);
    } else {
      message.error('更新失败:'+result.message);
    }

    return result.success;
  } catch (error) {
    hide();
    message.error('更新失败');
    return false;
  }
};

const TableList = () => {
  const [createModalVisible, handleModalVisible] = useState(false);
  const [showDetail, setShowDetail] = useState(false);
  const actionRef = useRef();
  const [currentRow, setCurrentRow] = useState();
  const [selectedRowsState, setSelectedRows] = useState([]);
  const [editingRow, setEditingRow] = useState([]);

  const intl = useIntl();

  const previewItem = record => {
    history.push(`/stock/info/`+record['stock&id']);
  };

  const formatCurrency = (num) => {
    num = num.toString().replace(/\$|\,/g,'');
    if(isNaN(num))
      num = "0";
    let sign = (num == (num = Math.abs(num)));
    num = Math.floor(num*100+0.50000000001);
    let cents = num%100;
    num = Math.floor(num/100).toString();
    if(cents<10){
      cents = "0" + cents;
    }
    for (var i = 0; i < Math.floor((num.length-(1+i))/3); i++)
      num = num.substring(0,num.length-(4*i+3))+','+
        num.substring(num.length-(4*i+3));
    return (((sign)?'':'-') + num + '.' + cents);
  };

  const editTabCell4Number = (cell, record) => {
    let domValue = record[cell.dataIndex];

    let showCommit = true;
    if(cell.dataIndex==='stock&totalFee'){
      showCommit = false;
    }
    if(editingRow.outId === record.outId){
      if(showCommit){
        return (
          <Space>
            <InputNumber min={0} defaultValue={domValue} onChange={(v)=>{
              domValue=v;
              editingRow["stock&amount"] = v;
            }}/>
            <Button size="small" shape="circle" icon={<CheckOutlined />} onClick={()=>{
              handleUpdate(editingRow, (result)=>{
                if(result.success) {
                  setEditingRow([]);
                  actionRef.current?.reloadAndRest?.();
                }
              });
            }}/>
          </Space>);
      } else {
        return (
          <Space>
            <InputNumber min={0} defaultValue={domValue} onChange={(v)=>{
              domValue=v;
              editingRow["stock&totalFee"] = v;
            }}/>
          </Space>);
      }

    } else {
      return (
        <a onClick={
          ()=>setEditingRow({...record})
        }>¥{formatCurrency(domValue)}</a>
      );
    }
  };

  const columns = [
    {
      title: "代码",
      dataIndex: 'stockCode',
      tip: '股票基金代码',
      render: (dom, entity) => {
        return (
          <a
            onClick={() => {
              previewItem(entity);
              /*setCurrentRow(entity);
              setShowDetail(true);*/
            }}>
            {dom}
          </a>
        );
      },
    },
    {
      title: "市场",
      sorter: true,
      dataIndex: 'stock&marketName',
      hideInSearch: true,
      tip: '证券市场',
    },
    {
      title: "类型",
      dataIndex: 'stock&typeName',
      hideInSearch: true,
      tip: '证券类型',
    },
    {
      title: "名称",
      dataIndex: 'stock&name',
      tip: '证券名称',
    },
    {
      title: "操作",
      dataIndex: 'type',
      sorter: true,
      hideInForm: true,
      hideInSearch: true,
      render: (val, entity) => {
        return (<>
            <a onClick={()=>{
              confirm({
                title: '确认下单',
                content: 'are you ok ?',
                onOk() {
                  handleBuy([entity], (result)=>{
                    if(result){
                      message.success('下单成功');
                      setSelectedRows([]);
                      actionRef.current?.reloadAndRest?.();
                    } else {
                      message.error('下单失败：'+result.message);
                    }
                  });
                },
                onCancel(){
                  console.log('Cancel');
                },
              })
            }}>{val === 'BUY'?'买入':'卖出'}</a>
            <a style={{marginLeft:5}}
              onClick={() => {
                setCurrentRow(entity);
                setShowDetail(true);
              }}>
              详情
            </a>
            </>
        );
      },
    },
    {
      title: "当前价格",
      dataIndex: 'stock&currentPrice',
      hideInForm: true,
      hideInSearch: true,
      renderText: (val) =>{
        return (<>
          ¥{val}
          </>);
      },
    },
    {
      title: '持有',
      hideInSearch: true,
      children: [
        {
          title: "投入金额",
          sorter: true,
          dataIndex: 'stock&totalFee',
          hideInSearch: true,
          valueType: 'money',
          render: (text, record, index, action, cell) => editTabCell4Number(cell, record),
        },
        {
          title: "数量",
          dataIndex: 'stock&amount',
          sorter: true,
          hideInSearch: true,
          valueType: 'digit',
          render: (text, record, index, action, cell) => editTabCell4Number(cell, record),
        },
        {
          title: "当前价值",
          dataIndex: 'stock&currentValue',
          hideInForm: true,
          hideInSearch: true,
          valueType: 'money',
        },
        {
          title: "盈利",
          dataIndex: 'stock&totalFee',
          hideInForm: true,
          hideInSearch: true,
          render: (text, record, index, action, cell) => {
            let v = record['stock&currentValue'] - text;
            if(v>=0){
              return (<><span style={{color: 'red'}}>¥{formatCurrency(v)}</span></>);
            } else {
              return (<><span style={{color: '#52c41a'}}>¥{formatCurrency(v)}</span></>);
            }
          },
        },
      ],
    },
    {
      title: '定投',
      hideInSearch: true,
      children: [
        {
          title: "数量",
          dataIndex: 'tradeAmount',
          sorter: true,
          hideInSearch: true,
          valueType: 'digit',
          tip: '本次定投买入数量(股)',
        },
        {
          title: "金额",
          dataIndex: 'tradeFee',
          sorter: true,
          hideInSearch: true,
          valueType: 'money',
          tip: '本次定投预计买入金额(元)',
        },
        {
          title: "费用",
          dataIndex: 'tradeServiceFee',
          sorter: true,
          hideInSearch: true,
          valueType: 'money',
          tip: '本次定投预计买入费用(元)',
        },
      ]
    },
  ];

  let totalMoney;
  return (
    <PageContainer>
      <ProTable
        bordered
        pagination={false}
        headerTitle={intl.formatMessage({
          id: 'pages.searchTable.title',
          defaultMessage: 'Enquiry form',
        })}
        actionRef={actionRef}
        rowKey="outId"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              handleModalVisible(true);
            }}
          >
            <PlusOutlined /> <FormattedMessage id="pages.searchTable.new" defaultMessage="New" />
          </Button>,
        ]}
        request={(params, sorter, filter) => queryTrade({ ...params, sorter, filter })}
        columns={columns}
        rowSelection={{
          onChange: (_, selectedRows) => {
            setSelectedRows(selectedRows);
          },
        }}
      />
      {selectedRowsState?.length > 0 && (
        <FooterToolbar
          extra={
            <div>
              <FormattedMessage id="pages.searchTable.chosen" defaultMessage="Chosen" />{' '}
              <a
                style={{
                  fontWeight: 600,
                }}
              >
                {selectedRowsState.length}
              </a>{' '}
              <FormattedMessage id="pages.searchTable.item" defaultMessage="项" />
              &nbsp;&nbsp;
              <span>
                {' 总投入金额：' }
                {totalMoney = selectedRowsState.reduce((pre, item) => pre + parseFloat(item.totalFee), 0)}{'元('}
                {totalMoney/10000.0}{'万)'}
              </span>
            </div>
          }
        >
          <Button
            onClick={()=>
              confirm({
                title: '确认下单',
                content: 'are you ok ?',
                onOk() {
                  handleBuy(selectedRowsState, (result)=>{
                    if(result){
                      message.success('下单成功');
                      setSelectedRows([]);
                      actionRef.current?.reloadAndRest?.();
                    } else {
                      message.error('下单失败：'+result.message);
                    }
                  });
                },
                onCancel(){
                  console.log('Cancel');
                },
                })
            }>
            下单
          </Button>
        </FooterToolbar>
      )}

      <TradeForm
        createModalVisible={createModalVisible}
        handleModalVisible={handleModalVisible}
        onSubmit={async (value) => {
          const success = await handleAdd(value);

          if (success) {
            handleModalVisible(false);

            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}
      />

      <Drawer
        width={600}
        visible={showDetail}
        onClose={() => {
          setCurrentRow(undefined);
          setShowDetail(false);
        }}
        closable={false}
      >
        <ReactJson name='data' displayDataTypes={false} src={currentRow?currentRow:{}}/>
      </Drawer>
    </PageContainer>
  );
};

export default TableList;
