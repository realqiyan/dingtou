function submitData(){
    $.ajax({
      method: 'POST',
      url: "/trade/adjust",
      data: {
        owner: "default",
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
function redo(){
    $.ajax({
      method: 'GET',
      url: "/statistic",
      data: {
        owner: "default"
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
        redo();
    });

});