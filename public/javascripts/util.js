var get_json = function(url, funcSuccess, funcError) {
    $.ajax({
        url: url,
        processData:false,
        type: 'GET',
        beforeSend:function(jqXHR, settings){
            jqXHR.setRequestHeader("Content-Type", "application/json");
        },
        success: function(data, textStatus, jqXHR){
            funcSuccess(data);
        },
        error: function(jqXHR, textStatus, errorThrown){
            funcError(data);
        },
        complete: function(jqXHR,textStatus){
        }
    });
};

var groupChildren = function(children){
    var result=""
    $.each(children,function(i,item){
        if (item != "-1") {
            result = result + "<a href='#groupId"+item+"'>"+item+"</a>" + " "
        }
    });
    return result;
}

var groupButtons = function(id){
    var buttonsDiv = $("<div class='btn-group btn-block'>").append(
         $("<button type='button' class='btn btn-default col-lg-4'>").text("CIs").click(function(){accountingCIs(id);}),
         $("<button type='button' class='btn btn-default col-lg-4'>").text("Results").click(function(){accountingResults(id);}),
         $("<button type='button' class='btn btn-default col-lg-4'>").text("Freqs").click(function(){accountingFrequencies(id);})
    );
    return buttonsDiv;
}

var addGroups = function(data){
    $(function(){
        var contentDiv=$("div#groups");
        contentDiv.html("");
        contentDiv.append($("<table id='groupsTable' class='table'>"));
        $("<thead>").append(
            $("<tr>").append(
                $("<th class='col-lg-1'>").text("Group ID"),
                $("<th class='col-lg-1'>").text("Level Depth"),
                $("<th class='col-lg-6'>").text("Name"),
                $("<th class='col-lg-2'>").text("Children"),
                $("<th class='col-lg-2'>").text("Actions")
            )
        ).appendTo("table#groupsTable");
        $.each(data,function(i,item){
            $("<tr id='groupId"+item.id+"'>").append(
                $("<td>").text(item.id),
                $("<td>").text(item.level),
                $("<td>").text(item.name),
                $("<td>").html(groupChildren(item.children)),
                $("<td>").append(groupButtons(item.id))
            ).appendTo("table#groupsTable");
            $("<tr id='groupId"+item.id+"_CIs' style='display:none'>").append(
                $("<td colspan='5'>").append(
                    $("<div class='row'>").append(
                        $("<div class='col-lg-12'>").append(
                            $("<div id='diag"+item.id+"' class='container col-lg-12'>").html("")
                        )
                    ),
                    $("<div class='row'>").append(
                        $("<div class='col-lg-12'>").append(
                            $("<div id='CIs"+item.id+"' class='container col-lg-5'>").html(""),
                            $("<div id='CIs"+item.id+"_benford' class='container col-lg-2'>").html(""),
                            $("<div id='results"+item.id+"' class='container col-lg-3'>").html("")
                        )
                    )
                )
            ).appendTo("table#groupsTable");
        });
    });
}

var accountingGroups = function(){
    $(function(){
        get_json("/api/Groups",
                addGroups,
                function(){});
    });
}

var histogram = function(svgName, data, svgWidth, svgHeight, maxY){
    $(function(){
        var numberBars = data.length;
        var adjust = 1/maxY;
        var barWidth = (svgWidth - 2) / numberBars - 1;
        d3.select("svg#"+svgName)
            .selectAll('rect')
            .data(data)
            .enter()
            .append('rect')
            .attr('name',function(d, i){return i})
            .attr('x',function(d, i){return i * (barWidth + 1) + 2})
            .attr('y',function(d){return svgHeight - d * svgHeight * adjust})
            .attr('width',barWidth)
            .attr('height',function(d){return d * svgHeight * adjust})
            .on('mouseover',function(){
                d3.select(this).style('fill','purple');
            })
            .on('mouseout',function(){
                d3.select(this).style('fill','black');
            });
    });
}

var addFrequencies = function(id, data){
    $(function(){
        var contentDiv=$("div#frequencies");
        contentDiv.show();
        contentDiv.html("");
        /*$.each(data,function(groups,digits){
            contentDiv.append($("<table id='"+groups+"freqTable' class='table'>"));
            $.each(digits,function(digit,freqs){
                $("<tr>").append(
                    $("<td>").text(digit),
                    $("<td>").text(freqs)
                ).appendTo("table#"+groups+"freqTable");
            });
        });*/
        contentDiv.append("<svg id='f"+id+"_d1d2' width='600px' height='300px'></svg>");
        histogram("f"+id+"_d1d2",data[0].d1d2, 600, 300, Math.max(...data[0].d1d2));
        var maxY = Math.max(Math.max(...data[0].d1), Math.max(...data[0].d2));
        contentDiv.append("<svg id='f"+id+"_d1' width='300px' height='300px'></svg>");
        histogram("f"+id+"_d1",data[0].d1, 300, 300, maxY);
        contentDiv.append("<svg id='f"+id+"_d2' width='300px' height='300px'></svg>");
        histogram("f"+id+"_d2",data[0].d2, 300, 300, maxY);
    });
}

