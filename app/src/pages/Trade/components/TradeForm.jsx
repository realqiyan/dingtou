import {
  ProFormSelect,
  ProFormText,
  ProFormDigit,
} from '@ant-design/pro-form';
import ProForm from "@ant-design/pro-form";
import ModalForm from "@ant-design/pro-form/lib/layouts/ModalForm";

const TradeForm = (props) => {
  return (
    <ModalForm
      visible={props.createModalVisible}
      onVisibleChange={props.handleModalVisible}
      title="创建订单"
      onFinish={props.onSubmit}>
      <ProForm.Group>
        <ProFormSelect
          rules={[
            {
              required: true,
              message: '请选择证券市场!',
            },
          ]}
          options={[
            {
              value: 'sh',
              label: '上交所',
            },
            {
              value: 'sz',
              label: '深交所',
            },
            {
              value: 'fund',
              label: '场外基金',
            },
          ]}
          width="sm"
          name="market"
          label="证券市场"
        />

        <ProFormSelect
          rules={[
            {
              required: true,
              message: '请选择证券类型!',
            },
          ]}
          options={[
            {
              value: 'stock',
              label: '场内证券',
            },
            {
              value: 'fund',
              label: '场外基金',
            },
          ]}
          width="sm"
          name="type"
          label="证券类型"
        />
      </ProForm.Group>

      <ProForm.Group>
        <ProFormText
          width="sm"
          name="code"
          label="证券代码"
          rules={[
            {
              required: true,
              message: '请输入证券代码!',
            },
          ]}
        />
        <ProFormText
          width="sm"
          name="name"
          label="证券名称"
          rules={[
            {
              required: true,
              message: '请输入证券名称!',
            },
          ]}
        />
        <ProFormText
          width="sm"
          name="category"
          label="分类"
          rules={[
            {
              required: true,
              message: '请输入证券分类!',
            },
          ]}
        />
        <ProFormDigit
          label="InputNumber"
          min={0}
          max={100000000}
          width="sm"
          name="increment"
          label="定投金额"
          placeholder="每周买入金额(元)"
          rules={[
            {
              required: true,
              message: '请输入定投金额!',
            }
          ]}
        />
        <ProFormDigit
          label="InputNumber"
          min={0}
          max={10}
          width="sm"
          name="serviceFeeRate"
          label="费率"
          placeholder="如万分之一：0.0001"
          rules={[
            {
              required: true,
              message: '请输入费率!',
            }
          ]}
        />
        <ProFormDigit
          label="InputNumber"
          min={0}
          max={10}
          width="sm"
          name="minServiceFee"
          label="最小服务费"
          placeholder="最小服务费：0.1元"
          rules={[
            {
              required: true,
              message: '请输入最小服务费!',
            }
          ]}
        />
        <ProFormDigit
          label="InputNumber"
          min={0}
          max={1000000000}
          width="sm"
          name="minTradeAmount"
          label="每手股数"
          placeholder="100/0.1"
          rules={[
            {
              required: true,
              message: '请输入每手股数!',
            }
          ]}
        />
      </ProForm.Group>
    </ModalForm>
  );
};

export default TradeForm;
