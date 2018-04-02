//Global Variables

var wordPerTopic = 10;
var labelPerTopic = 3;
var nbtopdocs = 10;
var topdocs_arr= [];
var toptnwords= [];
//var npmi = [];
var umass = [];
var uci = [];
var dbt = [];
//var npmiN = [];
var umassN = [];
var uciN = [];
var dbtN  = [];

//Init some rendering

$("#chooseword").hide();
$("#topiclabels").hide();
$("#tsnelabel").hide();
$("#loader").hide();

/* 
 * value accessor - returns the value to encode for a given data object.
 * scale - maps value to a visual display encoding, such as a pixel position.
 * map function - maps from data value to display value
 * axis - sets up axis
 */ 

$('#myslider').slider({
	formatter: function(value) {
		return 'correlated topics: ' + value;
	}
});

$('#wordpertopic').slider({	
	formatter: function(value) {
		return 'num. words: ' + value;
	},
	tooltip_position:'bottom'
});

//load data

function topicClick(topicClic) {
	$('#topiclist')
	.val(topicClic.id)
	.trigger("change");
	//alert("Topic clicked "+topicClic.id);
}

function change_topic_id(topic) {
	$('#topiclist')
	.val(topic)
	.trigger("change");
}

function docClick(topicClic) {
	alert("Doc clicked "+topicClic.id);
}

/* ------- Utils functions --------*/
function rainbow(numOfSteps, step) {
	// This function generates vibrant, "evenly spaced" colours (i.e. no clustering). This is ideal for creating easily distinguishable vibrant markers in Google Maps and other apps.
	// Adam Cole, 2011-Sept-14
	// HSV to RBG adapted from: http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript
	var r, g, b;
	var h = step / numOfSteps;
	var i = ~~(h * 6);
	var f = h * 6 - i;
	var q = 1 - f;
	switch(i % 6){
	case 0: r = 1; g = f; b = 0; break;
	case 1: r = q; g = 1; b = 0; break;
	case 2: r = 0; g = 1; b = f; break;
	case 3: r = 0; g = q; b = 1; break;
	case 4: r = f; g = 0; b = 1; break;
	case 5: r = 1; g = 0; b = q; break;
	}
	var c = "#" + ("00" + (~ ~(r * 255)).toString(16)).slice(-2) + ("00" + (~ ~(g * 255)).toString(16)).slice(-2) + ("00" + (~ ~(b * 255)).toString(16)).slice(-2);
	return (c);
}

//This function return a color for a vazlue between 0 and 1. The range is for red to green.

function perc2color(a) {
	var perc = a * 100;
	var r, g, b = 0;
	if(perc < 50) {
		r = 255;
		g = Math.round(5.1 * perc);
	}
	else {
		g = 255;
		r = Math.round(510 - 5.10 * perc);
	}
	var h = r * 0x10000 + g * 0x100 + b * 0x1;
	return '#' + ('000000' + h.toString(16)).slice(-6);
}
function setCookie(cname, cvalue) {
	var d = new Date();
	d.setTime(d.getTime() + (365*24*60*60*1000));
	var expires = "expires="+ d.toUTCString();
	document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}
function getCookie(cname) {
	var name = cname + "=";
	var decodedCookie = decodeURIComponent(document.cookie);
	var ca = decodedCookie.split(';');
	for(var i = 0; i <ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0) == ' ') {
			c = c.substring(1);
		}
		if (c.indexOf(name) == 0) {
			return c.substring(name.length, c.length);
		}
	}
	return "";
}
function checkCookie(cname) {
	var cookieValue = getCookie(cname);
	if (cookieValue != "") {
		return(cookieValue);
	} else {
		return("Select a label");
	}
}

function splitURL(url) {
	var pathArray = url.split( '/' );
	var newPathname = "";
	for (i = 0; i < (pathArray.length-1); i++) {
		if (i>0) newPathname += "/";
		if (typeof pathArray[i] === 'undefined' || !pathArray[i])
			newPathname += "";
		else
			newPathname += pathArray[i];
	}
	return newPathname;
}

/* ------- Math Utils functions --------*/

function standardDeviation(values){
	var avg = average(values);

	var squareDiffs = values.map(function(value){
		var diff = value - avg;
		var sqrDiff = diff * diff;
		return sqrDiff;
	});

	var avgSquareDiff = average(squareDiffs);

	var stdDev = Math.sqrt(avgSquareDiff);
	return stdDev;
}

function average(data){
	var sum = 0;
	var len = data.length;
	for (var i = 0; i < len; i++) {
		sum += data[i];	
	}
	return sum/len;
}

function normalize(bob){
	var avg = average(bob);
	var sd = standardDeviation(bob);
	for (var i = 0; i < len; i++) {
		bob[i] = ((bob[i] - avg) / sd);
	}

	var max = Math.max.apply(Math, bob);
	var min = Math.min.apply(Math,bob);
	var ec = max - min;
	var len = bob.length;
	for (var i = 0; i < len; i++) {
		bob[i] = ((bob[i] - min) / ec);
	}
	return bob;
}

//ready function

