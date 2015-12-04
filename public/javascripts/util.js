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
                },
                function(){});
    });
}

var accountingTests = function(id){
    $(function(){
        get_json("/api/TestsByGroup/"+id,
                function(data){
                    addTests(id, data);
                },
                function(){});
    });
}

var accountingLoad = function(){
    $(function(){
        $("p#loadStatus").text("Loading...");
        get_json("/api/acc/Load",
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
                    showResult(id,"d1d2");
                },
                function(){});
        get_json("/api/BenfCIsByGroup/"+id,
                function(data){
                    preProcessBenfCIsByGroup(data);
                    showResult(id,"d1d2");
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

var accountingExactParams = function(){
    $(function(){
        get_json("/api/ExactParams",
                function(data){
                    processExact(data);
                    showResult(id,"d1d2");
                },
                function(){});
    });
}

var imageResult = function(threshold, whiteBg){
    $(function(){
        get_json("/api/NewImage/"+threshold+"/"+whiteBg,
                function(data){
                    $("div#newImage").html('<img src="data:image/png;base64,' + data + '" class="img-responsive"/>');
                },
                function(){});
    });
}

var imageSBA = function(wSize){
    $(function(){
        get_json("/api/SBA/"+wSize,
                function(){},
                function(){});
    });
}

var imageLoad = function(){
    $(function(){
        $("div#originalImage").html("");
        $("div#newImage").html("");
        $("div#sba").hide();
        get_json("/api/Image",
                function(data){
                    $("div#originalImage").html('<img src="data:image/png;base64,' + data + '" class="img-responsive"/>');
                    $("div#sba").show();
                    imageSBA(15);
                    imageResult(0.8,1);
                },
                function(){});
        });
};


/// BUTTONS

var groupButtons = function(id){
    var buttonsDiv = $("<div class='btn-group btn-block'>").append(
         $("<button type='button' class='btn btn-default col-lg-6'>").text("Freqs").click(function(){
            if ($("div#freq"+id+"_d1d2").html() == "") {
                accountingFrequencies(id);
                accountingTests(id);
                $("tr#groupId"+id+"_freqs").show();
            } else {
                $("tr#groupId"+id+"_freqs").toggle();
            }
         }),
         $("<button type='button' class='btn btn-default col-lg-6'>").text("Results").click(function(){
            if ($("div#results"+id).html() == "") {
                if ($("div#freq"+id+"_d1d2").html() == "") {
                    accountingFrequencies(id);
                    accountingTests(id);
                };
                accountingCIs(id);
                accountingResults(id);
            } else {
                $("tr#groupId"+id+"_CIs").toggle();
            }
         })
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
    $(function(){
        $("div#freq"+id+"_d1d2").hide();
        $("div#freq"+id+"_d1").hide();
        $("div#freq"+id+"_d2").hide();
        $("div#freq"+id+"_"+digits).show();
    });
}

var showResult = function(id, digits){
    $(function(){
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

        $("div#exactParam"+id).find("table#exactParam_d1d2").hide();
        $("div#exactParam"+id).find("table#exactParam_d1").hide();
        $("div#exactParam"+id).find("table#exactParam_d2").hide();
        $("div#exactParam"+id).find("table#exactParam_r").hide();
        var regRows = $("table#"+id+"_r_CIsTable tr").length;

        $("table#"+id+"_diagTable").show();
        $("table#"+id+"_"+digits+"_ResultsTable").show();
        var name = (digits == 'reg') ? 'r' : digits;
        $("table#"+id+"_"+name+"_CIsTable").show();
        $("table#"+id+"_"+name+"_CIsTable_benford").show();
        $("div#exactParam"+id+" > table#exactParam_"+name).show();
        if (regRows == 3) {
            var rTable = $("div#exactParam"+id+" > table#exactParam_"+name);
            rTable.find("tr#alpha0").hide();
            rTable.find("tr#alpha1").hide();
            rTable.find("tr#beta0").hide();
            rTable.find("tr#beta1").hide();
        }
        $("tr#groupId"+id+"_CIs").show();
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
                            $("<div id='exactParam"+item.id+"' class='exactParams container col-lg-2'>").html(""),
                            $("<div id='results"+item.id+"' class='container col-lg-3'>").html("")
                        )
                    )
                )
            ).appendTo("table#groupsTable");
        });
        accountingExactParams();
    });
}

/// FREQUENCIES BUILDING

