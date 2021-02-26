var tradeData;

function reloadData(){
    $.ajax({
      url: "/trade/conform",
      data: {
        owner: "default",
        time: new Date().getTime()
      },
      success: function( result ) {
        tradeData = result;
        $("#json").JSONView(tradeData);
        $('#buy-btn').show();
        $('#json').JSONView('toggle', 2);
      }
    });
}

function settlement(){
    $.ajax({
      url: "/trade/settlement",
      data: {
        owner: "default",
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
        owner: "default",
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
        $('#json').JSONView('toggle', 2);
    });

    $('#reload-btn').on('click', function() {
        reloadData();
    });

    reloadData();
});