$(document).ready(function() {

	$("#loader").hide();
	$("#chooseword").hide();
	$("#topiclabels").hide();
	$("#tsne").hide();

	//init_top_topics();	

	jQuery.ajaxSetup({
		async : false
	});
	updateDatasets();
	jQuery.ajaxSetup({
		async : true
	});
	jQuery.ajaxSetup({
		async : false
	});
	updateTopics();
	jQuery.ajaxSetup({
		async : true
	});
	jQuery.ajaxSetup({
		async : false
	});
	getWords();
	jQuery.ajaxSetup({
		async : true
	});
	updateUsers();	
	read_hiddenTopic_file();
	var first_topic_to_print = get_first_nothidden_topic();	
	update_all_with_topic(first_topic_to_print);

	//updatetsne();

});

// interaction functions

function changeTitle(bob) {
	if(bob != "Select a label"){
		var tid = $("#topiclist").find(":selected").val();
		var dataset = $("#datasetlist").find(":selected").val();
		setCookie(dataset+","+tid,bob);
		$("#graphTitleLabel").html(bob);
		$("#tooltip_topic_" + tid).html(bob);
		console.log(activeNode);
		//reset_graph(tid);
	}
	$("#titleTop").html(bob);
}


function update_all_with_topic (topic) {
	$('#topiclist option[value=\"' + topic + '\"]').prop('selected', true);	
	change_topic_id(topic);
	printCurrentTopic(topic);
	//reset_graph(topic);	
}

$('#hidewords').on('click', function() {
	$("#wordselected").hide();
});


$('#datasetlist').on('change', function() {
	read_hiddenTopic_file();
	var first_topic_to_print = get_first_nothidden_topic();		
	change_topic_id(first_topic_to_print);
	$('#topiclist option[value=\"' + first_topic_to_print + '\"]').prop('selected', true);
	printCurrentTopic(first_topic_to_print);
	select_dataset();
	reset_graph(first_topic_to_print);
	setdatasetDescription();	
});

$('#topiclist').on('change', function() {
	var tid = $("#topiclist").find(":selected").val();
	printCurrentTopic(tid);
	getWords();
	getTopDocs();
	$("#topiclabels").hide();
	getLabels();
	reset_graph(tid);
	//updatetsne();
});

$('#wordpertopic').on('change', function() {
	getWords();
});


function setCurrentDataset() {
	var dataset = $("#datasetlist").find(":selected").val();
	document.cookie = "dataset="+dataset;	
}

function select_dataset() {
	setCurrentDataset();
	getWords();
	$("#topiclabels").hide();
	getLabels();
	updateTopics();
}

function updateDatasets() {
	jQuery.ajaxSetup({
		async : false
	});
	var current_dataset = getCookie("dataset");	
	$.get("./datasets/list", function(data) {
		var datasets = data.datasetsKeys;
		if (current_dataset == "")
			current_dataset = datasets[0];
		for (var i = 0; i < datasets.length; i++) {

			if (current_dataset == datasets[i]) {
				$('#datasetlist').append(
						$(
								'<option  selected onclick="select_dataset('
								+ datasets[i] + ')"></option>').text(
										datasets[i]));
			} else {
				$('#datasetlist').append(
						$(
								'<option onclick="select_dataset('
								+ datasets[i] + ')"></option>').text(
										datasets[i]));
			}
		}
	});
	getTopDocs();	
	setdatasetDescription();
}

function init_top_topics() {
	jQuery.ajaxSetup({
		async : false
	});
	$.get("./datasets/list", function(data) {
		var datasets = data.datasetsKeys;		
		for (var i = 0; i < datasets.length; i++) {
			$.get("/datasets/" + datasets[i] + "/init_toptopics", false);
		}
	});	
	jQuery.ajaxSetup({
		async : true
	});
}

