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

var sampleCIData = "";

var process_CIsByGroup = function(data){
    sampleCIData = data;
    var contentDiv=$("div#accounting");
    contentDiv.append("<ul>");
    contentDiv.append("<li>" + data[0].id + "</li>");
    contentDiv.append("<li>" + data[0].level + "</li>");
    $.each(data[0].CIs,function(key ,val){
        contentDiv.append("<li>" + key + "</li>");
        contentDiv.append("<li>" + val.n + "</li>");
        contentDiv.append("<li>" + val.mean[0].alpha + "</li>");
        contentDiv.append("<li>" + val.mean[0].lower + "</li>");
        contentDiv.append("<li>" + val.mean[0].upper + "</li>");
        contentDiv.append("<li>" + val.mean[0].t0 + "</li>");
    });
    contentDiv.append("</ul>");
};
