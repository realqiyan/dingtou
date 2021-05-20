function reloadDetailView(){
    $.ajax({
      url: "/stock/statisticsDetailView",
      data: {
        owner: $("#owner").val(),
        time: new Date().getTime()
      },
      success: function( result ) {
        var chartDom = document.getElementById('main');
        var myChart = echarts.init(chartDom);
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
      }
    });
}
function reloadSunburstView(){
    $.ajax({
      url: "/stock/statisticsCategoryView",
      data: {
        owner: $("#owner").val(),
        time: new Date().getTime()
      },
      success: function( result ) {
        var chartDom = document.getElementById('main');
        var myChart = echarts.init(chartDom);
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
      }
    });
}

$(function() {
    $('#reload-detail-btn').on('click', function() {
        reloadDetailView();
    });
    $('#reload-sunburst-btn').on('click', function() {
        reloadSunburstView();
    });
    reloadSunburstView();
});