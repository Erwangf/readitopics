var width_box = 70;
var height_box = 30;
var origine_x = 50;
var origine_y = 30;
var offset_x = 260;
var offset_y = 60;
var max_width_line = 20;
var size_middle_node = 7;
var t1;
var t2;

var histo;
var current;
var next;
var history_current;
var current_next;
var activeNode;

var max_correlated_topics = 10;
var max_histo = 3;
var max_current = 3;
var max_next = 3;

var hidden_topics = new Array();

var last_node_clicked = null;

var current_topic = 0;

 /*var svg2 = d3.select("#graphviz")
  .append("svg")
  .attr("width", origine_x + offset_x*2 + width_box*2)
  .attr("height", origine_y + offset_y*max_next);
*/  
var svg2 = d3.select("#graphviz")
.append("svg")
.attr("width",origine_x + offset_x*2 + width_box*2)
.attr("height",origine_y + offset_y*max_next);
var prev_activeTopic; 
var activeNode; 
var activeTopic;


/****** FUNCTIONS *******/

// init an array of dimension dim with value val
function init_arrays(dim, val) {
	var a = new Array();
	for (var i=0; i<dim; i++)
		a.push(val);
	return a;
}

function cleanGraph() {
	svg2.selectAll("*").remove();
}

$('#myslider').on('change', function() {
	max_next = $('#myslider').val();	
	d3.select("#graphviz")
	  .select("svg")
	  .attr("height", origine_y + offset_y*max_nb_rows());
	updateGraph_nextonly();
});

// compute the correlated topics of topic in dataset
// ignoring the indexes in hidden_topics
function compute_correlated_topics(dataset, topic) {
	current_next = [];
	jQuery.ajaxSetup({
		async : false
	});
	$.get("./datasets/" + dataset + "/cor/" + topic, function(data) {
		var nbfound = 0;
		var i = 1;
		var c = data.topics_cor;
		while ((nbfound < max_correlated_topics) && (i < c.length)) {
			var tc = c[i].index;
			if (hidden_topics.indexOf(tc) == -1) { 
				next[nbfound] = c[i].index;
				current_next[nbfound] = c[i].value;
				nbfound++;
			}
			i++;
		}
	});		
	//normalization of cor score	
	var sum_cor = 0;
	var min_cor = current_next[max_correlated_topics-1];
	for (var i=0; i<max_correlated_topics; i++) {
		current_next[i] -= (min_cor/2);
		sum_cor += current_next[i];		
	}	
	for (var i=0; i<max_correlated_topics; i++) {
		current_next[i] = (current_next[i] * max_width_line) / sum_cor; 
	}

	jQuery.ajaxSetup({
		async : true
	});
}

function draw_lines(list, col, max_row) {	
	for (var i=0; i<list.length; i++)
		if ((list[i] != -1) && (i<max_row))		
			add_line(col, i, list[i]);
}

