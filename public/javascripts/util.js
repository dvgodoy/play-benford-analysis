/// API CALLS

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

var accountingGroups = function(){
    $(function(){
        get_json("/api/Groups",
                addGroups,
                function(){});
    });
}

var accountingFrequencies = function(id){
    $(function(){
        get_json("/api/FreqByGroup/"+id,
                function(data){
                    addFrequencies(id, data);
                    $("tr#groupId"+id+"_freqs").show();
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
                    showResult(id,"d1d2");
                },
                function(){});
    });
}

/// BUTTONS

var groupButtons = function(id){
    var buttonsDiv = $("<div class='btn-group btn-block'>").append(
         $("<button type='button' class='btn btn-default col-lg-6'>").text("Freqs").click(function(){
            if ($("div#freq"+id+"_d1d2").html() == "") {
                accountingFrequencies(id);
            } else {
                $("tr#groupId"+id+"_freqs").toggle();
            }
         }),
         $("<button type='button' class='btn btn-default col-lg-6'>").text("Results").click(function(){
            accountingCIs(id); accountingResults(id);})
         }
    );
    return buttonsDiv;
}

var freqButtons = function(id){
    var buttonsDiv = $("<div id='freqBtns' class='btn-group btn-block'>").append(
         $("<button type='button' class='btn btn-default col-lg-4'>").text("D1D2").click(function(){showFreq(id,"d1d2");}).addClass('active'),
         $("<button type='button' class='btn btn-default col-lg-4'>").text("D1").click(function(){showFreq(id,"d1");}),
         $("<button type='button' class='btn btn-default col-lg-4'>").text("D2").click(function(){showFreq(id,"d2");})
    );
    return buttonsDiv;
}

var resultButtons = function(id){
    var buttonsDiv = $("<div id='resultBtns' class='btn-group btn-block'>").append(
         $("<button type='button' class='btn btn-default col-lg-3'>").text("D1D2").click(function(){showResult(id,"d1d2");}).addClass('active'),
         $("<button type='button' class='btn btn-default col-lg-3'>").text("D1").click(function(){showResult(id,"d1");}),
         $("<button type='button' class='btn btn-default col-lg-3'>").text("D2").click(function(){showResult(id,"d2");}),
         $("<button type='button' class='btn btn-default col-lg-3'>").text("REG").click(function(){showResult(id,"reg");})
    );
    return buttonsDiv;
}

var buttonChildren = function(children){
    var buttonsDiv = $("<div class='btn-group btn-block'>");
    $.each(children,function(i,item){
        if (item != "-1") {
            $("<a type='button' class='btn btn-default' href='#groupId"+item+"'>").text(item).appendTo(buttonsDiv);
        }
    });
    return buttonsDiv;
}

/// SHOW/HIDE

var showFreq = function(id, digits){
    $("div#freq"+id+"_d1d2").hide();
    $("div#freq"+id+"_d1").hide();
    $("div#freq"+id+"_d2").hide();
    $("div#freq"+id+"_"+digits).show();
}

var showResult = function(id, digits){
    $("table#"+id+"_d1d2_ResultsTable").hide();
    $("table#"+id+"_d1d2_CIsTable").hide();
    $("table#"+id+"_d1d2_CIsTable_benford").hide();
    $("table#"+id+"_d1_ResultsTable").hide();
    $("table#"+id+"_d1_CIsTable").hide();
    $("table#"+id+"_d1_CIsTable_benford").hide();
    $("table#"+id+"_d2_ResultsTable").hide();
    $("table#"+id+"_d2_CIsTable").hide();
    $("table#"+id+"_d2_CIsTable_benford").hide();
    $("table#"+id+"_reg_ResultsTable").hide();
    $("table#"+id+"_r_CIsTable").hide();
    $("table#"+id+"_r_CIsTable_benford").hide();
    $("table#"+id+"_"+digits+"_ResultsTable").show();
    var name = (digits == 'reg') ? 'r' : digits;
    $("table#"+id+"_"+name+"_CIsTable").show();
    $("table#"+id+"_"+name+"_CIsTable_benford").show();
}

var showCurrentDiv = function(divName) {
    $(function(){
        $("div#description").toggle(false);
        $("div#accounting").toggle(false);
        $("div#image").toggle(false);
        $("div#" + divName).toggle(true);
    });
}