var addTests = function(id, data){
    $(function(){
        $.each(data.z[0],function(key, val){
            if (key != "count") {
                var digits = key.toLocaleLowerCase().slice(4);
                var contentDiv=$("div#freq"+id+"_"+digits);
                var testTable = $("<table id=test'"+id+"_"+digits+"' class='table'>");
                contentDiv.append(
                    $("<tr>").append(
                        $("<td>").text("Z Test - Digits Rejected: "),
                        $("<td>").text((val.rejected.length > 0) ? val.rejected : "None")
                    ).appendTo(testTable)
                );
                $.each(val.rejected, function(idx, name){
                    d3.select("svg#f"+id+"_"+digits)
                        .select("rect#f"+name)
                        .attr("color","red")
                        .style("fill","red");
                });
            };
        });
        $.each(data.chisquared[0],function(key,val){
            if (key != "count") {
                var digits = key.toLocaleLowerCase().slice(4);
                var contentDiv=$("div#freq"+id+"_"+digits);
                var testTable = $("<table id=test'"+id+"_"+digits+"' class='table'>");
                contentDiv.append(
                    $("<tr>").append(
                        $("<td>").text("Chi-Squared Test - Overall Distribution: "),
                        $("<td>").text((val.rejected.length > 0) ? "Rejected" : "Accepted")
                    ).appendTo(testTable)
                );
            };
        });
    });
}