function add_line(col, i, weight) {
	var line = svg2	
	  .append("line")
	  .attr("class", "link " + col)
	  .attr("x1", function() {
		  if (col == 1)
			  return origine_x + (width_box/2);
		  else
			  return origine_x + (width_box/2) + offset_x;
	  })
	  .attr("y1", function() {
		  if (col == 1)
			  return origine_y + (height_box/2) + (prev_activeTopic*offset_y);
		  else
			  return origine_y + (height_box/2) + (activeTopic*offset_y);		   
	  })
	  .attr("x2", function() {
		  if (col == 1)
			  return origine_x + (width_box/2) + offset_x;
		  else
			  return origine_x + (width_box/2) +  (2*offset_x); 
	  })
	  .attr("y2", function() {
		  if (col == 1)
			  return origine_y + (height_box/2) + (i*offset_y);			  
		  else
			  return origine_y + (height_box/2) + (i*offset_y);
	  })
	  .attr("fill", "none")
	  .attr("stroke", "black")
	  .attr("stroke-width", function() {
		  return weight;
	  }
	  );
	
	var group = svg2.append("g")
	  .attr("class", "middle " + col)
	  .attr('transform', function() {
		  if (col == 1) {
			  x = ((origine_x + (width_box/2) + origine_x + (width_box/2) + offset_x) / 2);
			  y = ((origine_y + (height_box/2) + (prev_activeTopic*offset_y) + origine_y + (height_box/2) + (i*offset_y)) / 2);
		  }
		  else {
			  x = ((origine_x + (width_box/2) + offset_x + origine_x + (width_box/2) +  (2*offset_x)) / 2);
			  y = ((origine_y + (height_box/2) + (activeTopic*offset_y) + origine_y + (height_box/2) + (i*offset_y)) / 2);
		  }
		  return 'translate(' + x + ', ' + y +')';
	  });
	
	group.append("circle")
	.attr("cx", 0)
	.attr("cy", 0)
	.attr("r", size_middle_node)
	.attr("fill", "#4285BE")
	//.attr("attr", "selected_inter_node")
    .on('click', function() {
    	  var top1;
    	  var top2;
    	  if (col == 1) {
    		  top1 = histo[prev_activeTopic];
    		  top2 = current[i];
    	  }
    	  else {
    		  top1 = current[activeTopic];
    		  top2 = next[i];
    	  }
    	  if (last_node_clicked != null)
    		  last_node_clicked.classed("selected_inter_node", false);
    	  last_node_clicked = d3.select(this); 
    	  last_node_clicked.classed("selected_inter_node", true);
		 // show_intersect(top1, top2);
		  show_intersect2(top1, top2);
	   })
	   .style("cursor", "pointer")
	   .on("mouseover", function() {		   
		   d3.select(this)
		   .transition()
		   .duration(100)		
           .style("fill", "#F3A200");
	    })
	    .on("mouseout", function(d, i) {
	    	d3.select(this)
			   .transition()
			   .duration(100)		
	           .style("fill", function() {
	        	   var s = d3.select(this).classed("selected_inter_node");
	        	   if (!s)
	        		   return "#4285BE";
	           });
	    });

}

function color_topics() {
	var tip = $("#topiclist").find(":selected").val().trim();
	var tc = document.getElementById("selecteddoc").innerHTML.split(" ")[1].trim()-1;
	$('span[id="topiccolor_0"]').css( 'background-color', 'violet');
	$('span[id="topiccolor_0"]').css( 'color', 'white');
	$('span[id="topiccolor_0"]').addClass('topic');
	$('span[id="topiccolor_1"]').css( 'background-color', 'red');
	$('span[id="topiccolor_1"]').css( 'color', 'white');
	$('span[id="topiccolor_1"]').addClass('topic');
	$('span[id="topiccolor_2"]').css( 'background-color', 'green');
	$('span[id="topiccolor_2"]').css( 'color', 'white');
	$('span[id="topiccolor_2"]').addClass('topic');
	$('span[id="topiccolor_3"]').css( 'background-color', 'brown');
	$('span[id="topiccolor_3"]').css( 'color', 'white');
	$('span[id="topiccolor_3"]').addClass('topic');
	$('span[id="topiccolor_4"]').css( 'background-color', 'grey');
	$('span[id="topiccolor_4"]').css( 'color', 'white');
	$('span[id="topiccolor_4"]').addClass('topic');
	for (var tt = 0;tt<5;tt++){
		if (document.getElementById("topicid_"+tt+"_"+tc).innerHTML.trim() == tip){
		document.getElementById("topiccolor_"+tt).style.backgroundColor = 'rgba(255,255,0,1)';
		document.getElementById("topiccolor_"+tt).style.color = 'black';
		}	
		if (document.getElementById("topicid_"+tt+"_"+tc).innerHTML.trim() == t2){
		document.getElementById("topiccolor_"+tt).style.backgroundColor =  'blue';
		}	
	}
}

