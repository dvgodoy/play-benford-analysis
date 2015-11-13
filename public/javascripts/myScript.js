prob = Array.apply(null, {length: 9}).map(Function.call, function(x){return Math.round((Math.log(x + 2) - Math.log(x + 1))*Math.LOG10E*10000)/10000});

benf = {
	"title": "Benford",
		"probs": prob.map(function(p, i){return {"prob": p}})

};

starter = d3.select('body').select('div.starter-template');

mySVG = d3.select('svg');

names = mySVG.selectAll('text')
		.data(benf.probs)
		.enter()
		.append('rect')
		.attr('name',function(d, i){return i})
		.attr('x',function(d, i){return i * 35})
		.attr('y',function(d){return 200 - d.prob * 1000})
		.attr('width',30)
		.attr('height',function(d){return d.prob * 1000})
		.on('mouseover',function(){
		    d3.select(this).style('fill','purple');
		})
		.on('mouseout',function(){
		  d3.select(this).style('fill','black');
		});