function updateTopics() {
	jQuery.ajaxSetup({
		async : false
	});
	var dataset = $("#datasetlist").find(":selected").val();
	var req = "./datasets/" + dataset + "/topics/count";
	document.getElementById('topiclist').innerHTML = "";
	$.get(req, function(data) {
		var nbtopics = data.topicsCount;
		for (var i = 0; i < nbtopics; i++) {
			if (i == get_first_nothidden_topic()) { // && !$("#topiclist").val()) {
				$('#topiclist').append(
						$('<option selected onclick="getWords()"></option>')
						.text('Topic ' + i).val(i));
			} else {
				$('#topiclist').append(
						$('<option onclick="getWords()"></option>').text(
								'Topic ' + i).val(i));
			}
		}
	});
	getLabels();
	jQuery.ajaxSetup({
		async : true
	});
	jQuery.ajaxSetup({
		async : false
	});
	getCoherences();
	jQuery.ajaxSetup({
		async : true
	});
}
function ChangeCollapseBut(){
	inside =document.getElementById('colap').innerHTML;
	if(inside=="Learn more"){
		document.getElementById('colap').innerHTML = "Close about";
	}else{
		document.getElementById('colap').innerHTML = "About Readitopics";
	}
}
function GetCellValues() {
	var table = document.getElementById('labels');
	var user = document.getElementById('id_eval').value;
	var datasetid = document.getElementById('datasetlist').value;
	var tid = document.getElementById('topiclist').value;
	var r=0;
	for (var r = 0, n = table.rows.length-1; r < n; r++) {
		var Cell = table.rows[r].cells[0].innerText;
		var note = document.getElementById('labelgrade_'+ Cell).value;
		var evaluation = document.getElementById('labeleval_'+ Cell).value;
		write_LRtoserv(user,datasetid,tid,Cell,note,evaluation); 
	}
	//"/Grades/:datasetid/:tid/:meth/:note/:eval"
}
function write_LRtoserv(user,datasetid,tid,meth,note,evaluation) {
	var bool;
	var req ="\\user\\"+user+"\\"+datasetid+"\\"+tid+"\\"+meth+"\\"+note+"\\"+evaluation;
	$.get(req,bool);
}
function getLabelerDescription(labeler) {
	var aff;
	var req ="./Labeler/"+labeler;
	jQuery.ajaxSetup({
		async : false
	});

	var res = $.get(req,function(data) {
		res = data;
	});

	jQuery.ajaxSetup({
		async : true
	});

	return res.responseText;
}
function setdatasetDescription() {
	var tid = $("#datasetlist").find(":selected").val();
	var req ="./datasets/"+tid+"/des"
	jQuery.ajaxSetup({
		async : false
	});

	var res = $.get(req,function(data) {
		res = data;
	});

	jQuery.ajaxSetup({
		async : true
	});
	//alert(res.responseText);
	//return res.responseText;
	$("#descriptiondatas").html(res.responseText);
	$("#titledatas").html(tid+" : ");

}

// give a specific order to the printed labels
// (+ extra information whether the label must be added to the suggested list on the left
function getNumLabeler(l) {
	var sorted_labels = [ "zero_order_1", "c-order_t", "one_order", "sentence_based_COS0" ];
	for (i=0; i<sorted_labels.length; i++) {
		if (l == sorted_labels[i])
			return (i);	
	}
	return -1;
}
function isAddedToList(i) {
	var printed_or_not = [ true, true, true, false ];
	return (printed_or_not[i]); 	
}
function getNameLabeler(i) {
	var names = [ "0-order labels",  "T-order labels", "1-order labels", "illustrative sentences" ];
	return (names[i]); 	
}
// the above needs refactoring!!! probably using object-oriented js
//

// keep only unique values from labeler table (from https://stackoverflow.com/questions/9229645/remove-duplicate-values-from-js-array/9229821)
function uniq(a) {
    var seen = {};
    return a.filter(function(item) {
        return seen.hasOwnProperty(item) ? false : (seen[item] = true);
    });
}

function getLabels() {
	var tid = $("#topiclist").find(":selected").val();

	// added by julien in case of no topic selected
	if (typeof tid === 'undefined' || !tid)
	{ tid = get_first_nothidden_topic(); }

	$("#topiclabelid").html(tid);
	var dataset = $("#datasetlist").find(":selected").val();

	//get user chosen labels
	var usrLabel = checkCookie(dataset+","+tid);
	changeTitle(usrLabel);	

	var topic = tid

	$("#topitwlid").html(tid+" of dataset "+dataset);
	$("#topic_selected").html(tid);

	var req = "./datasets/" + dataset + "/topics/" + topic + "/labels/list/"
	+ labelPerTopic;


	$("#labels").html();
	$
	.get(
			req,
			function(data) {

				var labelshtml = "<tr><td>Labeler</td><td>Suggested labels</td></tr>";
				var labels_html_each = [];
				var list_label_tobesorted = [];
				var listLabel = "";				
				//Object.keys(data.labels)
				var labelers = Object.keys(data.labels);
				labelers = labelers.sort();
				labelers.forEach(
						function(key) {
							//var strat = key;
							var valueslabels ="" ;
							var num_labeler = getNumLabeler(key);
							var name_labeler = getNameLabeler(num_labeler);
							labels_html_each[num_labeler] = "<tr><td class=\"col-md-1\"><span class=\"label label-default \" data-toggle=\"tooltip\" data-placement=\"up\" data-container=\"body\" title=\""+getLabelerDescription(key)+"\">"
							+ name_labeler + "</span></td>\n";
							var labels = data.labels[key];
							for (i = 0; i < labels.length; i++) {
								if (isAddedToList(num_labeler)) {
									//listLabel +='<li><a href="#" onclick="changeTitle(' + "'" + labels[i].label + "'" +')">'+ labels[i].label +'</a></li>';
									list_label_tobesorted.push(labels[i].label);
								}
								if (i > 0) {
									if (isAddedToList(num_labeler))
										valueslabels += ", ";
									else
										valueslabels += "</td><tr><tr><td></td><td>";
								}
								+ '</a>'
								valueslabels +='<span class="colored_title" id=ng"'+key+i+'" draggable="true" ondragstart="drag(event)">' + labels[i].label
								+ '</span>';
							}
							//labelshtml += "<td class=col-md-3>"
							labels_html_each[num_labeler] += "<td class=col-md-3>"
								+ valueslabels
								+"</td>\n</tr>";
						});

				for (var i=0; i<labels_html_each.length; i++) {
					if (labels_html_each[i] != null)
					labelshtml += labels_html_each[i];
				}
				
				var sorting_list = uniq(list_label_tobesorted).sort();
				for (var i=0; i<sorting_list.length; i++) {
					listLabel +='<li><a href="#" onclick="changeTitle(' + "'" + sorting_list[i] + "'" +')">'+ sorting_list[i] +'</a></li>';
				}				
				listLabel+= '<li role="separator" class="divider"></li> <li><a href="#" data-toggle="modal" data-target="#myModal2"><b>Choose your own label</b></a></li>';

				$("#listLabelb").html(listLabel);
				$("#labels").html(labelshtml);

				// var wordlist="";
				// for (var i = 0; i < words.length; i++) {
				// wordlist = wordlist +'<li role="presentation"
				// class="li-word'+i+'"><a href="#"
				// onclick="updateWords()">'+words[i].mot+'</a></li>'
				// $("#chooseword").show();
				// }
				// $(".ul-word").html(wordlist);
				// $("#topiclabels").show();

				// <h5><span class="label label-default">z0</span>
				// president of the USA, USA elections, swing states,
				// Hilary Clienton's emails</h5>
				$("#topiclabels").show();
				$('[data-toggle="tooltip"]').tooltip({
					html: true,
					container: 'body'
				});
			});
}

