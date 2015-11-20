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
        get_json("/api/Groups",
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

var histogram = function(svgName, data){
    $(function(){
        d3.select('svg#'+svgName)
            .selectAll('text')
            .data(data)
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
    });
}

var accountingFrequencies = function(id){
    $(function(){
        get_json("/api/FreqByGroup/"+id,
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
                    histogram('d1',data[0].d1)
                    contentDiv.append("<svg id='d2' width='300px' height='300px'></svg>");
                    histogram('d2',data[0].d2)
                },
                function(){});
    });
}

var accountingLoad = function(){
    $(function(){
        $("p#loadStatus").text("Loading...")
        get_json("/api/Load",
                function(){
                    $("p#loadStatus").text("Loaded!");
                    accountingGroups();
                    accountingFrequencies(0);
                },
                function(){$("p#loadStatus").text("Error!");});
        });
};

var accountingCalc = function(numSamples){
    $(function(){
        $("p#calcStatus").text("Calculating...")
        get_json("/api/Calc/"+numSamples,
                function(){$("p#calcStatus").text("Calculated!");},
                function(){$("p#calcStatus").text("Error!");});
    });
}

var startInterval = function(size){
    var intervalId = setInterval(function(){$("button#btnProgress").click();},size);
    return intervalId;
}

var stopInterval = function(id){
    clearInterval(id);
}

var accountingResults = function(id){
    $(function(){
        var intervalId = setInterval(function(){$("button#btnProgress").click();},500);
        get_json("/api/CIsByGroup/"+id,
                processCIsByGroup,
                function(){},
                intervalId);
    });
}

var statRows = function(contentDiv, stat, name) {
    contentDiv.append("<tr>");
    contentDiv.append("<td>" + name + "</td>");
    contentDiv.append("</tr>");
    $.each(stat,function(idx, item){
        contentDiv.append("<tr>");
        contentDiv.append("<td>" + item.alpha + "</td>");
        contentDiv.append("<td>" + item.li + "</td>");
        contentDiv.append("<td>" + item.ui + "</td>");
        contentDiv.append("<td>" + item.lower + "</td>");
        contentDiv.append("<td>" + item.upper + "</td>");
        contentDiv.append("<td>" + item.t0 + "</td>");
        contentDiv.append("</tr>");
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
            if (key != "r") {
                contentDiv.append("<li>" + key + "</li>");
                contentDiv.append("<li>" + val.n + "</li>");
                statRows(contentDiv, val.mean, "Mean");
                statRows(contentDiv, val.variance, "Variance");
                statRows(contentDiv, val.skewness, "Skewness");
                statRows(contentDiv, val.kurtosis, "Kurtosis");
            } else if (val.n >= 1000) {
                contentDiv.append("<li>" + key + "</li>");
                contentDiv.append("<li>" + val.n + "</li>");
                statRows(contentDiv, val.alpha0, "Alpha 0");
                statRows(contentDiv, val.alpha1, "Alpha 1");
                statRows(contentDiv, val.beta0, "Beta 0");
                statRows(contentDiv, val.beta1, "Beta 1");
            }
        });
        contentDiv.append("</ul>");
    });
};

var sparkProgress = function(id){
    $(function(){
        get_json("/api/Progress",
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

var progressHandlingFunction = function(e){
    if(e.lengthComputable){
        $('progress').attr({value:e.loaded,max:e.total});
    }
}

$(function(){
    $("a#linkHome").click(function(){showCurrentDiv("description");});
    $("a#linkAccounting").click(function(e){e.preventDefault();showCurrentDiv("accounting");});
    $("a#linkImage").click(function(){showCurrentDiv("image");});
    showCurrentDiv("description");
});

$(function(){
    $("form#uploadForm").submit(function(e) {
        var formData = new FormData($(this)[0]);
        $.ajax({
            url: $(this).attr('action'),  //Server script to process data
            type: 'POST',
            xhr: function() {  // Custom XMLHttpRequest
                var myXhr = $.ajaxSettings.xhr();
                if(myXhr.upload){ // Check if upload property exists
                    myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // For handling the progress of the upload
                }
                return myXhr;
            },
            //Ajax events
            beforeSend:function(jqXHR, settings){
                $("progress").show();
            },
            success: function(data, textStatus, jqXHR){
            },
            error: function(jqXHR, textStatus, errorThrown){
            },
            complete: function(jqXHR,textStatus){
            },
            // Form data
            data: formData,
            //Options to tell jQuery not to process data or worry about content-type.
            cache: false,
            contentType: false,
            processData: false
        }).done(function(data){
            console.log(data);
        });
        e.preventDefault();
    });
});
