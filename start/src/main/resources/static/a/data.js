
function exportData(){
    $.ajax({
      url: "/export",
      data: {
        owner: $("#owner").val(),
        time: new Date().getTime()
      },
      success: function( result ) {
        $("#json").JSONView(result);
        $('#json').JSONView('toggle');
        $('#importData').val(JSON.stringify(result))
      }
    });
}

function submitData(data){
    $.ajax({
      method: 'POST',
      url: "/import",
      data: {
        owner: $("#owner").val(),
        data: data
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

    $('#copy-btn').on('click', function() {
        $("#importData").select();
        document.execCommand("Copy");
        //$temp.remove();
        alert("复制成功");
    });

    $('#submit-btn').on('click', function() {
        submitData($('#importData').val());
    });

    $('#reload-btn').on('click', function() {
        exportData();
    });

    exportData();
});