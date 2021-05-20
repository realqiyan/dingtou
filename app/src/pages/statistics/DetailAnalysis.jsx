import React from 'react';
import { loadDetail } from './service';


const loadDetailAnalysis = async(callback) =>{
  const data = await loadDetail();

  callback(data);
};

const loadView = () =>{
  var chartDom = document.getElementById('detail');
  var echarts = require("./echarts.min.js");
  var myChart = echarts.init(chartDom);

  loadDetailAnalysis((result) => {
    var option = {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: result,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    myChart.setOption(option);
  });
};

class DetailAnalysis extends React.Component {
  componentDidMount() {
    loadView();
  }

  render() {
    return (<>
        <div id="detail" style={{ width: '100%', height: 500 }} />
      </>
    );
  }
}

export default DetailAnalysis;