var addFrequencies = function(id, data){
    $(function(){
        var contentDiv=$("div#freq"+id+"_d1d2");
        contentDiv.show();
        contentDiv.html("");
        contentDiv.append(histogram("f"+id+"_d1d2", data[0].d1d2, 900, 300));
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
    var init = (numberBars == 90) ? 10 : ((numberBars == 10) ? 0 : 1)
    if (numberBars == 10){
        var prob = Array.apply(null, {length: 10}).map(Function.call, function(x){
          var total = 0;
          for (i=1;i<10;i++) {
            total = total + (Math.log(1 + 1/(x + 10*i))*Math.LOG10E)
          };
          return total;
         });
    } else {
        var prob = Array.apply(null, {length: numberBars}).map(Function.call, function(x){return (Math.log(1 + 1/(x + init))*Math.LOG10E)});
    };
    var adjust = 0.8/Math.max(Math.max(...data),prob[0]);
    var barWidth = (svgWidth - 2) / numberBars - 1;
    d3.select(svgHist.get(0))
        .selectAll('rect')
        .data(data)
        .enter()
        .append('rect')
        .attr('id',function(d, i){return "f" + parseInt(i + init);})
        .attr('x',function(d, i){return i * (barWidth + 1) + 2;})
        .attr('y',function(d){return svgHeight - d * svgHeight * adjust - 30;})
        .attr('width',barWidth)
        .attr('height',function(d){return d * svgHeight * adjust;})
        /*.on('mouseover',function(){
            d3.select(this).style('fill','orange');
        })
        .on('mouseout',function(){
            d3.select(this).style('fill','black');
        });*/
    d3.select(svgHist.get(0))
        .selectAll('circle')
        .data(prob)
        .enter()
        .append('circle')
        .attr('cx', function(d, i){return i * (barWidth + 1) + 2 + barWidth / 2;})
        .attr('cy', function(d){return svgHeight - d * svgHeight * adjust - 30;})
        .attr('r',barWidth / 4)
        .style('fill','red');
    var seq = Array.apply(0,Array((numberBars == 90) ? 9 : numberBars)).map(function(x,i){ return i * ((numberBars == 90) ? 10 : 1) + init; });
    d3.select(svgHist.get(0))
        .selectAll('text')
        .data(seq)
        .enter()
        .append('text')
        .attr('x', function(d){return (d - seq[0]) * (barWidth + 1) + ((numberBars == 90) ? 0 : (2 + barWidth / 2));})
        .attr('y', svgHeight - 10)
        .text(function(d){return d;});
    return svgHist;
}

/// DIAGNOSTICS

var diagnosticsTable = function(data){
    var diagTable = $("<table id='"+data[0].id+"_diagTable' class='table' style='display:none'>");
    $("<tr>").append(
        $("<th colspan='2'>").text("Bootstrap Results")
    ).appendTo(diagTable);
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
    var resTable = $("<table id='"+tableName+"' class='table' style='display:none'>");
    $("<thead>").append(
        $("<tr>").append(
            $("<th colspan='2'>").text("Result")
        ),
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

        if (data[0].results.statsDiag==1 && (data[0].results.n < 1000 || data[0].results.regsDiag!=-1)) {
            d3.select("svg#f"+data[0].id+"_d1d2").selectAll("[color=red]").style("fill","green");
            d3.select("svg#f"+data[0].id+"_d1").selectAll("[color=red]").style("fill","green");
            d3.select("svg#f"+data[0].id+"_d2").selectAll("[color=red]").style("fill","green");
        }

        if (data[0].results.n < 1000) {
            groupColor = (data[0].results.statsDiag==1) ? "rgb(96,192,0)" : "rgb(255,32,0)"
        } else {
            if (data[0].results.regsDiag==1) {
                groupColor = (data[0].results.statsDiag==1) ? "rgb(96,192,0)" : "rgb(255,128,0)"
            } else if (data[0].results.regsDiag==-1) {
                groupColor = (data[0].results.statsDiag==1) ? "rgb(255,128,0)" : "rgb(255,32,0)"
            } else {
                groupColor = "rgb(96,192,96)"
            }
        }
        $("tr#groupId"+data[0].id+" > td").css("background-color",groupColor);

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
    processCIsByGroup(data, "");
}

var preProcessBenfCIsByGroup = function(data){
    processCIsByGroup(data, "_benford");
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
                    $("<td align='right'>").text((item.alpha*100).toFixed(1)+"%"),
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
    var cisTable = $("<table id='"+tableName+"' class='table' style='display:none'>");
    if (short) {
        $("<thead>").append(
            $("<tr>").append(
                $("<th colspan='2' align='center'>").text("Benford Sample")
            ),
            $("<tr>").append(
                $("<th>").text("Lower"),
                $("<th>").text("Upper")
            )
        ).appendTo(cisTable);
    } else {
        $("<thead>").append(
            $("<tr>").append(
                $("<th>"),
                $("<th>"),
                $("<th colspan='3' align='center'>").text("Bootstrap Sample")
            ),
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
    } else {
        CIsRows(cisTable, results.pearson, "Pearson", 0.99, short);
        if (results.n >= 1000) {
            CIsRows(cisTable, results.alpha0, "Alpha 0", 0.975, short);
            CIsRows(cisTable, results.alpha1, "Alpha 1", 0.975, short);
            CIsRows(cisTable, results.beta0, "Beta 0", 0.975, short);
            CIsRows(cisTable, results.beta1, "Beta 1", 0.975, short);
        }
    };
    return cisTable;
}

var processCIsByGroup = function(data, benford){
    $(function(){
        var short = (benford != "");
        var contentDiv=$("div#CIs"+data[0].id+benford);
        contentDiv.html("");
        $.each(data[0].CIs,function(key ,val){
            contentDiv.append(CIsTable(data[0].id, key, val, benford));
        });
    });
};

/// EXACT

var exactRows = function(exactTable, stat, name){
    $("<tr id='"+name+"'>").append(
        $("<td align='right'>").text(stat[0].toFixed(4))
    ).appendTo(exactTable);
}

var exactParmTable = function(digits, results){
    var exactTable = $("<table id='exactParam_"+digits+"' class='table' style='display:none'>");
    $("<thead>").append(
        $("<tr>").append(
            $("<th align='center'>").text("Benford")
        ),
        $("<tr>").append(
            $("<th>").text("Exact")
        )
    ).appendTo(exactTable);
    if (digits != "r") {
        exactRows(exactTable, results.mean, "mean");
        exactRows(exactTable, results.variance, "variance");
        exactRows(exactTable, results.skewness, "skewness");
        exactRows(exactTable, results.kurtosis, "kurtosis");
    } else {
        exactRows(exactTable, results.pearson, "pearson");
        exactRows(exactTable, results.alpha0, "alpha0");
        exactRows(exactTable, results.alpha1, "alpha1");
        exactRows(exactTable, results.beta0, "beta0");
        exactRows(exactTable, results.beta1, "beta1");
    };
    return exactTable;
}

var processExact = function(data){
    $(function(){
        var contentDiv=$(".exactParams");
        contentDiv.html("");
        $.each(data,function(key ,val){
            contentDiv.append(exactParmTable(key, val));
        });
    });
};

/// GENERAL FUNCTIONS

var progressHandlingFunction = function(e){
    if(e.lengthComputable){
        $("div#loadProgress").css('width', (100*e.loaded/e.total)+'%').attr('aria-valuenow', (100*e.loaded/e.total));
    }
}

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
            $("div#load").show();
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