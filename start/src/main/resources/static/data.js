var exportData;

function exportData(){
    $.ajax({
      url: "/export",
      data: {
        owner: "default",
        time: new Date().getTime()
      },
      success: function( result ) {
        exportData = result;
        $("#json").JSONView(exportData);
        $('#json').JSONView('toggle');
        $('#importData').val(JSON.stringify(exportData))
      }
    });
}

function submitData(data){
    $.ajax({
      method: 'POST',
      url: "/import",
      data: {
        owner: "default",
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

    exportData();
});