var accountingFrequencies = function(id){
    $(function(){
        get_json("/api/FreqByGroup/"+id,
                function(data){
                    addFrequencies(id, data);
                },
                function(){});
    });
}

var accountingLoad = function(){
    $(function(){
        $("p#loadStatus").text("Loading...");
        get_json("/api/Load",
                function(){
                    accountingGroups();
                    $("p#loadStatus").text("");
                    accountingCalc(1000);
                },
                function(){$("p#loadStatus").text("Error!");});
        });
};

var accountingCalc = function(numSamples){
    $(function(){
        $("p#calcStatus").text("Calculating...");
        get_json("/api/Calc/"+numSamples,
                function(){$("p#calcStatus").text("Calculated!");},
                function(){$("p#calcStatus").text("Error!");});
    });
}

var accountingCIs = function(id){
    $(function(){
        get_json("/api/CIsByGroup/"+id,
                function(data){
                    preProcessCIsByGroup(data);
                },
                function(){});
        get_json("/api/BenfCIsByGroup/"+id,
                function(data){
                    preProcessBenfCIsByGroup(data);
                    $("tr#groupId"+id+"_CIs").show();
                },
                function(){});
    });
}

var accountingResults = function(id){
    $(function(){
        get_json("/api/ResultsByGroup/"+id,
                function(data){
                    processResultsByGroup(data);
                },
                function(){});
    });
}

var resultsRows = function(tableName, stat, name) {
    $("<tr>").append(
        $("<td>").text(name),
        $("<td align='center'>").html("<span class='glyphicon "+((stat.contains)?"glyphicon-ok":"glyphicon-remove")+"'></span>"),
        $("<td align='center'>").html("<span class='glyphicon "+((stat.contains)?"glyphicon-ok":"glyphicon-remove")+"'></span>")
    ).appendTo("table#"+tableName);
}

var processResultsByGroup = function(data){
    $(function(){
        var diagDiv=$("div#diag"+data[0].id);
        diagDiv.html("");
        diagDiv.append("<table id='"+data[0].id+"_diagTable' class='table'>");
        $("<tr>").append(
            $("<td>").text("Stats Criteria Diagnostic: "),
            $("<td align='center'>").html("<span class='glyphicon "+((data[0].results.statsDiag==1)?"glyphicon-ok":"glyphicon-remove")+"'></span>"),
            $("<td>").text("Regs Criteria Diagnostic: "),
            $("<td>").html("<span class='glyphicon "+((data[0].results.n >= 1000)?((data[0].results.regsDiag==1)?"glyphicon-ok":((data[0].results.regsDiag==-1)?"glyphicon-remove":"glyphicon-question-sign")):"glyphicon-exclamation-sign")+"'></span>")
        ).appendTo("table#"+data[0].id+"_diagTable");
        var contentDiv=$("div#results"+data[0].id);
        contentDiv.html("");
        $.each(data[0].results,function(key, val){
            if (key == "d1d2" || key == "d1" || key == "d2" || key == "reg") {
                var tableName = data[0].id+"_"+key+"_ResultsTable";
                contentDiv.append($("<table id='"+tableName+"' class='table'>"));
                $("<thead>").append(
                    $("<tr>").append(
                        $("<th>").text(key),
                        $("<th>").text("Ovelaps"),
                        $("<th>").text("Contains")
                    )
                ).appendTo("table#"+tableName);
                if (key != "reg") {
                    resultsRows(tableName, val.mean, "Mean");
                    resultsRows(tableName, val.variance, "Variance");
                    resultsRows(tableName, val.skewness, "Skewness");
                    resultsRows(tableName, val.kurtosis, "Kurtosis");
                } else {
                    resultsRows(tableName, val.pearson, "Pearson");
                    if (data[0].results.n >= 1000) {
                        resultsRows(tableName, val.alpha0, "Alpha 0");
                        resultsRows(tableName, val.alpha1, "Alpha 1");
                        resultsRows(tableName, val.beta0, "Beta 0");
                        resultsRows(tableName, val.beta1, "Beta 1");
                    }
                }
            }
        });
    });
}

var preProcessCIsByGroup = function(data){
    processCIsByGroup(data, "", "Sample");
}

