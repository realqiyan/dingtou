var tradeData;

function reloadData(){
    $.ajax({
      url: "/trade/conform",
      data: {
        owner: $("#owner").val(),
        time: new Date().getTime()
      },
      success: function( result ) {
        tradeData = result;
        var output = document.getElementById("output");
        output.innerHTML = "";
        for(var i=0; i<tradeData.length; i++) {
            var obj = tradeData[i];
            output.innerHTML += "代码: " + obj.stock.code + "(" + obj.stock.name+ ")<br>";
            output.innerHTML += "当前价格: " + obj.stock.tradeCfg.attributes.currentTradePrice + "<br>";
            output.innerHTML += "当前估值: " + obj.stock.tradeCfg.attributes.currentTargetIndexValuation + "<br>";
            output.innerHTML += "预估交易金额: " + obj.tradeFee + "<br>";
            output.innerHTML += "预估服务费: " + obj.tradeServiceFee + "<br>";
            output.innerHTML += "交易份额: " + obj.tradeAmount + "<br>";
            output.innerHTML += "交易时间: " + obj.createTime + "<hr>";
        }
        $("#json").JSONView(tradeData);
        $('#buy-btn').show();
        $('#json').JSONView('toggle', 1);
      }
    });
}

function settlement(){
    $.ajax({
      url: "/trade/settlement",
      data: {
        owner: $("#owner").val(),
        time: new Date().getTime()
      },
      success: function( result ) {
        $("#result").text("settlement finish.");
      }
    });
}

function submitData(){
    $.ajax({
      method: 'POST',
      url: "/trade/buy",
      data: {
        owner: $("#owner").val(),
        orders: JSON.stringify(tradeData)
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}

$(function() {
    $('#buy-btn').hide();

    $('#buy-btn').on('click', function() {
        submitData();
    });
    $('#settlement-btn').on('click', function() {
        settlement();
    });

//    $('#toggle-btn').on('click', function() {
//        $('#json').JSONView('toggle');
//    });

    $('#toggle-level-btn').on('click', function() {
        $('#json').JSONView('toggle', 1);
    });

    $('#reload-btn').on('click', function() {
        reloadData();
    });

    reloadData();
});