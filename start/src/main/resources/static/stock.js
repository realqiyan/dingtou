function reloadData(){
    $.ajax({
      url: "/stock/query",
      data: {
        owner: $("#owner").val(),
        time: new Date().getTime()
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}

function add(){
    //type=stock&code=515180&increment=500&serviceFeeRate=0.0001&minServiceFee=0.2&market=sh&minTradeAmount=100
    $.ajax({
      method: 'GET',
      url: "/stock/add",
      data: {
        owner: $("#owner").val(),
        type: $('#type').val(),
        code: $('#code').val(),
        increment: $('#increment').val(),
        serviceFeeRate: $('#serviceFeeRate').val(),
        minServiceFee: $('#minServiceFee').val(),
        market: $('#market').val(),
        minTradeAmount: $('#minTradeAmount').val()
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}

$(function() {

    $('#toggle-btn').on('click', function() {
        $('#json').JSONView('toggle',2);
    });

    $('#reload-btn').on('click', function() {
        reloadData();
    });
    $('#add-btn').on('click', function() {
        add();
    });

    reloadData();
});