var preProcessBenfCIsByGroup = function(data){
    processCIsByGroup(data, "_benford", "Benford");
}

var CIsRows = function(tableName, stat, name, alpha, short) {
    $.each(stat,function(i,item){
        if (item.alpha == alpha) {
            if (short) {
                $("<tr>").append(
                    $("<td align='right'>").text(item.lower.toFixed(4)),
                    $("<td align='right'>").text(item.upper.toFixed(4))
                ).appendTo("table#"+tableName);
            } else {
                $("<tr>").append(
                    $("<td>").text(name),
                    $("<td align='right'>").text(item.alpha.toFixed(4)),
                    /*$("<td align='right'>").text(item.li.toFixed(4)),
                    $("<td align='right'>").text(item.ui.toFixed(4)),*/
                    $("<td align='right'>").text(item.lower.toFixed(4)),
                    $("<td align='right'>").text(item.upper.toFixed(4)),
                    $("<td align='right'>").text(item.t0.toFixed(4))
                ).appendTo("table#"+tableName);
            }
        };
    });
}

var processCIsByGroup = function(data, benford, title){
    $(function(){
        var short = (benford != "");
        var contentDiv=$("div#CIs"+data[0].id+benford);
        contentDiv.html("");
        $.each(data[0].CIs,function(key ,val){
            var tableName = data[0].id+"_"+key+"_CIsTable"+benford;
            if (key != "r") {
                contentDiv.append($("<table id='"+tableName+"' class='table'>"));
                if (short) {
                    $("<thead>").append(
                        $("<tr>").append(
                            $("<th>").text("Lower"),
                            $("<th>").text("Upper")
                        )
                    ).appendTo("table#"+tableName);
                } else {
                    $("<thead>").append(
                        $("<tr>").append(
                            $("<th>").text(key),
                            $("<th>").text("Alpha"),
                            /*$("<th>").text("Lower Index"),
                            $("<th>").text("Upper Index"),*/
                            $("<th>").text("Lower"),
                            $("<th>").text("Upper"),
                            $("<th>").text("Statistic")
                        )
                    ).appendTo("table#"+tableName);
                };
                CIsRows(tableName, val.mean, "Mean", 0.99, short);
                CIsRows(tableName, val.variance, "Variance", 0.99, short);
                CIsRows(tableName, val.skewness, "Skewness", 0.99, short);
                CIsRows(tableName, val.kurtosis, "Kurtosis", 0.99, short);
            } else if (val.n >= 1000) {
                contentDiv.append($("<table id='"+tableName+"' class='table'>"));
                if (short) {
                    $("<thead>").append(
                        $("<tr>").append(
                            $("<th>").text("Lower"),
                            $("<th>").text("Upper")
                        )
                    ).appendTo("table#"+tableName);
                } else {
                    $("<thead>").append(
                        $("<tr>").append(
                            $("<th>").text(key),
                            $("<th>").text("Alpha"),
                            /*$("<th>").text("Lower Index"),
                            $("<th>").text("Upper Index"),*/
                            $("<th>").text("Lower"),
                            $("<th>").text("Upper"),
                            $("<th>").text("Statistic")
                        )
                    ).appendTo("table#"+tableName);
                };
                CIsRows(tableName, val.pearson, "Pearson", 0.99, short);
                CIsRows(tableName, val.alpha0, "Alpha 0", 0.975, short);
                CIsRows(tableName, val.alpha1, "Alpha 1", 0.975, short);
                CIsRows(tableName, val.beta0, "Beta 0", 0.975, short);
                CIsRows(tableName, val.beta1, "Beta 1", 0.975, short);
            }
        });
    });
};

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
        $("div#loadProgress").css('width', (100*e.loaded/e.total)+'%').attr('aria-valuenow', (100*e.loaded/e.total));
    }
}

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

$(function(){
    $("a#linkHome").click(function(){showCurrentDiv("description");});
    $("a#linkAccounting").click(function(e){e.preventDefault();showCurrentDiv("accounting");});
    $("a#linkImage").click(function(){showCurrentDiv("image");});
    showCurrentDiv("description");
});

$(document).on('change', '.btn-file :file', function() {
  var input = $(this),
      numFiles = input.get(0).files ? input.get(0).files.length : 1,
      label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
  input.trigger('fileselect', [numFiles, label]);
});

$(document).ready( function() {
    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;
        if( input.length ) {
            input.val(log);
        } else {
            if( log ) alert(log);
        }
    });
});

var startInterval = function(size){
    var intervalId = setInterval(function(){$("button#btnProgress").click();},size);
    return intervalId;
}

var stopInterval = function(id){
    clearInterval(id);
}

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
