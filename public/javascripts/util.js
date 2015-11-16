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

var accountingGroups = function(){
    $(function(){
        var jobId = $("div#jobId").text();
        get_json("/api/"+jobId+"/Groups",
                function(data){
                    var contentDiv=$("div#groups");
                    contentDiv.html("");
                    contentDiv.append("<table>");
                    $.each(data,function(key,item){
                        contentDiv.append("<tr>");
                        contentDiv.append("<td>" + item.id + "</td>");
                        contentDiv.append("<td>" + item.level + "</td>");
                        contentDiv.append("<td>" + item.name + "</td>");
                        contentDiv.append("<td>" + item.children + "</td>");
                        contentDiv.append("</tr>");
                    });
                    contentDiv.append("</table>");
                },
                function(){});
    });
}

var accountingFrequencies = function(id){
    $(function(){
        var jobId = $("div#jobId").text();
        get_json("/api/"+jobId+"/FreqByGroup/"+id,
                function(data){
                    var contentDiv=$("div#frequencies");
                    contentDiv.html("");
                    contentDiv.append("<table>");
                    $.each(data,function(groups,digits){
                        contentDiv.append("<p>" + groups + "</p>");
                        contentDiv.append("<tr>");
                        $.each(digits,function(digit,freqs){
                            contentDiv.append("<p>" + digit + "</p>");
                            contentDiv.append("<tr>");
                            contentDiv.append("<td>" + freqs + "</td>");
                            contentDiv.append("</tr>");
                        });
                        contentDiv.append("</tr>");
                    });
                    contentDiv.append("</table>");
                    contentDiv.append("<svg id='d1' width='300px' height='300px'></svg>");

                    d3.select('svg#d1')
                        .selectAll('text')
                        .data(data[0].d1)
                        .enter()
                        .append('rect')
                        .attr('name',function(d, i){return i})
                        .attr('x',function(d, i){return i * 35})
                        .attr('y',function(d){return 300 - d * 300})
                        .attr('width',30)
                        .attr('height',function(d){return d * 300})
                        .on('mouseover',function(){
                            d3.select(this).style('fill','purple');
                        })
                        .on('mouseout',function(){
                          d3.select(this).style('fill','black');
                        });
                },
                function(){});
    });
}

var loadStatus = 0;
var accountingLoad = function(){
    loadStatus = 0;
    calcStatus = 0;
    $(function(){
        $("p#loadStatus").text("Loading...")
        get_json("/api/load",
                function(jobId){
                    $("div#jobId").text(jobId);
                    loadStatus = 1;
                    $("p#loadStatus").text("Loaded!");
                    accountingGroups();
                    accountingFrequencies(0);
                },
                function(){loadStatus = -1; $("p#loadStatus").text("Error!");});
        });
};

var calcStatus = 0;
var accountingCalc = function(){
    calcStatus = 0;
    $(function(){
        var jobId = $("div#jobId").text();
        $("p#calcStatus").text("Calculating...")
        get_json("/api/"+jobId+"/calc/1000",
                function(){calcStatus = 1; $("p#calcStatus").text("Calculated!");},
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
        var jobId = $("div#jobId").text();
        get_json("/api/"+jobId+"/CIsByGroup/0",
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