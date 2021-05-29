function submitData(){
    $.ajax({
      method: 'POST',
      url: "/trade/adjust",
      data: {
        owner: $("#owner").val(),
        type: $('#type').val(),
        code: $('#code').val(),
        tradeFee: $('#tradeFee').val(),
        tradeAmount: $('#tradeAmount').val(),
        tradeServiceFee: $('#tradeServiceFee').val()
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}
function redoStatistic(){
    $.ajax({
      method: 'GET',
      url: "/trade/statistic",
      data: {
        owner: $("#owner").val()
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}

function redoAdjust(){
    $.ajax({
      method: 'POST',
      url: "/trade/auto-adjust",
      data: {
        owner: $("#owner").val()
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}

$(function() {
    $('#submit-btn').on('click', function() {
        submitData();
    });
    $('#redo-btn').on('click', function() {
        redoStatistic();
    });
    $('#build-adjust-btn').on('click', function() {
        redoAdjust();
    });

});