function reloadData(){
    $.ajax({
      url: "/stock/query",
      data: {
        owner: "default",
        time: new Date().getTime()
      },
      success: function( result ) {
        $("#json").JSONView(result);
      }
    });
}

$(function() {

    $('#toggle-btn').on('click', function() {
        $('#json').JSONView('toggle');
    });

    $('#reload-btn').on('click', function() {
        reloadData();
    });

    reloadData();
});