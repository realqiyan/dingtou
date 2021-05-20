import React from 'react';
import { Tabs } from 'antd';
import { PageContainer } from '@ant-design/pro-layout';
import CategoryAnalysis from './statistics/CategoryAnalysis';
import DetailAnalysis from './statistics/DetailAnalysis'

const { TabPane } = Tabs;

export default () => {

  return (
    <PageContainer>
      <Tabs type="card" style={{backgroundColor: 'white'}}>
        <TabPane tab="统计" key="detail">
          <CategoryAnalysis />
        </TabPane>
        <TabPane tab="明细" key="category">
          <DetailAnalysis />
        </TabPane>
      </Tabs>
    </PageContainer>
  )
}