function show_intersect2(top1, top2) {
t1 = top1;
t2 = top2;
	document.getElementById('container_td').innerHTML ="";
	document.getElementById('container_td').innerHTML +='<div id = "container_td" style="position: relative;"><span id="topcdocs3" style="position: relative;margin-bottom: 20px;"></span><div class="btn-toolbar" role="toolbar" aria-label="tbwbg"><div id ="list_td" class="btn-group btn-group-sm" role="group" aria-label="fg"></div></div><div id="demo3" class="collapse" style=" text-align: justify"><span id="docs3" style="max-height: 300px;display: block;overflow: auto;"></span></div> </span>';
	topdocs_arr = [];
	var datasetg = $("#datasetlist").find(":selected").val();
	toptnwords2 = [];
	var req2 = "./datasets/" + datasetg + "/topics/" + top2 + "/words/list/"
			+ 10;
	$.get(req2, function(data) {
		words = data.wordsList;
		var pond = 0;
		for (var i = 0; i < words.length; i++) {
			if(words[i].proba>pond){
			pond=words[i].proba;
			}
		}
		for (var i = 0; i < words.length; i++) {
			proba =words[i].proba/pond;
			var tw = { mot:   words[i].mot, prob: proba };
			toptnwords2.push(tw);	
		}		
	});	
	$("#topcdocs3").html('<h4>Top-5 docs for topics '+top1+' and '+top2+'</h4>');
	var req = "./datasets/" + datasetg + "/topics/" + top1 + "/topics/" + top2 + "/topdocs/5";
	$.get(req, function(data) {
	document.getElementById("list_td").innerHTML  = "";
		docs = data.docs;
		for (var i = 0; i < 5; i++) {
		html_td="";
			var txt = docs[i].text;						
			if (docs[i].name) {
				html_td+=" </br>";
				var idd = "docnum_" + i;				
				html_td += "<span id=\"" + idd + "\"> doc <em>" + docs[i].name + "</em> </span>";
				for (var j = 0; j < 5; j++) {
					html_td += "<button id='but_topicid_" + j + "_" + i + "' class='transp' onclick='';><span id=\"topiccolor_" + j + "\" > <span id=\"topicid_" + j + "_" + i + "\">0</span></span></button>";
				} 
			}			
			html_td+="<blockquote class=\"quotetext\" style=\"margin-bottom : 0\"><p>" + txt + "</p>\n <footer id=\"selecteddoc\">doc "+(i+1)+"\n";
			if (docs[i].author) {
				html_td+= " - Written by " + docs[i].author;
			}			
			html_td+= "</footer>\n</blockquote>";
			for (var ki = 0;ki<10;ki++){
			var regex = new RegExp("\\b"+toptnwords[ki].mot+"\\b", 'gi');
			html_td = html_td.replace(regex, '<mark style="background-color: rgba(255,255,0,'+toptnwords[ki].prob+')">'+toptnwords[ki].mot+'</mark>');
			}
			for (var ki = 0;ki<10;ki++){
			var regex = new RegExp("\\b"+toptnwords2[ki].mot+"\\b", 'gi');
			html_td = html_td.replace(regex, '<mark style="background-color: rgba(66,139,201,'+toptnwords2[ki].prob+')">'+toptnwords2[ki].mot+'</mark>');
			}
			topdocs_arr.push(html_td); 
			document.getElementById("list_td").innerHTML += '<button type="button" id="colap" class="btn btn-default" onclick="showdoc2(\'doc '+(i+1)+'\')">doc '+(i+1)+'</button>';
		}
	});
	
	$("#container_td").show();
	$("#topcdocs3").show();
}
function showdoc2(top){
var tc = "null";
var i =(top.split(" ")[1].trim()-1);	
var iddocn = "docnum_"+i;
if(document.getElementById("selecteddoc") != null){
	tc = document.getElementById("selecteddoc").innerHTML.trim();
	if( tc == top.trim()){
		$("#docs3").html("");
		$('#demo3').collapse("hide");
	}else{
		$('#demo3').collapse();
		var doc_ec = topdocs_arr[top.split(" ")[1]-1];
		$("#docs3").html(doc_ec);
	var docn = document.getElementById(iddocn).childNodes[1].innerHTML;
	distrib_topics2(docn,i);	
				color_topics();	
	}
}else{
	var doc_ec = topdocs_arr[top.split(" ")[1]-1];
	$("#docs3").html(doc_ec);	
	$('#demo3').collapse("show");

	var docn = document.getElementById(iddocn).childNodes[1].innerHTML;
	distrib_topics2(docn,i);	
			color_topics();
}	
}