/**
 * function updateWords() {
 * 
 * $(".ul-word").html("");$("#topwordsn").html(""); $("#chooseword").hide();
 * 
 * var tid = $("#topiclist").find(":selected").val(); if (parseInt(tid) >= 0) {
 * $("#topiclabelid").html(tid); //getWords(tid); getLabels(); }
 * 
 * //$("#wordportlet").show(); }
 */
function getCoherence(){

	var topic = $("#topiclist").find(":selected").val();
	//$("#npmi").html(npmi[topic].toFixed(2));
	$("#umass").html(umass[topic].toFixed(2));
	$("#uci").html(uci[topic].toFixed(2));
	$("#dbt").html(dbt[topic].toFixed(2));
	//document.getElementById("npmi").style.color = perc2color(npmiN[topic]);
	document.getElementById("umass").style.color = perc2color(umassN[topic]);
	document.getElementById("uci").style.color = perc2color(uciN[topic]);
	document.getElementById("dbt").style.color = perc2color(dbtN[topic]);
}

function getCoherences(){
	var i = 0;
	var dataset = $("#datasetlist").find(":selected").val();
	jQuery.ajaxSetup({
		async : false
	});

	$("#topiclist > option").each(function() {
		var id = this.value;
		var req = "./datasets/" + dataset + "/topics/" + id + "/coherence";
		$.get(req, function(data) {
			//npmi[i] = data.coher.NPMI;
			umass[i] = data.coher.UMASS;
			uci[i] = data.coher.UCI;
			dbt[i] = data.coher.DBT;
			i++;
		});
	});
	jQuery.ajaxSetup({
		async : true
	});
	/*var npmiC = npmi.slice();
	npmiN = normalize(npmi).slice();
	npmi = npmiC*/
	var umassC = umass.slice();
	umassN = normalize(umass).slice();
	umass = umassC
	var uciC = uci.slice();
	uciN = normalize(uci).slice();
	uci = uciC
	var dbtC = dbt.slice();
	dbtN = normalize(dbt).slice();
	dbt = dbtC;
}

function getWords(IDtopic) {
	toptnwords=[];
	wordPerTopic = $("#wordpertopic").val();
	var dataset = $("#datasetlist").find(":selected").val();
	var topic = $("#topiclist").find(":selected").val();
	getCoherence();
	// added by julien in case of no topic selected
	//
	if (typeof topic === 'undefined' || !topic)
	{ topic = get_first_nothidden_topic(); }
	//

	var req = "./datasets/" + dataset + "/topics/" + topic + "/words/list/"
	+ wordPerTopic;

	$("#topwordsn").html();
	$.get(req, function(data) {

		words = data.wordsList;

		var wordlist = "";
		var wordlist2 = "";
		var pond = 0;
		for (var i = 0; i < words.length; i++) {
			//pond = pond + (words[i].proba * words[i].proba);
			//pond = pond + words[i].proba
			if(words[i].proba>pond){
				pond=words[i].proba;
			}
		}
		for (var i = 0; i < words.length; i++) {
			//proba =(words[i].proba * words[i].proba)/ pond;
			proba =words[i].proba/pond;
			var tw = { mot:   words[i].mot, prob: proba };
			toptnwords.push(tw);	

			wordlist2 = wordlist2 + '<tr>\n<td class=\"col-md-1\">'
			//+'<span><a href="#" onClick="getTopDocsftooltip(this.innerHTML);return false;">' + words[i].mot
			+'<span class="colored_title" >' + words[i].mot
			//+'<span><a href="#" onClick="getTopDocsftooltip(this.innerHTML);return false;" data-toggle=\"tooltip\" data-placement=\"right\" data-container="body"  title=\"<h4>Top-docs for '+words[i].mot+'</h4><br /> ' + getTopDocs(words[i].mot) + '\">' + words[i].mot
			//+ '</a></span></td>'
			+ '</span></td>'
			+'<td><div class="progress" style="width:100%;margin:0px 0px 0px 0px;"><div class="progress-bar" role="progressbar" aria-valuenow="'+proba*100+'" aria-valuemin="0" aria-valuemax="100" style="width:'+proba*100+'%" data-toggle=\"tooltip\" data-placement=\"right\" title=\"<h5>Probability: '+words[i].proba.toFixed(4)+'</h5>\"></div></div></td>'		
			+'</tr>';		
			$("#chooseword").show();
		}		

		$("#topwordsn").html(wordlist2);
		//$(".ul-word").html(wordlist);
		//$("#topiclabels").show();
		$("#topwordsn").show();

		//$('[data-toggle="popover"]').popover();

		$('[data-toggle="tooltip"]').tooltip({
			html: true,
			container: 'body'
		});

		/*$('[data-toggle="tooltip"]').on('shown.bs.tooltip',function(e){
		    var $link =$(this);

		    var winW=$(window).width();    
		    var poL=$link.offset().left+$link.width();        
		    var newW=winW-poL-60;        

		    var poId=$link.attr('aria-describedby');
		    var $po=$('#'+poId);


		    //$(".popover").css({"max-width": "none;"});

		    $po.css({"max-width": "none;"});

		   //$po.find('.popover-content').width(newW);
		   //$po.find('.popover-title').width(newW);
		   //this.width(newW);
		  // this.data("bs.popover").tip().css({"max-width": "3000px"});

		});*/
	});	
}

