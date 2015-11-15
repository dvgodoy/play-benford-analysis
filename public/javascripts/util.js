var sampleCIData = "";

var get_json = function(url, funcSuccess, funcError, intervalId) {
    $.ajax({
        url: url,
        processData:false,
        type: 'GET',
        beforeSend:function(jqXHR, settings){
            jqXHR.setRequestHeader("Content-Type", "application/json");
        },
        success: function(data, textStatus, jqXHR){
            funcSuccess(data, intervalId);
        },
        error: function(jqXHR, textStatus, errorThrown){
            funcError(data);
        },
        complete: function(jqXHR,textStatus){
        }
    });
};

var loadStatus = 0;
var accountingLoad = function(url){
    loadStatus = 0;
    calcStatus = 0;
    $(function(){
        $("p#loadStatus").text("Loading...")
        get_json(url,
                function(){loadStatus = 1; $("p#loadStatus").text("Loaded!");},
                function(){loadStatus = -1; $("p#loadStatus").text("Error!");});
        });
};

var calcStatus = 0;
var accountingCalc = function(url){
    calcStatus = 0;
    $(function(){
        $("p#calcStatus").text("Calculating...")
        get_json(url,
                function(data){calcStatus = 1; $("p#calcStatus").text(data);},
                function(){calcStatus = -1; $("p#calcStatus").text("Error!");});
    });
}

var startInterval = function(size){
    var intervalId = setInterval(function(){$("button#btnProgress").click();},size);
    return intervalId;
}

var stopInterval = function(id){
    clearInterval(id);
}

var accountingResults = function(url){
    $(function(){
        var intervalId = setInterval(function(){$("button#btnProgress").click();},500);
        get_json(url,
                processCIsByGroup,
                function(){},
                intervalId);
    });
}

var processCIsByGroup = function(data, intervalId){
    $(function(){
        clearInterval(intervalId);
        $("button#btnProgress").click();
        sampleCIData = data;
        var contentDiv=$("div#results");
        contentDiv.html("");
        contentDiv.append("<ul>");
        contentDiv.append("<li>" + data[0].id + "</li>");
        contentDiv.append("<li>" + data[0].level + "</li>");
        $.each(data[0].CIs,function(key ,val){
            contentDiv.append("<li>" + key + "</li>");
            contentDiv.append("<li>" + val.n + "</li>");
            contentDiv.append("<li>" + val.mean + "</li>");
        });
        contentDiv.append("</ul>");
    });
};

var sparkProgress = function(url){
    $(function(){
        get_json(url,
                function(data){
                    var contentDiv=$("div#progress");
                    contentDiv.html("");
                    contentDiv.append("<ul>");
                    var jsonData = $.parseJSON(data);
                    $.each(jsonData,function(idx,item){
                        contentDiv.append("<li> jobId:" + item.jobId + " description:" + item.description + " status:" + item.status + "</li>");
                    });
                    contentDiv.append("</ul>");
                },
                function(){});
    });
}

var showCurrentDiv = function(divName) {
    $(function(){
        $("div#description").toggle(false);
        $("div#accounting").toggle(false);
        $("div#image").toggle(false);
        $("div#" + divName).toggle(true);
    });
}

$(function(){
    $("a#linkHome").click(function(){showCurrentDiv("description");});
    $("a#linkAccounting").click(function(e){e.preventDefault();showCurrentDiv("accounting");});
    $("a#linkImage").click(function(){showCurrentDiv("image");});
    showCurrentDiv("description");
});