import { Descriptions, Button, message } from 'antd';
import { queryStockById, updateStock } from '../service';
import React from 'react';
import ReactJson from 'react-json-view';
import {market, stockType, stockStatus} from "../../utils/configs"

const loadStockInfo = async(id, callback) =>{
  const result = await queryStockById({id: id});
  if(result?.success){
    callback(result.data);
  }
};

const handUpdate = async (values, callback) => {
  const result = await updateStock(values);
  callback(result);
};

class StockForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      editing: false,
      editJson:null,
      stock: [],
      jsonOptions: {
        onEdit: ( edit ) => {
          this.setState({editJson:edit.updated_src});
        },
        onAdd: ( add ) => {
        }
      }

    }
  }

  componentDidMount() {
    const {id} = this.props;
    loadStockInfo(id, (data)=>{
      this.setState({stock: data})
    });
  }

  render() {
    const {id} = this.props;
    const {stock, editing} = this.state;

    if(editing){
      return (
        <Descriptions style={{padding:10}} title="证券信息" bordered column={{xxl: 4, xl: 3, lg: 3, md: 3, sm: 2, xs: 1}}
                      extra={<div>
                        <Button type="primary" style={{display:this.state.editJson==null?'none':'inline'}} onClick={()=>{
                          const hide = message.loading('更新中...');
                          handUpdate(this.state.editJson, (result)=>{
                            hide();
                            if(result.success){
                              message.success('更新成功');
                              loadStockInfo(id, (data)=>{
                                this.setState({stock: data})
                              });
                            } else {
                              message.error('更新失败：'+result.message);
                            }
                          });
                        }}>提交</Button>&nbsp;
                        <Button type="primary" onClick={()=>{
                        this.setState({editing: !editing, editJson: null})}}>{editing?'取消':'编辑'}</Button>
                      </div>}>
          <Descriptions.Item label="">
            <ReactJson name='json' displayDataTypes={false} src={stock} {...this.state.jsonOptions}/>
          </Descriptions.Item>
        </Descriptions>
      );
    } else {
      return (
        <Descriptions style={{padding:10}} title="证券信息" bordered column={{xxl: 4, xl: 3, lg: 3, md: 3, sm: 2, xs: 1}}
                      extra={<Button type="primary" onClick={()=>{
                        this.setState({editing: !editing})
                      }}>{editing?'取消':'编辑'}</Button>}>
          <Descriptions.Item label="市场">{market()[stock.market]}</Descriptions.Item>
          <Descriptions.Item label="代码">{stock.code}</Descriptions.Item>
          <Descriptions.Item label="名称">{stock.name}</Descriptions.Item>
          <Descriptions.Item label="类型">{stockType()[stock.type]}</Descriptions.Item>
          <Descriptions.Item label="投入金额">{stock.totalFee}</Descriptions.Item>
          <Descriptions.Item label="持有数量">{stock.amount}</Descriptions.Item>
          <Descriptions.Item label="分类">{stock.category}</Descriptions.Item>
          <Descriptions.Item label="状态">{stockStatus()[stock.status]}</Descriptions.Item>
          <Descriptions.Item label="备注">{stock.marks}</Descriptions.Item>
          <Descriptions.Item label="交易配置">
            <ReactJson name='json' displayDataTypes={false} src={stock.tradeCfg} />
          </Descriptions.Item>

        </Descriptions>
      );
    }
  };
}

export default StockForm;