function getTopDocs(word) {

	$("#wordselected").html("");
	$("#wordselected").val(word);
	$("#wordselected").html(word);

	var dataset = $("#datasetlist").find(":selected").val();
	var topic = $("#topiclist").find(":selected").val();

	// added by julien in case of no topic selected
	if (typeof topic === 'undefined' || !topic)
	{ topic = get_first_nothidden_topic(); }

	var req = "./datasets/" + dataset + "/topics/" + topic + "/words/" + word
	+ "/topdocs/list";
	//alert(req);
	var doclist = "";

	jQuery.ajaxSetup({
		async : false
	});
	var content = "";
	$.get(req, function(data) {

		docs = data.docs;

		if (docs.length <= 0) {
			content = "This word is not occurring in one of the top docs.";
		} else {

			var nb = docs.length;
			if (nb > labelPerTopic) {
				nb = labelPerTopic
			}

			for (var i = 0; i < nb; i++) {
				if (i == 0) {
					doclist += "";
				}	

				var txt = docs[i].text;
				var regex = new RegExp("(" + word + "(?!\\w))", 'gi');
				txt = txt.replace(regex, "<mark>$1</mark>");

				content += "<p>"+txt+"<br />";
				if (docs[i].author) {
					content += " - Written by " + docs[i].author; + "<br/>";
				}
				if (docs[i].name) {
					content += " (file: " + docs[i].name + ") <br />";
				}
				content += "</p>";

			}
		}
	});
	jQuery.ajaxSetup({
		async : true
	});

	return content;
}