function buildGraph() {
	
	cleanGraph();
	
	var tid = current[activeTopic];
	
	// current dataset
	var dataset = $("#datasetlist").find(":selected").val();
	
	// get the top k correlated topics
	if (next[0] == -1)
		compute_correlated_topics(dataset, tid);

	// draw the lines
	if (histo[0] != -1)
		draw_lines(history_current, 1, max_current);
	
	if (next[0] != -1)
		draw_lines(current_next, 2, max_next);

	// draw the nodes
	draw_nodes(histo, 0, max_histo);
	draw_nodes(current, 1, max_current);
	draw_nodes(next, 2, max_next);
	//activeNode = "node_1_0"; 	
}

function updateGraph_nextonly() {
	
	//svg2.selectAll("link 2").remove();
	$(".link.2").remove();
	$(".node.2").remove();
	$(".middle.2").remove();
	
	if (next[0] != -1)
		draw_lines(current_next, 2, max_next);
	
	// draw the nodes
	draw_nodes(current, 1, max_current);
	draw_nodes(next, 2, max_next);	
}

// get the maximum number of rows
function max_nb_rows() {
	return Math.max(max_histo, max_current, max_next);
}

function updateGraph() {
	d3.selectAll(".link")
	.each(function(d) {			
		d3.select(this)
		.transition()
		.duration(300)
		.attr('transform', function(d) {
			var prev_t_x = 0;
			var prev_t_y = 0;
			var t = d3.select(this).attr("transform");
			if (t != null) {
				var values = t.match(/-?[\d\.]+/g);
				prev_t_x = values[0];
				prev_t_y = values[1];
			}			
			var posx = (prev_t_x - offset_x);
			var posy = prev_t_y;
			return 'translate(' + posx + ',' + posy + ')';
		});
	});
	d3.selectAll(".middle")
	.each(function(d) {			
		d3.select(this)
		.transition()
		.duration(300)
		.attr('transform', function(d) {
			var prev_t_x = 0;
			var prev_t_y = 0;
			var t = d3.select(this).attr("transform");
			if (t != null) {
				var values = t.match(/-?[\d\.]+/g);
				prev_t_x = values[0];
				prev_t_y = values[1];
			}			
			var posx = (prev_t_x - offset_x);
			var posy = prev_t_y;
			return 'translate(' + posx + ',' + posy + ')';
		});
	});	
	d3.selectAll(".node")
		.each(function(d) {			
			d3.select(this)
			.transition()
			.duration(300)
			.attr('transform', function(d) {
				var t = d3.select(this).attr("transform");
				var values = t.match(/-?[\d\.]+/g);
				var posx = (values[0] - offset_x);
				var posy = values[1];
				return 'translate(' + posx + ',' + posy + ')';
			});
		});
	d3.select("#graphviz")
	  .select("svg")
	  .transition()
	  .duration(300)
	  .attr("height", origine_y + offset_y*max_nb_rows())
	  .on("end", buildGraph);
}

var ttt = 0;

function next_button() {
	var col = activeNode.slice(5, 6);
	var row = activeNode.slice(7, 8);
	if (col == 2)	{ // next column
		prev_activeTopic = activeTopic;
		activeTopic = activeNode.slice(7, 8);
		newTopic = next[activeTopic];
		history_current = current_next.slice();
		max_histo = max_current;
		histo = current.slice();
		current = next.slice();
		max_current = max_next;
		max_next = $('#myslider').val();
		activeNode = "node_1_" + activeTopic; 		
		next = init_arrays(max_correlated_topics, -1);
		var dataset = $("#datasetlist").find(":selected").val();		
		compute_correlated_topics(dataset, newTopic);
		printCurrentTopic(newTopic);
		updateGraph();
	}
	else
	if (col == 1)	{ // current column
	//if (row != activeTopic) {
		var dataset = $("#datasetlist").find(":selected").val();
		newTopic = current[row];
		next = init_arrays(max_correlated_topics, -1);
		compute_correlated_topics(dataset, newTopic);
		activeTopic = row;
		buildGraph();		
	//}				
	}
}

function recompute_for_activeNode(update) {
	var col = activeNode.slice(5, 6);
	var row = activeNode.slice(7, 8);		
	if (col == 0) {
		printCurrentTopic(histo[row]);
		reset_graph(histo[row])
	}
	else {
		if (update || (row != activeTopic)) {
			printCurrentTopic(current[row]);
			reset_graph(current[row])
		}
	}
}

