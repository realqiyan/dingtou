import PageContainer from "@ant-design/pro-layout/lib/components/PageContainer";
import React from "react";
import StockForm from "./components/StockForm"
import StockOrderForm from './components/StockOrderForm'

const StockInfoDetail = (props) =>{
  const { match } = props;
  const { params } = match;

  return (
    <PageContainer style={{backgroundColor: 'white'}}>
      <StockForm id={params.id}/>
      <StockOrderForm id={params.id}/>
    </PageContainer>
  );
};

export default StockInfoDetail;
