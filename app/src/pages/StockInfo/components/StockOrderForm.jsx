import React, { useState } from 'react';
import {Table, Input, InputNumber, Popconfirm, Form, Typography, message, Select} from 'antd';
import { queryStockOrderById } from '../service';

const { Option } = Select;
const loadStockOrders = async (stockId, pagination, filters, sorter, callback) => {
  const result = await queryStockOrderById({stockId: stockId, ...pagination});

  if(callback){
    callback(result);
  }

  return result;
};

const EditableCell = ({
                        editing,
                        dataIndex,
                        title,
                        inputType,
                        record,
                        index,
                        children,
                        ...restProps
                      }) => {
  let inputNode = (inputType === 'number' || inputType === 'money') ? <InputNumber /> : <Input />;
  if(inputType === 'select&type'){
    inputNode = <Select><Option value="buy">买入</Option><Option value="sell">卖出</Option><Option value="adjust">调整</Option></Select>;
  }
  return (
    <td {...restProps}>
      {editing ? (
        <Form.Item
          name={dataIndex}
          style={{
            margin: 0,
          }}
          rules={[
            {
              required: true,
              message: `Please Input ${title}!`,
            },
          ]}
        >
          {inputNode}
        </Form.Item>
      ) : (
        children
      )}
    </td>
  );
};

const StockOrderForm = (param) => {
  const [form] = Form.useForm();
  const {id} = param;
  const  stockId = id;
  const [pagination, setPagination] = useState({
    current: 1, pageSize: 20, total: 1
  });
  const [data, setData] = useState();
  const [editingKey, setEditingKey] = useState('');

  const handleTableChange = (pagination, filters, sorter) => {
    loadStockOrders(stockId, pagination, filters, sorter, (result)=>{
      pagination.current = pagination.current+1;
      if(result?.success){
        setData(result.data);
        setPagination({current: result.current, pageSize: result.pageSize, total: 20});
      }
    });
  };

  if(!data){
    handleTableChange(pagination, null, null);
  }

  const isEditing = (record) => record.id === editingKey;

  const edit = (record) => {
    form.setFieldsValue({
      name: '',
      age: '',
      address: '',
      ...record,
    });
    setEditingKey(record.id);
  };

  const cancel = () => {
    setEditingKey('');
  };

  const save = async (key) => {
    try {
      const row = await form.validateFields();
      const newData = [...data];
      const index = newData.findIndex((item) => key === item.id);

      let updateData;
      if (index > -1) {
        const item = newData[index];
        newData.splice(index, 1, { ...item, ...row });
        updateData = newData[index];
        setData(newData);
        setEditingKey('');
      } else {
        newData.push(row);
        updateData = newData;
        setData(newData);
        setEditingKey('');
      }
    } catch (errInfo) {
      console.log('Validate Failed:', errInfo);
    }
  };

  const columns = [
    {
      title: '操作类型',
      dataIndex: 'type',
      valueType: 'select&type',
      editable: true,
      render: (val, entity) => {
        return (<>
            {val === 'buy'?'买入':val==='adjust'?'调整':'卖出'}
          </>
        );
      },
    },
    {
      title: '交易金额',
      dataIndex: 'tradeFee',
      valueType: 'money',
      editable: true,
    },
    {
      title: '交易数量',
      dataIndex: 'tradeAmount',
      editable: true,
      valueType: 'number'
    },
    {
      title: '佣金',
      dataIndex: 'tradeServiceFee',
      valueType: 'money',
      editable: true,
    },
    {
      title: '状态',
      dataIndex: 'tradeStatus',
      editable: false,
      render: (val, entity) => {
        return (<>
            {val === 'done'?'完成':'处理中'}
          </>
        );
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      editable: false,
    },
    {
      title: '下单时间',
      dataIndex: 'tradeTime',
      editable: true,
    },
    {
      title: '操作',
      dataIndex: 'operation',
      render: (_, record) => {
        const editable = isEditing(record);
        return editable ? (
          <span>
            <Popconfirm title="确认更新?" onConfirm={()=>save(record.id)}>
              <a>更新</a>
            </Popconfirm>
            &nbsp;
            <Popconfirm title="确认取消?" onConfirm={()=>cancel()}>
              <a>取消</a>
            </Popconfirm>
          </span>
        ) : (
          <Typography.Link disabled={editingKey !== ''} onClick={() => edit(record)}>
            编辑
          </Typography.Link>
        );
      },
    },
  ];
  const mergedColumns = columns.map((col) => {
    if (!col.editable) {
      return col;
    }

    return {
      ...col,
      onCell: (record) => ({
        record,
        inputType: col.valueType,
        dataIndex: col.dataIndex,
        title: col.title,
        editing: isEditing(record),
      }),
    };
  });

  return (
    <Form form={form} component={false}>
      <Table
        components={{
          body: {
            cell: EditableCell,
          },
        }}
        bordered
        dataSource={data}
        columns={mergedColumns}
        rowClassName="editable-row"
        pagination={{
          onChange: cancel, ...pagination
        }}
        onChange={handleTableChange}
      />
    </Form>
  );
};

export default StockOrderForm;