/*function backtrack() {
	if (old_activeTopic != -1) {
		setCurrentTopic(old_activeTopic);
		activeNode = "n0";
		updateGraph();
	}
}*/

/* get an estimation of topic size */
function get_topic_size(topic) {
	var dataset = $("#datasetlist").find(":selected").val();
	var req = "./datasets/" + dataset + "/topics/" + topic + "/size";
	jQuery.ajaxSetup({
		async : false
	});	
	var res = $.get(req,function(data) {
		res = data;
	});
	jQuery.ajaxSetup({
		async : true
	});
	return parseFloat(res.responseText).toFixed(1);
}

/* update topic title (1st row of viz) */
function printCurrentTopic(topic) {
	//var title = getktopwords(topic, 5);
	var dataset = $("#datasetlist").find(":selected").val();
	var title = checkCookie(dataset+","+topic);
	if(title === "Select a label"){
		title = getktopwords(topic, 5);
	}
	var s = "";
	s += "<h4>";
	s += "Current ";
	s += "<span class='label label-success topic-name'>topic " + topic + "</span>";
	s += "&nbsp;:&nbsp;";
	s += "<span class='colored_title' id='graphTitleLabel'>" + title + "</span> (#docs: " + get_topic_size(topic) +") ";
	s += "<button onclick='hide_topic("
		+ topic
		+ "); update_hidden_list_HTML(); write_hiddenTopic_file();' class='transp'>"
		+ "<span class=\"label label-default \" data-toggle=\"tooltip\" data-placement=\"up\" data-container=\"body\" title=\""
		+ "hide the current topic"
		+ "\">hide topic <span class='glyphicon glyphicon-zoom-out'></span></span>"
		+ "</button>";
	s += "</h4>";
	$('#title_topic').html(s);
	current_topic = topic;
}

/* total reset of the graph with topic seed */
function reset_graph(topic) {
	init_graph(max_correlated_topics, -1, topic);
	buildGraph();
}

/* update current topic, including the html select */
function updateTopicsWithSelected(idtopic) {
	$('#topiclist option[value=\"' + idtopic + '\"]')
		.prop('selected', true);
	printCurrentTopic(idtopic);	
	update_all();
}

function update_all() {
	$("#topwordsn").hide();
	$("#topcdocs").hide();
	$("#topiclabels").hide();	
	buildGraph();
	getWords();		
	getTopDocs();	
	getLabels();
}

function getktopwords(topic, k) {
	var dataset = $("#datasetlist").find(":selected").val();
	var req = "./datasets/" + dataset + "/topics/" + topic + "/words/list/"	+ k;
	//console.log("requete : " + req);
	var wordlist = "";
	jQuery.ajaxSetup({
		async : false
	});
	$.get(req, function(data) {	
		words = data.wordsList;			
		for (var i = 0; i < words.length; i++) {
			wordlist += words[i].mot;
			if (i < (words.length-1))
				wordlist += ", ";
			else
				wordlist += "...";
		}
	});
	jQuery.ajaxSetup({
		async : true
	});
	return wordlist;
}

function draw_nodes(list, col, max_row) {
	//console.log("max row = " + max_row);
	for (var i=0; i<list.length; i++)
		if ((list[i] != -1) && (i<max_row))		
			add_node(col, i, list[i]);
}