function getTopDocs() {
	document.getElementById('container_td').innerHTML ="";
	document.getElementById('container_td').innerHTML +='<div id = "container_td" style="position: relative;"><span id="topcdocs3" style="position: relative;margin-bottom: 20px;"></span><div class="btn-toolbar" role="toolbar" aria-label="tbwbg"><div id ="list_td" class="btn-group btn-group-sm" role="group" aria-label="fg"></div></div><div id="demo3" class="collapse" style=" text-align: justify"><span id="docs3" style="max-height: 300px;display: block;overflow: auto;"></span></div> </span>';
	topdocs_arr = [];
	var datasetg = $("#datasetlist").find(":selected").val();
	var topicg = $("#topiclist").find(":selected").val();
	$("#topcdocs3").html('<h4>Top-'+nbtopdocs+' documents for topic ' + topicg + ' (click on it)</h4>');
	var req = "./datasets/" + datasetg + "/topics/" + topicg + "/topdocs/"+nbtopdocs;
	var docnames = "";
	$.get(req, function(data) {
		document.getElementById("list_td").innerHTML  = "";
		docs = data.docs;
		for (var i = 0; i < nbtopdocs; i++) {
			html_td="";
			if (i == 0) {
				docnames += "";
			}	
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
			docnames += docs[i].name;
			topdocs_arr.push(html_td); 
			document.getElementById("list_td").innerHTML += '<button type="button" id="colap" class="btn btn-default" onclick="showdoc(\'doc '+(i+1)+'\')">doc '+(i+1)+'</button>';
			if (i < (nbtopdocs-1)) 	{
				docnames += "*";
			}
		}
	});

	$("#container_td").show();
	$("#topcdocs3").show();
}
function showdoc(top){
	var i =(top.split(" ")[1].trim()-1);	
	var iddocn = "docnum_"+i;
	var tc = "null";
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
		var docn = document.getElementById(iddocn).childNodes[1].innerHTML;
		distrib_topics2(docn,i);	
		color_topics();
		$('#demo3').collapse("show");
	}
}
function getTopDocsftooltip(word) {
	document.getElementById('container_td').innerHTML ="";
	document.getElementById('container_td').innerHTML +='<span id="topcdocs3" style="position: relative;margin-bottom: 20px;">	</span>';
	document.getElementById('container_td').innerHTML +=' <button id="colap" class="btn btn-secondary" data-toggle="collapse" data-target="#demo3">Show</button>';
	document.getElementById('container_td').innerHTML +=' <div id="demo3" class="collapse" style=" text-align: justify"><span id="docs3" style="max-height: 300px;display: block;overflow: auto;">';
	document.getElementById('container_td').innerHTML +='</span></div>';
	$("#topcdocs3").html('<h4>@ containing \"'+word+'\"<a href="#" onClick="getTopDocs();return false;"> <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span></a></h4>');
	var datasetg = $("#datasetlist").find(":selected").val();
	var topicg = $("#topiclist").find(":selected").val();
	var req = "./datasets/" + datasetg + "/topics/" + topicg + "/words/" + word
	+ "/topdocs/list";

	jQuery.ajaxSetup({
		async : false
	});
	var content = "";
	var docnames = "";
	$.get(req, function(data) {

		docs = data.docs;

		if (docs.length <= 0) {
			content = "This word is not occuring in one of the top-docs.";
		} else {

			var nb = docs.length;
			if (nb > labelPerTopic) {
				nb = labelPerTopic
			}

			for (var i = 0; i < nb; i++) {
				if (i == 0) {
					content += "";
					docnames += "";
				}	

				if (docs[i].name) {
					content += "<br/>";
				} 

				var txt = docs[i].text;
				//var regex = new RegExp("(" + word + "(?!\\w))", 'gi');
				//txt = txt.replace(regex, "<mark>$1</mark>");
				var regex = new RegExp("\\b"+word+"\\b", 'gi');
				txt = txt.replace(regex, "<mark>"+word+"</mark>");
				content += "<p>"+txt+"<br />";
				if (docs[i].author) {
					content += " - Written by " + docs[i].author; + "<br/>";
				}
				docnames += docs[i].name;
				if (i < (nbtopdocs-1)) 	{
					docnames += "*";
				}				
				content += "</p>";
			}
		}
	});
	$("#docs3").html(content);
	$("#topcdocs3").show();
	$('#demo3').collapse("show");
	jQuery.ajaxSetup({
		async : true
	});
}
//nice tincel effect (think hard about how to improve it ;)
function color_topics() {
	$('span[id="topiccolor_0"]').css( 'background-color', 'blue');
	$('span[id="topiccolor_0"]').css( 'color', 'white');
	$('span[id="topiccolor_0"]').addClass('topic');
	$('span[id="topiccolor_1"]').css( 'background-color', 'red');
	$('span[id="topiccolor_1"]').css( 'color', 'white');
	$('span[id="topiccolor_1"]').addClass('topic');
	$('span[id="topiccolor_2"]').css( 'background-color', 'green');
	$('span[id="topiccolor_2"]').css( 'color', 'white');
	$('span[id="topiccolor_2"]').addClass('topic');
	$('span[id="topiccolor_3"]').css( 'background-color', 'yellow');
	$('span[id="topiccolor_3"]').css( 'color', 'white');
	$('span[id="topiccolor_3"]').addClass('topic');
	$('span[id="topiccolor_4"]').css( 'background-color', 'grey');
	$('span[id="topiccolor_4"]').css( 'color', 'white');
	$('span[id="topiccolor_4"]').addClass('topic');
}

//print the distribution for all docs
function distrib_topics(docnames) {
	var split = docnames.split("*");
	for (var i = 0; i < split.length; i++) {
		var docname = split[i];
		if (docname != "")
			print_distrib(docname, i);
	}
}
function distrib_topics2(docname,i) {
	var total = 200;	
	var top_topics = getTopTopics(docname);
	var p_total = 0;
	for (var j = 0; j < top_topics.length; j++)
		p_total += top_topics[j].proba;
	for (var j = 0; j < top_topics.length; j++)
	{
		var idd = "topicid_" + j + "_" + i;
		var w = (top_topics[j].proba * total) / p_total;
		$('span[id=' + idd + ']').css("display", "inline-block");
		$('span[id=' + idd + ']').css( 'width', w + "px");
		$('span[id=' + idd + ']').attr("title", top_topics[j].proba.toFixed(4)); 
		$('span[id=' + idd + ']').html(top_topics[j].num);	
		$("button[id=but_topicid_" + j + "_" + i + "]").attr("onclick","update_all_with_topic (" + top_topics[j].num + ");");
	}
}
//print the distribution for the doc num. i whose name is docname
function print_distrib(docname, i) {
	var total = 200;	
	var top_topics = getTopTopics(docname);
	var p_total = 0;
	for (var j = 0; j < top_topics.length; j++)
		p_total += top_topics[j].proba;
	for (var j = 0; j < top_topics.length; j++)
	{
		var idd = "topicid_" + j + "_" + i;
		var w = (top_topics[j].proba * total) / p_total;
		$('span[id=' + idd + ']').css("display", "inline-block");
		$('span[id=' + idd + ']').css( 'width', w + "px");
		$('span[id=' + idd + ']').attr("title", top_topics[j].proba.toFixed(4)); 
		$('span[id=' + idd + ']').html(top_topics[j].num);	
		$("button[id=but_topicid_" + j + "_" + i + "]").attr("onclick","update_all_with_topic (" + top_topics[j].num + ");");
	}
}

