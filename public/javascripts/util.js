var get_json = function(url, func) {
    $.ajax({
        url: url,
        processData:false,
        type: 'GET',
        beforeSend:function(jqXHR, settings){
            jqXHR.setRequestHeader("Content-Type", "application/json");
        },
        success: function(data, textStatus, jqXHR){
            func(data);
        },
        error: function(jqXHR, textStatus, errorThrown){
        },
        complete: function(jqXHR,textStatus){
        }
    });
};