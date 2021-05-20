import React from 'react';
import { loadCategory } from './service';

const loadCategoryAnalysis = async(callback) =>{
  const data = await loadCategory();

  callback(data);
};

const loadView = () =>{
  var chartDom = document.getElementById('category');
  var echarts = require("./echarts.min.js");
  var myChart = echarts.init(chartDom);

  loadCategoryAnalysis((result) => {
    var option = {
      tooltip: {
        trigger: 'item'
      },
      textStyle: {
        fontSize: 9
      },
      series: {
        type: 'sunburst',
        data: result,
        radius: [0, '95%'],
        sort: null,
        emphasis: {
          focus: 'ancestor'
        }
      }
    };
    myChart.setOption(option);
  });
};

class CategoryAnalysis extends React.Component {
  componentDidMount() {
    loadView();
  }

  render() {
    return (<>
        <div id="category" style={{ width: '100%', height: 500 }} />
      </>
    );
  }
}

export default CategoryAnalysis;
