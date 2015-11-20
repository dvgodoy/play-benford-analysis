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

var groupChildren = function(children){
    var result=""
    $.each(children,function(i,item){
        if (item != "-1") {
            result = result + "<a href='#groupId"+item+"'>"+item+"</a>" + " "
        }
    });
    return result;
}

var accountingGroups = function(){
    $(function(){
        get_json("/api/Groups",
                function(data){
                    var contentDiv=$("div#groups");
                    contentDiv.html("");
                    contentDiv.append($("<table id='groupsTable' class='table'>"));
                    $("<thead>").append(
                        $("<tr>").append(
                            $("<th class='col-lg-1'>").text("Id"),
                            $("<th class='col-lg-1'>").text("Level"),
                            $("<th class='col-lg-8'>").text("Name"),
                            $("<th class='col-lg-2'>").text("Children")
                        )
                    ).appendTo("table#groupsTable");
                    $.each(data,function(i,item){
                        $("<tr id='groupId"+item.id+"'>").append(
                            $("<td>").text(item.id),
                            $("<td>").text(item.level),
                            $("<td>").text(item.name),
                            $("<td>").html(groupChildren(item.children))
                        ).appendTo("table#groupsTable");
                    });
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
                    $.each(data,function(groups,digits){
                        contentDiv.append($("<table id='"+groups+"freqTable' class='table'>"));
                        $.each(digits,function(digit,freqs){
                            $("<tr>").append(
                                $("<td>").text(digit),
                                $("<td>").text(freqs)
                            ).appendTo("table#"+groups+"freqTable");
                        });
                    });
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
                    accountingGroups();
                    accountingFrequencies(0);
                },
                function(){});
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

var statRows = function(tableName, stat, name) {
    $.each(stat,function(i,item){
        $("<tr>").append(
            $("<td>").text(name),
            $("<td>").text(item.alpha),
            $("<td>").text(item.li),
            $("<td>").text(item.ui),
            $("<td>").text(item.lower),
            $("<td>").text(item.upper),
            $("<td>").text(item.t0)
        ).appendTo("table#"+tableName);
    });
}

var processCIsByGroup = function(data, intervalId){
    $(function(){
        clearInterval(intervalId);
        $("button#btnProgress").click();
        sampleCIData = data;
        var contentDiv=$("div#results");
        contentDiv.html("");
        $.each(data[0].CIs,function(key ,val){
            if (key != "r") {
                contentDiv.append(
                    $("<table id='"+data[0].id+"CIsTable' class='table'>").append(
                        $("<tr>").append(
                            $("<td>").text("Group ID"),
                            $("<td>").text(data[0].id),
                            $("<td>").text("Level"),
                            $("<td>").text(data[0].level),
                            $("<td>").text("#"+val.n)
                        )
                    )
                );
                contentDiv.append($("<table id='"+data[0].id+"_"+key+"CIsTable' class='table'>"));
                $("<thead>").append(
                    $("<tr>").append(
                        $("<th>").text(key),
                        $("<th>").text("Alpha"),
                        $("<th>").text("Lower Index"),
                        $("<th>").text("Upper Index"),
                        $("<th>").text("Lower Bound"),
                        $("<th>").text("Upper Bound"),
                        $("<th>").text("Statistic")
                    )
                ).appendTo("table#"+data[0].id+"_"+key+"CIsTable");
                statRows(data[0].id+"_"+key+"CIsTable", val.mean, "Mean");
                statRows(data[0].id+"_"+key+"CIsTable", val.variance, "Variance");
                statRows(data[0].id+"_"+key+"CIsTable", val.skewness, "Skewness");
                statRows(data[0].id+"_"+key+"CIsTable", val.kurtosis, "Kurtosis");
            } else if (val.n >= 1000) {
                contentDiv.append(
                    $("<table id='"+data[0].id+"CIsTable' class='table'>").append(
                        $("<tr>").append(
                            $("<td>").text("Group ID"),
                            $("<td>").text(data[0].id),
                            $("<td>").text("Level"),
                            $("<td>").text(data[0].level),
                            $("<td>").text("#"+val.n)
                        )
                    )
                );
                contentDiv.append($("<table id='"+data[0].id+"_"+key+"CIsTable' class='table'>"));
                $("<thead>").append(
                    $("<tr>").append(
                        $("<th>").text(key),
                        $("<th>").text("Alpha"),
                        $("<th>").text("Lower Index"),
                        $("<th>").text("Upper Index"),
                        $("<th>").text("Lower Bound"),
                        $("<th>").text("Upper Bound"),
                        $("<th>").text("Statistic")
                    )
                ).appendTo("table#"+data[0].id+"_"+key+"CIsTable");
                statRows(data[0].id+"_"+key+"CIsTable", val.alpha0, "Alpha 0");
                statRows(data[0].id+"_"+key+"CIsTable", val.alpha1, "Alpha 1");
                statRows(data[0].id+"_"+key+"CIsTable", val.beta0, "Beta 0");
                statRows(data[0].id+"_"+key+"CIsTable", val.beta1, "Beta 1");
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
        $("div#loadProgress").css('width', (100*e.loaded/e.total)+'%').attr('aria-valuenow', (100*e.loaded/e.total));
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