function getTopTopics(namedoc) {

	var dataset = $("#datasetlist").find(":selected").val();
	var topic = $("#topiclist").find(":selected").val();
	//var req = "./datasets/" + dataset + "/toptopics/" + namedoc;

	var req = "./datasets/" + dataset + "/fasttoptopics/" + namedoc;

	var top_topics = new Array();

	jQuery.ajaxSetup({
		async : false
	});

	$.get(req, function(docs) {

		//docs = data.topics;
		var nb = docs.length;

		for (var i = 0; i < nb; i++) {

			var num_topic = docs[i].num;
			var proba_topic = docs[i].proba;
			var tuple = new Object();
			tuple.num = num_topic;
			tuple.proba = proba_topic;

			top_topics[i] = tuple;
		}
	});

	jQuery.ajaxSetup({
		async : true
	});

	return top_topics;

}

function getcor() {

	var dataset = $("#datasetlist").find(":selected").val();
	var topic = $("#topiclist").find(":selected").val();
	var req = "./datasets/" + dataset + "/cor/" + topic;
	jQuery.ajaxSetup({
		async : false
	});

	$.get(req, function(data) {

		var cor = data.topics_cor;
		var nb = cor.length;

		for (var i = 0; i < nb; i++) {
			var num_topic = cor[i].index;
			var corel = cor[i].value;

		}
	});
	jQuery.ajaxSetup({
		async : true
	});
}
/*
function updatetsne() {

	$("#loader").show();
	$("#tsne").html("");
	$("#tsne").show();
	$("#tsnelabel").show();

	//http://localhost:4567/datasets/ASOIAF/tsne
	var dataset = $("#datasetlist").find(":selected").val();
	$.getJSON("./datasets/"+dataset+"/tsne", function(data) {   

		// get bounds of tsne div.
		var columnWidth = document.querySelector ('#tsne').getBoundingClientRect();

		
		var margin = {top: 20, right: 20, bottom: 30, left: 40},
		    width = 960 - margin.left - margin.right,
		    height = 500 - margin.top - margin.bottom;

		var margin = {top: 20, right: 40, bottom: 20, left: 40};
		var width = columnWidth.right - columnWidth.left - margin.right - margin.left;
		var height = width * 0.4;


		var topics = null;
		var nbtopics = null;
		var documents = null;
		var nbdocs = null;


		topics = data.topics;
		nbtopics = data.numtopics;
		documents = data.docs;
		nbdocs = data.numdocs;


		// setup x 
		var xValue = function(d) { return d.x;}, // data -> value
		xScale = d3.scaleLinear().range([0, width]), // value -> display
		xMap = function(d) { return xScale(xValue(d));}, // data -> display
		xAxis = d3.axisBottom(xScale);

		// setup y
		var yValue = function(d) { return d.y;}, // data -> value
		yScale = d3.scaleLinear().range([height, 0]), // value -> display
		yMap = function(d) { return yScale(yValue(d));}, // data -> display
		yAxis = d3.axisLeft(yScale);

		// setup fill color
		var cValued = function(d) { return +d.top;};
		var cValue = function(d) { return +d.id;},

		color = [];
		if (nbtopics == 10) {
			color = d3.schemeCategory10;
		} else if (nbtopics == 20) {
			color = d3.schemeCategory20;
		} else {
			for (i = 0; i < nbtopics; i++) { 
				color[i] = rainbow(nbtopics, i);
			}
		}
		//



		// add the graph canvas to the body of the webpage
		var svg = d3.select("#tsne").append("svg")
		.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom)
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
		var tooltip = d3.select("body").append("div")
		.attr("class", "tooltip")
		.style("opacity", 0);
		$("#tsne").css("width",width + margin.left + margin.right+ margin.left);
		$("#tsne").css("height",height + margin.top + margin.bottom + margin.top);

		// change string (from CSV) into number format
		data.topics.forEach(function(d) {
			d.x = +d.x;
			d.y = +d.y;

		});

		data.docs.forEach(function(d) {
			d.x = +d.x;
			d.y = +d.y;

		});

		// don't want dots overlapping axis, so add in buffer to data domain
		minX = Math.min(d3.min(data.topics, xValue)-1, d3.min(data.docs, xValue)-1);
		maxX = Math.max(d3.max(data.topics, xValue)+1, d3.max(data.docs, xValue)+1);

		minY = Math.min(d3.min(data.topics, yValue)-1, d3.min(data.docs, yValue)-1);
		maxY = Math.max(d3.max(data.topics, yValue)+1, d3.max(data.docs, yValue)+1);


		// xScale.domain([d3.min(data.topics, xValue)-1, d3.max(data.topics, xValue)+1]);
		// yScale.domain([d3.min(data.topics, yValue)-1, d3.max(data.topics, yValue)+1]);

		xScale.domain([minX, maxX]);
		yScale.domain([minY, maxY]);

		/* // x-axis
	  svg.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis)
	    .append("text")
	      .attr("class", "label")
	      .attr("x", width)
	      .attr("y", -6)
	      .style("text-anchor", "end")
	      .text("t-SNE(x)");

	  // y-axis
	  svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	    .append("text")
	      .attr("class", "label")
	      .attr("transform", "rotate(-90)")
	      .attr("y", 6)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text("t-SNE(y)");

		// draw documents
		svg.selectAll(".dot")
		.data(data.docs)
		.enter().append("circle")
		.attr("class", "dot")
		.attr("r", 2)
		.attr("cx", xMap)
		.attr("cy", yMap)
		.style("fill", function(d) { return color[cValued(d)];}) 
		.on("mouseover", function(d) {
			tooltip.transition()
			.duration(200)
			.style("opacity", .9);
			tooltip.html("documents "+d["id"] + "<br/> (" + xValue(d) 
					+ ", " + yValue(d) + ")")
					.style("left", (d3.event.pageX + 5) + "px")
					.style("top", (d3.event.pageY - 28) + "px");
		})
		.on("click", function(d) { docClick(d) })
		.on("mouseout", function(d) {
			tooltip.transition()
			.duration(500)
			.style("opacity", 0);
		});

		// draw topics
		svg.selectAll(".topiks")
		.data(data.topics)
		.enter().append("circle")
		.attr("class", "dot")
		.attr("r", 6)
		.attr("cx", xMap)
		.attr("cy", yMap)
		.style("fill", function(d) { return color[cValue(d)];}) 
		.on("mouseover", function(d) {
			tooltip.transition()
			.duration(200)
			.style("opacity", .9);
			tooltip.html("topic "+d["id"] + "<br/> (" + xValue(d) 
					+ ", " + yValue(d) + ")")
					.style("left", (d3.event.pageX + 5) + "px")
					.style("top", (d3.event.pageY - 28) + "px");
		})
		.on("click", function(d) { topicClick(d) })
		.on("mouseout", function(d) {
			tooltip.transition()
			.duration(500)
			.style("opacity", 0);
		});


		if (nbtopics <= 12) {
			// draw legend
			var legend = svg.selectAll(".legend")
			//.data(d3.scaleLinear().domain([0, nbtopics - 1]))
			.data(topics)
			.enter().append("g")
			.attr("class", "legend")
			.attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

			// draw legend colored rectangles
			legend.append("rect")
			.attr("x", width - 18)
			.attr("width", 18)
			.attr("height", 18)
			//.style("fill", color);
			.style("fill", function(d) { return color[+d.id];} );

			// draw legend text
			legend.append("text")
			.attr("x", width - 24)
			.attr("y", 9)
			.attr("dy", ".35em")
			.style("text-anchor", "end")
			.text(function(d) { return "topic "+d.id;})
		}

		$("#loader").hide();
	});

}
*/
function updateUsers() {
	jQuery.ajaxSetup({
		async : false
	});
	$.get("./users/list", function(data) {

		var users = data.split("\r");
		for (var i = 0; i < users.length; i++) {
			if (i == 0) {
				$('#user').append(
						/*$(
								'<option  selected onclick="select_dataset('
										+ datasets[i] + ')"></option>').text(
								datasets[i]));*/
						$(
						'<option  selected></option>').text(
								users[i].split(";")[0]));
			} else {
				$('#user').append(
						$(
						'<option></option>').text(
								users[i].split(";")[0]));
			}
		}
	});
	jQuery.ajaxSetup({
		async : true
	});
}