function add_node(col, row, topic) {
	 var group = svg2
	   .append("g")
	   .attr("class", "node " + col)
	   .attr("id", "group_"+col+"_"+row)
	   .attr('transform','translate(' + (origine_x + (col*offset_x)) + ', ' + (origine_y + (row*offset_y)) +')');
	 var rect = group
	   .append("rect")
	   .attr("id", "node_"+col+"_"+row)
	   .attr("width", width_box)
	   .attr("height", height_box)
	   .attr("rx", 10)
	   .attr("ry", 10)
	   .attr("stroke", "black")
	   .attr("stroke-width", 2)
	   .attr("fill", function() {
		 if (activeNode == ("node_"+col+"_"+row))
	    	 return "#F3A200";
	     else
	    	 return "#F3E7B6";
	   });
	 group
	   .append("text")
	   .attr("x", width_box/2)
	   .attr("y", height_box/2 + 3)
	   .text(topic)
	   .attr("text-anchor", "middle")
	   .on('click', function() {
		   activeNode = "node_"+col+"_"+row;
		   updateTopicsWithSelected(topic);
	   })
	   .style("cursor", "pointer")
	   .on("mouseover", function(d, i) {		   
		   svg2.select("#tooltip_topic_" + topic + "_" + col + "_" + row)
		   .transition()
		   .duration(100)		
           .style("opacity", .7);
	   })
	    .on("mouseout", function(d, i) {
	    	svg2.select("#tooltip_topic_" + topic + "_" + col + "_" + row)
			   .transition()
			   .duration(100)		
	           .style("opacity", 0);
	   });
	 
	 // tooltip
	 group
	   .append("text")
	   .attr("id", function(d, i) {
		   return "tooltip_topic_" + topic + "_" + col + "_" + row;
	   })
	   .attr("x", 10)
	   .attr("y", -3)
	   .attr("font-size", "12")
	   //.attr("dx", width_box)
	   .text(function(d, i) {
			var dataset = $("#datasetlist").find(":selected").val();
			var title = checkCookie(dataset+","+topic);
			if(title === "Select a label"){
				title = getktopwords(topic, 3);
			}
		   return title;
		   //return getktopwords(topic, 3);
	   })
	   .style("opacity", 0);
	   
	 d3.select("#group").style("opacity", 1);
	 /*group
	   .transition()
	   .duration(200)
	   .attr('transform','translate(30,200)');*/
	 	
}

function init_graph(nb_rows, val, topic) {
	histo = init_arrays(nb_rows, val)
	current = init_arrays(nb_rows, val);
	activeTopic = 0;
	activeNode = "node_1_0"; 
	current[0] = topic;
	next = init_arrays(nb_rows, val);
	history_current = new Array(nb_rows);
	current_next = new Array(nb_rows);
}

/* managing hiddent topics */

function read_hiddenTopic_file() {
	hidden_topics = [];
	var dataset = $("#datasetlist").find(":selected").val();
	var req = "./datasets/" + dataset + "/hiddentopics";
	jQuery.ajaxSetup({
		async : false
	});	
	$.get(req, function(data) {
		var t = data.split("\n");
		for (var i = 0; i < t.length; i++) {
			if (!(typeof t[i] === 'undefined' || !t[i]))
				hide_topic(parseInt(t[i]));
		}
	});
	jQuery.ajaxSetup({
		async : true
	});
}

function write_hiddenTopic_file() {
	var s = "";
	var dataset = $("#datasetlist").find(":selected").val();
	for (var i=0; i<hidden_topics.length; i++) {
		s += hidden_topics[i] + ";";
	}
	var req = "./datasets/" + dataset + "/hiddentopics/save/" + s;
	jQuery.ajaxSetup({
		async : false
	});	
	var bool;
	$.get(req, bool);
	jQuery.ajaxSetup({
		async : true
	});
}

function update_hidden_list_HTML() {
	var s = "";
	s += "<table>";
	for (var t=0; t<hidden_topics.length; t++) {
		s += "<tr><td>";
		s += "<button type='button' class='btn btn-default' onclick=show_topic("
			+ hidden_topics[t]
			+ ");write_hiddenTopic_file();''>"
			+ hidden_topics[t]
			+ " <span class='glyphicon glyphicon-zoom-in'></span>"
			+ "</button>";
		s += "</td></tr>";
	}
	s += "</table>";
	$("#hidden_topics_list").html(s);
}

function hide_topic(topic) {
	if (hidden_topics.indexOf(topic) == -1) {
		hidden_topics.push(topic);
		hidden_topics = hidden_topics.sort(function (a, b) {  return a - b;  });
		update_hidden_list_HTML();
	}
}

function show_topic(topic) {
	var index = hidden_topics.indexOf(topic);
	hidden_topics.splice(index, 1);
	update_hidden_list_HTML();
}

function get_first_nothidden_topic() {
	var i=0;
	while (hidden_topics.indexOf(i) != -1) i++;
	return i;
}

/*
div.transition()		
    .duration(200)		
    .style("opacity", .9);		
div.html("node " + i)	
    .style("left", (posx) + "px")
    .style("top", (posy) + "px");*/