/// GROUP BUILDING

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
                $("<td>").append(buttonChildren(item.children)),
                $("<td>").append(groupButtons(item.id))
            ).appendTo("table#groupsTable");
            $("<tr id='groupId"+item.id+"_freqs' style='display:none'>").append(
                $("<td colspan='5' align='center'>").append(
                    $("<div class='row'>").append(
                        $("<div class='col-lg-12'>").append(
                            freqButtons(item.id)
                        )
                    ),
                    $("<div class='row'>").append(
                        $("<div class='col-lg-12'>").append(
                            $("<div id='freq"+item.id+"_d1d2' class='container col-lg-12'>").html(""),
                            $("<div id='freq"+item.id+"_d1' class='container col-lg-12'>").html(""),
                            $("<div id='freq"+item.id+"_d2' class='container col-lg-12'>").html("")
                        )
                    )
                )
            ).appendTo("table#groupsTable");
            $("<tr id='groupId"+item.id+"_CIs' style='display:none'>").append(
                $("<td colspan='5'>").append(
                    $("<div class='row'>").append(
                        $("<div class='col-lg-12'>").append(
                            $("<div id='diag"+item.id+"' class='container col-lg-4'>").html("")
                        )
                    ),
                    $("<div class='row'>").append(
                        $("<div class='col-lg-12'>").append(
                            resultButtons(item.id)
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

/// FREQUENCIES BUILDING

var addFrequencies = function(id, data){
    $(function(){
        var contentDiv=$("div#freq"+id+"_d1d2");
        contentDiv.show();
        contentDiv.html("");
        contentDiv.append(histogram("f"+id+"_d1d2", data[0].d1d2, 600, 300));
        var contentDiv=$("div#freq"+id+"_d1");
        contentDiv.hide();
        contentDiv.html("");
        contentDiv.append(histogram("f"+id+"_d1", data[0].d1, 300, 300));
        var contentDiv=$("div#freq"+id+"_d2");
        contentDiv.hide();
        contentDiv.html("");
        contentDiv.append(histogram("f"+id+"_d2", data[0].d2, 300, 300));
    });
}

var histogram = function(id, data, svgWidth, svgHeight){
    var svgHist = $("<svg id='"+id+"' width='"+svgWidth+"px' height='"+svgHeight+"px'>");
    var numberBars = data.length;
    if (numberBars == 10){
        var prob = Array.apply(null, {length: 10}).map(Function.call, function(x){
          var total = 0;
          for (init=1;init<10;init++) {
            total = total + (Math.log(1 + 1/(x + 10*init))*Math.LOG10E)
          };
          return total;
         });
    } else {
        var init = (numberBars == 90) ? 10 : ((numberBars == 10) ? 0 : 1)
        var prob = Array.apply(null, {length: numberBars}).map(Function.call, function(x){return (Math.log(1 + 1/(x + init))*Math.LOG10E)});
    };
    var adjust = 0.9/Math.max(Math.max(...data),prob[0]);
    var barWidth = (svgWidth - 2) / numberBars - 1;
    d3.select(svgHist.get(0))
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
            d3.select(this).style('fill','orange');
        })
        .on('mouseout',function(){
            d3.select(this).style('fill','black');
        });
    d3.select(svgHist.get(0))
        .selectAll('circle')
        .data(prob)
        .enter()
        .append('circle')
        .attr('cx', function(d, i){return i * (barWidth + 1) + 2 + barWidth / 2})
        .attr('cy', function(d){return svgHeight - d * svgHeight * adjust})
        .attr('r',barWidth / 4)
        .style('fill','red');
    return svgHist;
}

/*$.each(data,function(groups,digits){
    contentDiv.append($("<table id='"+groups+"freqTable' class='table'>"));
    $.each(digits,function(digit,freqs){
        $("<tr>").append(
            $("<td>").text(digit),
            $("<td>").text(freqs)
        ).appendTo("table#"+groups+"freqTable");
    });
});*/

/// DIAGNOSTICS

var diagnosticsTable = function(data){
    var diagTable = $("<table id='"+data[0].id+"_diagTable' class='table'>");
    $("<tr>").append(
        $("<td>").text("Stats Criteria Diagnostic: "),
        $("<td align='center'>").html("<span class='glyphicon "+((data[0].results.statsDiag==1)?"glyphicon-ok":"glyphicon-remove")+"'></span>")
    ).appendTo(diagTable);
    $("<tr>").append(
        $("<td>").text("Regs Criteria Diagnostic: "),
        $("<td align='center'>").html("<span class='glyphicon "+((data[0].results.n >= 1000)?((data[0].results.regsDiag==1)?"glyphicon-ok":((data[0].results.regsDiag==-1)?"glyphicon-remove":"glyphicon-question-sign")):"glyphicon-exclamation-sign")+"'></span>")
    ).appendTo(diagTable);
    return diagTable;
}

/// RESULTS

var resultsRows = function(resTable, stat, name) {
    $("<tr>").append(
        //$("<td>").text(name),
        $("<td align='center'>").html("<span class='glyphicon "+((stat.contains)?"glyphicon-ok":"glyphicon-remove")+"'></span>"),
        $("<td align='center'>").html("<span class='glyphicon "+((stat.contains)?"glyphicon-ok":"glyphicon-remove")+"'></span>")
    ).appendTo(resTable);
}

var resultsTable = function(id, n, digits, results){
    var tableName = id+"_"+digits+"_ResultsTable";
    var resTable = $("<table id='"+tableName+"' class='table'>");
    $("<thead>").append(
        $("<tr>").append(
            //$("<th>").text(digits),
            $("<th>").text("Ovelaps"),
            $("<th>").text("Contains")
        )
    ).appendTo(resTable);
    if (digits != "reg") {
        resultsRows(resTable, results.mean, "Mean");
        resultsRows(resTable, results.variance, "Variance");
        resultsRows(resTable, results.skewness, "Skewness");
        resultsRows(resTable, results.kurtosis, "Kurtosis");
    } else {
        resultsRows(resTable, results.pearson, "Pearson");
        if (n >= 1000) {
            resultsRows(resTable, results.alpha0, "Alpha 0");
            resultsRows(resTable, results.alpha1, "Alpha 1");
            resultsRows(resTable, results.beta0, "Beta 0");
            resultsRows(resTable, results.beta1, "Beta 1");
        }
    }
    return resTable;
}

var processResultsByGroup = function(data){
    $(function(){
        var diagDiv=$("div#diag"+data[0].id);
        diagDiv.html("");
        diagDiv.append(diagnosticsTable(data));
        var contentDiv=$("div#results"+data[0].id);
        contentDiv.html("");
        $.each(data[0].results,function(key, val){
            if (key == "d1d2" || key == "d1" || key == "d2" || key == "reg") {
                contentDiv.append(resultsTable(data[0].id, data[0].results.n, key, val));
            }
        });
    });
}

/// CONFIDENCE INTERVALS

var preProcessCIsByGroup = function(data){
    processCIsByGroup(data, "", "Sample");
}

var preProcessBenfCIsByGroup = function(data){
    processCIsByGroup(data, "_benford", "Benford");
}

var CIsRows = function(cisTable, stat, name, alpha, short) {
    $.each(stat,function(i,item){
        if (item.alpha == alpha) {
            if (short) {
                $("<tr>").append(
                    $("<td align='right'>").text(item.lower.toFixed(4)),
                    $("<td align='right'>").text(item.upper.toFixed(4))
                ).appendTo(cisTable);
            } else {
                $("<tr>").append(
                    $("<td>").text(name),
                    $("<td align='right'>").text(item.alpha.toFixed(4)),
                    /*$("<td align='right'>").text(item.li.toFixed(4)),
                    $("<td align='right'>").text(item.ui.toFixed(4)),*/
                    $("<td align='right'>").text(item.lower.toFixed(4)),
                    $("<td align='right'>").text(item.upper.toFixed(4)),
                    $("<td align='right'>").text(item.t0.toFixed(4))
                ).appendTo(cisTable);
            }
        };
    });
}

var CIsTable = function(id, digits, results, benford){
    var short = (benford != "");
    var tableName = id+"_"+digits+"_CIsTable"+benford;
    var cisTable = $("<table id='"+tableName+"' class='table'>");
    if (short) {
        $("<thead>").append(
            $("<tr>").append(
                $("<th>").text("Lower"),
                $("<th>").text("Upper")
            )
        ).appendTo(cisTable);
    } else {
        $("<thead>").append(
            $("<tr>").append(
                $("<th>").text(digits),
                $("<th>").text("Alpha"),
                $("<th>").text("Lower"),
                $("<th>").text("Upper"),
                $("<th>").text("Statistic")
            )
        ).appendTo(cisTable);
    };
    if (digits != "r") {
        CIsRows(cisTable, results.mean, "Mean", 0.99, short);
        CIsRows(cisTable, results.variance, "Variance", 0.99, short);
        CIsRows(cisTable, results.skewness, "Skewness", 0.99, short);
        CIsRows(cisTable, results.kurtosis, "Kurtosis", 0.99, short);
    } else if (results.n >= 1000) {
        CIsRows(cisTable, results.pearson, "Pearson", 0.99, short);
        CIsRows(cisTable, results.alpha0, "Alpha 0", 0.975, short);
        CIsRows(cisTable, results.alpha1, "Alpha 1", 0.975, short);
        CIsRows(cisTable, results.beta0, "Beta 0", 0.975, short);
        CIsRows(cisTable, results.beta1, "Beta 1", 0.975, short);
    };
    return cisTable;
}

var processCIsByGroup = function(data, benford, title){
    $(function(){
        var short = (benford != "");
        var contentDiv=$("div#CIs"+data[0].id+benford);
        contentDiv.html("");
        $.each(data[0].CIs,function(key ,val){
            contentDiv.append(CIsTable(data[0].id, key, val, benford));
        });
    });
};

var progressHandlingFunction = function(e){
    if(e.lengthComputable){
        $("div#loadProgress").css('width', (100*e.loaded/e.total)+'%').attr('aria-valuenow', (100*e.loaded/e.total));
    }
}

/// GENERAL FUNCTIONS

$(function(){
    $("a#linkHome").click(function(){showCurrentDiv("description");});
    $("a#linkAccounting").click(function(e){e.preventDefault();showCurrentDiv("accounting");});
    $("a#linkImage").click(function(){showCurrentDiv("image");});
    showCurrentDiv("description");

   $('body').on('click', '.btn-group button', function (e) {
       $(this).addClass('active');
       $(this).siblings().removeClass('active');
   });

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