/*
function LaunchEvaluation() {

	var user = $("#user").find(":selected").val();
	var pass = $("#pwd").val();
	jQuery.ajaxSetup({
		async : false
	});	
	$.get("./users/list", function(data) {
		var users = data.split("\r");
		for (var i = 0; i < users.length; i++) {
			if (users[i].split(";")[0] == user) {
				if(users[i].split(";")[1]==pass){
					localStorage.setItem("user", user);
					var current_path = splitURL(document.location.pathname);					
					window.location.pathname = current_path + '/evaluation.html';
				}else{
					alert("wrong password !!!! ");
				}
			}
		}
	});
	jQuery.ajaxSetup({
		async : true
	});
}
*/

//DND
//https://www.w3schools.com/html/html5_draganddrop.asp
function allowDrop(ev) {
	ev.preventDefault();
}

function drag(ev) {
	ev.dataTransfer.setData("text/html", ev.target.id);
}

function drop(ev) {	
	jQuery.ajaxSetup({
		async : false
	});
	ev.preventDefault();
	var data=ev.dataTransfer.getData("text/html");
	var nodeCopy = document.getElementById(data).cloneNode(true);
	/*console.log("transfert " + nodeCopy.textContent + " de " + current_topic);
	nodeCopy.id = "Topic_Title";
	nodeCopy.style.color = "white";
	ev.target.innerHTML = "";
	ev.target.appendChild(nodeCopy);*/
	changeTitle(nodeCopy.textContent);
	jQuery.ajaxSetup({
		async : true
	});
}
