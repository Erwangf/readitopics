//CONFIG
var wordPerTopic = 100;
var labelPerTopic = 3;
var nbtopdocs = 5;
var nbtopdocspzd =3;
var datasetg="ASOIAF";
var topicg=0;
var bool=true;
var who = "null";
var nbeval;
var page_tuto = 1;
var topdocs_arr= [];
var	topdocs_arr_pzd = [];
var toptnwords= [];

//var title_cho;
 text_tuto= [];

// Init some rendering
//chooseword -> dataset desc and top words
$("#selected_ngrams").hide();
$("#topiclabels").hide();

$(document).ready(function() {
 text_tuto[0] = "Here, you have the whole interface." ; 
 text_tuto[1] = "You can find a description of the dataset on the lower left"; 
 text_tuto[2] = "Here you have the top words of the topic, meaning the most representatives words."; 
 text_tuto[3] = "And their importance for this topic"; 
 text_tuto[4] = "Then, you can take a look at the documents that maximise the probability p(d|z) by clicking on the buttons here"; 
 text_tuto[5] = "Similarly, you can click on candidates and top words to have the extracts of the top documents containing the selected term"; 
 text_tuto[6] = "Before you begin to evaluate, you are asked to choose a label based on what you understood" ;
 text_tuto[7] = "Then you just have to rate the candidates according to your opinion, first the n-grams" ;
 text_tuto[8] = "Then the sentences" ;
text_tuto[9] = "Eventually, you just need to say if you still think that the label you choosed first is a good label" ;
	page_tuto = 1;
	$('#mod_cont').modal('show'); 
	who = localStorage.getItem("user");
	if (who == null){
		alert("you are not allowed to go there");
		window.location.pathname = '/index.html';
	}	
	$("#name").html(who);
	getMaxeval(who);
	MAJ();
$("#card").flip({
  trigger: 'manual'
});
});
    
function MAJ(){
	$('html, body').animate({ scrollTop: 0 });
	$("#selected_ngrams").html("");
		document.getElementById("chooseword").style.color = "black";
		document.getElementById("selected_ngrams").style.color = "black";
    //On met à jour le dataset et le topic 
	jQuery.ajaxSetup({
		async : false
	});
    getStory(who);
    jQuery.ajaxSetup({
		async : true
	});
	jQuery.ajaxSetup({
		async : false
	});
    setdatasetDescription();
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
	jQuery.ajaxSetup({
		async : false
	});
   	$("#topiclabels").hide();
	$("#topcdocs").hide();
	getLabels();
    jQuery.ajaxSetup({
		async : true
	});
	jQuery.ajaxSetup({
		async : false
	});
	$("#chooseword").show();
	$("#topiclabels").show();
	getTopDocs();
	getTopDocspzd();
	$("#topcdocs").show();
	showdoc('doc 1');
	showtructitle();
}    


function getStory(user){
	jQuery.ajaxSetup({
		async : false
	});
	$.get("./users/"+user+"/story", function(data) {
	if(data!="done"){
		var datas = data.split(";");
		datasetg =datas[0] ;
		topicg=datas[1] ;
		var numb=eval(datas[2])+1;
		$('#number_cutop').html(numb);
		maj_breadc(numb);
	}else{ 
	quit();
	} 
	});
	jQuery.ajaxSetup({
		async : true
	});
}
function getMaxeval(who){
jQuery.ajaxSetup({
		async : false
	});
	$.get("./users/"+who+"/maxEval", function(data) {
		nbeval = data;
	});
	jQuery.ajaxSetup({
		async : true
	});
for (var r = 0;r < nbeval; r++) {
     document.getElementById("bcrumb").innerHTML += '<li class="active"><span>'+(r+1)+'</span></li>';
 } 
}
function maj_breadc(num){
	document.getElementById("bcrumb").innerHTML = "";
	var number=eval(num);
	if((number % 20) == 0){
		var p_ent = (Math.floor(number/20) -1) * (20) + 1;
		var p_entto =p_ent + 20 ;
		var blue_n = p_entto ;
 		var tot = eval(nbeval) +1;
	}else{
		var p_ent = Math.floor(number/20) * 20 + 1;
		var p_entto =p_ent + 20 ;
		var blue_n = number % 20 + p_ent ;
 		var tot = eval(nbeval) +1;
	}

if(p_entto>nbeval){
for (var r = p_ent;r<blue_n; r++) {
     document.getElementById("bcrumb").innerHTML +='<li><a href="#">'+r+'</a></li>';
      }
for (var r =blue_n;r <tot; r++) {
     document.getElementById("bcrumb").innerHTML += '<li class="active"><span>'+r+'</span></li>';
      }  
}else{
for (var r = p_ent;r < blue_n; r++) {
	 console.log(r);
     document.getElementById("bcrumb").innerHTML +='<li><a href="#">'+r+'</a></li>';
      } 
for (var r =blue_n;r < p_entto; r++) {
     document.getElementById("bcrumb").innerHTML += '<li class="active"><span>'+r+'</span></li>';
      } 
 document.getElementById("bcrumb").innerHTML += '<li class="active"><span>...</span></li>';
  document.getElementById("bcrumb").innerHTML += '<li class="active"><span>'+(nbeval)+'</span></li>';
  }
}

function setdatasetDescription() {
	var req ="./datasets/"+datasetg+"/des"
	jQuery.ajaxSetup({
		async : false
	});
	
	var res = $.get(req,function(data) {
	res = data;
	});
	$("#descriptiondatas").html(res.responseText);
	$("#titledatas").html(datasetg+" : ");
	$("#nameoftd").html("from the dataset "+ datasetg);
	
	jQuery.ajaxSetup({
		async : true
	});	
}
			
function getLabels() {
	var dataset = datasetg;
	// added by julien in case of no topic selected
		if (typeof parseInt(topicg) !== 'number')
		topicg = "0";	
	//	
	$("#topitwlid").html(topicg+" of dataset "+datasetg);
	var req = "./datasets/" + datasetg+ "/topics/" + topicg + "/labels/list/"
			+ labelPerTopic;
	
	$("#labels").html();
				var req = "./datasets/" + datasetg+ "/topics/" + topicg + "/labels/listforeval/"
			+ labelPerTopic;
			$
			.get(
					req,
					function(data) {

						var labelshtml = "";
						var labelers = Object.keys(data.labels);
						var ligne_n=[];
						var ligne_p=[];
						var compt = 0;
						labelers.forEach(
										function(key) {
										    compt++;
											var labels = data.labels[key];
												label_html ="";
												var valueslabels ="" ;
													if (labels.labeler.startsWith("sentence")){
													label_html  += "<tr>\n<td class=\"col-md-1\"><span>"
													+ labels.labeler+"</span></td>\n";
													valueslabels +='<p style="color: #428bca">'+labels.label+'<p>';
													
													 label_html  += "<td class=col-md-10>"
													+ valueslabels
													+'</td><td class=col-md-2>'
													+'<form id ="labgrad'+compt+'">'
													+'<input type="radio" name="optradio" value="1">Yes<br />'
													+'<input type="radio" name="optradio" checked="checked" value="0">I Don\'t Know<br />'
													+'<input type="radio" name="optradio" value="-1">No<br />'
													+'</form>'
													/*
													+ '<select class="form-control" style="width:auto;height:32px" id="labelgrade_'
													+ labels.labeler
													+'"><option value="NO"> I have No Opinion </option><option>3 - Yes, perfectly</option><option>2.a - Yes, but it is too broad</option><option >2.b - Yes, but it is too precise</option><option>1 - It is related, but not relevant</option><option>0 - No, it is unrelated</option></select>'
													*/
													+'</td>'
													+'</td>'
													+"\n</tr>";
													ligne_p.push(label_html);
													}else{
													label_html  += "<tr>\n<td class=\"col-md-1\"><span>"
													+ labels.labeler+"</span></td>\n";
												valueslabels +='<a href="#" onClick="getTopDocsftooltip(this.innerHTML);return false;">' 
												+ labels.label
												+ '</a>';
													
													 label_html  += "<td class=col-md-10>"
													+ valueslabels
													+'</td><td class=col-md-1>'
													+ '<select class="form-control" style="width:auto;height:32px" id="labelgrade_'
													+ labels.labeler
													+'"><option value="NO"> I have No Opinion </option><option>3 - Yes, perfectly</option><option>2.a - Yes, but it is too broad</option><option >2.b - Yes, but it is too precise</option><option>1 - It is related, but not relevant</option><option>0 - No, it is unrelated</option></select>'
													+'</td>'
													+'</td>'
													+"\n</tr>";
													ligne_n.push(label_html);
													}
													
										});
										labelshtml ="";
										labelshtmlp ="";
										ligne_n = FisherYates(ligne_n);
										ligne_p = FisherYates(ligne_p);
										for (var i = 0; i < ligne_n.length; i++) {
    										labelshtml+=ligne_n[i];
											}
											labelshtml +='<tr>\n'
													+'<td></td>'
													+'<td>'
													+'</td>'
													+'<td>'
												    +'</br> <button type="submit" class="btn btn-primary" style="width:85px;" onclick="GetCellValuesNG()">rate!</button>'	
													+'</td>\n</tr>';
										//console.log(ligne_p.length);
										for (var i = 0; i < ligne_p.length; i++) {
    										labelshtmlp+=ligne_p[i];
											}
												labelshtmlp +='<tr>\n'
													+'<td></td>'
													+'<td>'
													+'</td>'
													+'<td>'
												    +'</br> <button type="submit" class="btn btn-primary" style="width:85px;" onclick="GetCellValuesS()">rate!</button>'	
													+'</td>\n</tr>';
												

						$("#labels").html(labelshtml);
						$("#labelsp").html(labelshtmlp);
						
						$('#labels tr > *:nth-child(1)').toggle();
						$('#labelsp tr > *:nth-child(1)').toggle();
					
					});
}
function rectitle(){
$('#title_cho').html($("#usr").val());
document.getElementById("titleproposal").innerHTML= "";
document.getElementById("card").style.visibility = "visible";
document.getElementsByClassName("front")[0].style.visibility = "visible";
}
function showtructitle(){
document.getElementById("titleproposal").innerHTML= "</br><h4><b>Based on what you understand of the topic, wich label would you choose for it ?</h4 ></b>"
		+"<form class=\"form-inline\">"
		+"<input type=\"text\" class=\"form-control\" id=\"usr\" style=\"width: 57%\">"
		+"<button class=\"btn btn-default\" id=\"chosenwd\" onclick=\"rectitle()\">submit</button>"
		+"</form>";
}
function getWords() {
	var req = "./datasets/" + datasetg + "/topics/" + topicg + "/words/list/"
			+ wordPerTopic;
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
		toptnwords = [];
		for (var i = 0; i < words.length; i++) {
			//proba =(words[i].proba * words[i].proba)/ pond;
			proba =words[i].proba/pond;
			var tw = { mot:   words[i].mot, prob: proba };
			toptnwords.push(tw);		
					wordlist2 = wordlist2 + '<tr>\n<td class=\"col-md-1\">'
					+'<span><a href="#" onClick="getTopDocsftooltip(this.innerHTML);return false;">' + words[i].mot
					+ '</a></span></td>'
					+'<td><div class="progress" style="width:100%;margin:0px 0px 0px 0px;"><div class="progress-bar" role="progressbar" aria-valuenow="'+proba*100+'" aria-valuemin="0" aria-valuemax="100" style="width:'+proba*100+'%" data-toggle=\"tooltip\" data-placement=\"right\" title=\"<h5>Probability : '+words[i].proba.toFixed(4)+'</h5>\"></div></div></td>'		
					+'</tr>';
		}
		
		$("#topwordsn").html(wordlist2);
		//$(".ul-word").html(wordlist);
		$('[data-toggle="tooltip"]').tooltip({
			html: true,
		    container: 'body'
		});
	});
}

function getTopDocs() {
	document.getElementById('container_td').innerHTML ="";
	document.getElementById('container_td').innerHTML +='<div id = "container_td" style="position: relative;"><span id="topcdocs" style="position: relative;"></span>   <!-- <button id="colap" class="btn btn-secondary" data-toggle="collapse" data-target="#demo">Enhance Top-docs</button> --><div class="btn-toolbar" role="toolbar" aria-label="tbwbg"><div id ="list_td" class="btn-group btn-group-sm" role="group" aria-label="fg"></div></div><div id="demo" class="collapse" style=" text-align: justify">            <span id="docs" style="max-height: 300px;display: block;overflow: auto;"></span></div> </span>';
	topdocs_arr = [];
	$("#topcdocs").html('<h4><span id="nbtopdocs"></span></h4>');
	$("#nbtopdocs").html(nbtopdocs);
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
			for (var ki = 0;ki<10;ki++){
			//console.log(toptnwords[ki].mot);
			//var opacity_j = 1 - (ki/10);
			//var tp=toptnwords[ki].mot.replace(/\s+/g, '_');
			var tp = toptnwords[ki].mot.replace(/(?=[() ])/g, '\\');
			var expr_reg = "\\b"+tp+"\\b";
			var regex = new RegExp(expr_reg, 'gi');		
			txt = txt.replace(regex, '<mark style="background-color: rgba(255,255,0,'+toptnwords[ki].prob+')">'+toptnwords[ki].mot+'</mark>');
			
			}					
			if (docs[i].name) {
			html_td+=" </br>"+docs[i].name;
			}			
			html_td+="<blockquote class=\"quotetext\" style=\"margin-bottom : 0\"><p>" + txt + "</p>\n <footer id=\"selecteddoc\">top doc n&deg;"+(i+1)+"\n";
			if (docs[i].author) {
				html_td+= " - Written by " + docs[i].author;
			}			
			html_td+= "</footer>\n</blockquote>";
			docnames += docs[i].name;
			topdocs_arr.push(html_td); 
			document.getElementById("list_td").innerHTML += '<button type="button" id="colap" class="btn btn-default" onclick="showdoc(\'doc '+(i+1)+'\')">doc '+(i+1)+'</button>';
			if (i < (nbtopdocs-1)) 	{
				docnames += "*";
				}
		}
	});
}

function getTopDocspzd() {
	var text = "Top-"+eval(nbtopdocs+nbtopdocspzd)+ " documents ("+nbtopdocs+" p(d|z) + "+nbtopdocspzd+" p(z|d) in red )"
	$("#nbtopdocs").html(text);
	var req = "./datasets/" + datasetg + "/topics/" + topicg + "/topdocspzd/"+nbtopdocspzd;
	var docnames = "";
	$.get(req, function(data) {
		docs = data.docs;
		for (var i = 0; i < nbtopdocspzd; i++) {
			html_td="";
			if (i == 0) {
				docnames += "";
			}	
			var txt = docs[i].text;	
			for (var ki = 0;ki<10;ki++){
				var tp = toptnwords[ki].mot.replace(/(?=[() ])/g, '\\');
				var expr_reg = "\\b"+tp+"\\b";
				var regex = new RegExp(expr_reg, 'gi');	
				txt = txt.replace(regex, '<mark style="background-color: rgba(255,255,0,'+toptnwords[ki].prob+')">'+toptnwords[ki].mot+'</mark>');
			}					
			if (docs[i].name) {
				html_td+=" </br>"+docs[i].name;
			}			
			html_td+="<blockquote class=\"quotetext\" style=\"margin-bottom : 0\"><p>" + txt + "</p>\n <footer id=\"selecteddoc\">doc "+(nbtopdocs+i+1)+"\n";
			if (docs[i].author) {
				html_td+= " - Written by " + docs[i].author;
			}			
			html_td+= "</footer>\n</blockquote>";
			docnames += docs[i].name;
			topdocs_arr.push(html_td); 
			document.getElementById("list_td").innerHTML += '<button type="button" id="colap" class="btn btn-default" onclick="showdoc(\'doc '+(nbtopdocs+i+1)+'\')" style="background-color : red">doc '+(nbtopdocs+i+1)+'</button>';
			if (i < (nbtopdocs-1)) 	{
				docnames += "*";
			}
		}
	});
}


function showdoc(top){
var tc = "null";
for (var j = nbtopdocs;j<nbtopdocs+nbtopdocspzd;j++){
if (document.getElementById("list_td").childNodes[j].innerHTML.trim() == top.trim()){
document.getElementById("list_td").childNodes[j].style.backgroundColor = 'rgb(400,145,145)';
}else{
document.getElementById("list_td").childNodes[j].style.backgroundColor = 'red';
}
}
for (var j = 0;j<nbtopdocs;j++){
if (document.getElementById("list_td").childNodes[j].innerHTML.trim() == top.trim()){
document.getElementById("list_td").childNodes[j].style.backgroundColor = 'rgb(230,230,230)';
}else{
document.getElementById("list_td").childNodes[j].style.backgroundColor = '#fff';
}
}

if(document.getElementById("selecteddoc") != null){

	tc = document.getElementById("selecteddoc").innerHTML.trim();
	if( tc == top.trim()){
		$("#docs").html("");
		$('#demo').collapse("hide");
	}else{
		//console.log(tc);
		$('#demo').collapse();
		var doc_ec = topdocs_arr[top.split(" ")[1]-1];
		$("#docs").html(doc_ec);	
	}
}else{
//console.log( topdocs_arr);
	var doc_ec = topdocs_arr[top.split(" ")[1]-1];
	//console.log(topdocs_arr[0]);
	$("#docs").html(doc_ec);	
	$('#demo').collapse("show");
}
}
// nice tincel effect (think hard about how to improve it ;)
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

// print the distribution for all docs
function distrib_topics(docnames) {
	var split = docnames.split("*");
	for (var i = 0; i < split.length; i++) {
    	var docname = split[i];    	
    	print_distrib(docname, i);
	}
}

// print the distribution for the doc num. i whose name is docname
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
	}
}

//utilitaires
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
function getTopDocsftooltip(word) {
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
				/*if (docs[i].name) {
					content += "<br>";
				} */
				
				var txt = docs[i].text;
				//var regex = new RegExp("(" + word + "(?!\\w))", 'gi');
				//txt = txt.replace(regex, "<mark>$1</mark>");
				var tp = word.replace(/(?=[() ])/g, '\\');
				var regex = new RegExp("\\b"+tp+"\\b", 'gi');
				txt = txt.replace(regex, "<mark>"+word+"</mark>");
				content += "<span align='justify'><footer><mark>top doc n&deg;"+(docs[i].rang+1);
				if (docs[i].author) {
					content += " - Written by " + docs[i].author;
				}
				content +="</mark></footer>\n";
				content += "<p align='justify'>..."+txt+"...</p>";

				docnames += docs[i].name;
				if (i < (nbtopdocs-1)) 	{
					docnames += "*";
				}				
				content += "</span><br><br>";
				//\n</blockquote>
			}
		}
	});
	jQuery.ajaxSetup({
		async : true
	});
	$("#selected_w").html("<b>"+word+"</b>");
	var twb =window.innerHeight/2;
	$("#mod_content2").css('height', twb);
	$("#mod_content2").html(content);
	$("#mod_conttd").modal("show");
}

/*function getTopTopics(namedoc) {
	
	var dataset = datasetg;
	var req = "./datasets/" + dataset + "/toptopics/" + namedoc;
	
	var top_topics = new Array();
	
	jQuery.ajaxSetup({
		async : false
	});
	
	$.get(req, function(data) {

		docs = data.topics;
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
*/

//Ecriture
function GetCellValuesNG() {
     document.getElementsByClassName("front")[0].style.visibility = "hidden";
     document.getElementsByClassName("back")[0].style.visibility = "visible";
	var table = document.getElementById('labels');
	var user = localStorage.getItem("user");
	var r=0;
	var tableau = "";
	var ngr = new Map();
	var myArray = new Array(table.rows.length);  
    for (var r = 0, n = table.rows.length-1; r < n; r++) {
    	var Cell = table.rows[r].cells[0].innerText;
		var note = table.rows[r].cells[2].childNodes[0].value.split(" - ")[0].trim();
		tableau += ":" + Cell + "," + note;
		var person = {grade:note, ngram:table.rows[r].cells[1].innerText};
		if(note==="NA"){
		person = {grade:0, ngram:table.rows[r].cells[1].innerText};
		}
		myArray[r] = person; 
      }  
    jQuery.ajaxSetup({
		async : false
	});
	var sortFn = function(a, b) {
 		 if (a.grade < b.grade) return 1;
 		 if (a.grade > b.grade) return -1;
 		 if (a.grade == b.grade) return 0;
	}
	myArray.sort(sortFn);
	var maxi = myArray[0].grade;	
	var monSet = "<h4>Prefered n-grams</h4><p>";
	for(var i= 0; i < myArray.length-1; i++)
	{
     if(myArray[i].grade<maxi){
     	break;
     	}else{
     	monSet +='<span><a href="#" onClick="getTopDocsftooltip(this.innerHTML);return false;">' + myArray[i].ngram
					+ '</a></span>'
     	 monSet +="</br>";
     	}
	}
	monSet += "</p>";
	//$("#selected_ngrams").html(monSet);
	//$("#selected_ngrams").show();
	write_LRtoserv(user,datasetg,topicg,tableau.substring(1),"ng"); 
	jQuery.ajaxSetup({
		async : true
	});
    transitiontb();
}
function GetCellValuesS() {
	jQuery.ajaxSetup({
		async : false
	});
	transitiontf();
	jQuery.ajaxSetup({
		async : true
	});
	var user = localStorage.getItem("user");
	var r=0;
    var table_p = document.getElementById('labelsp');
	var tableau_p = "";
    for (var r = 0, n = table_p.rows.length-1; r < n; r++) {
    	var Cell = table_p.rows[r].cells[0].innerText;
		var aidi = '#'+table_p.rows[r].cells[2].childNodes[0].id;
		var note = $(aidi+" input[type='radio']:checked").val();
		tableau_p += ":" + Cell + "," + note;
      }  
    jQuery.ajaxSetup({
		async : false
	});
	write_LRtoserv(user,datasetg,topicg,tableau_p.substring(1),"sb"); 
	jQuery.ajaxSetup({
		async : true
	});
	$('#mod_final').modal('show');
}
function finalmodal(){
	jQuery.ajaxSetup({
		async : false
	});
	var user = localStorage.getItem("user");
	var keepit =  $("#keepit input[type='radio']:checked").val();
	console.log(keepit);
	var CW = document.getElementById('title_cho').innerHTML;
	write_LRtoserv(user,datasetg,topicg,CW+":"+keepit,"cw"); 
		jQuery.ajaxSetup({
		async : true
	});
$('#mod_final').modal('hide');
	jQuery.ajaxSetup({
		async : false
	});
	MAJ();
	jQuery.ajaxSetup({
		async : true
	});
}
function write_LRtoserv(user,datasetid,tid,tab,type) {
	var bool;
	var req =".\\user\\"+user+"\\"+datasetid+"\\"+tid+"\\"+tab+"\\"+type;
	$.get(req,bool);
}


//Tutorial
function tutorial(){

if (page_tuto == 11){
    var lalalala ='  <div class="modal-dialog modal-lg"><div  class="modal-content"><div class="modal-header">';
    lalalala +=  '<h4 class="modal-title" style="text-align:center">Time to start</h4></div>';
    lalalala +=  '<div class="modal-body"><p>Thank you for following the tutorial</p></div>';
    lalalala +=  '<div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Close</button></div></div></div>';
    $('#mod_cont').html(lalalala);

}else{
	var image =  '<img class="img-responsive" src="./public/'+page_tuto+'.PNG" alt="HTML5 Icon" style="">';
    var lalalala ='  <div class="modal-dialog modal-lg"><div  class="modal-content"><div class="modal-header">';
    lalalala +=  '<h4 class="modal-title" style="text-align:center;">Step '+page_tuto+' on 10</h4></div>';
    lalalala +=  '<div class="modal-body"><h4 style="text-align:center;font-weight: bold;">'+text_tuto[page_tuto-1]+'</h4></b></br>'+image+'</br><p style="float : right;"><a href="#" onClick="tutorial();return false;"> <span class="glyphicon glyphicon-forward" aria-hidden="true"></span></a></p></br>  	</div>';
    lalalala +=  '<div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Close</button></div></div></div>';
    $('#mod_cont').html(lalalala);
    page_tuto++;
    }
}

//Navigation, HTML
function quit(){
	//var theend = '<div class="modal-header">'+ '<h4 class="modal-title" style="text-align:center">Good by <span id="name"></span></h4></div>'+ '<div class="modal-body"><p></p></div>'+ '<div class="modal-footer"><button type="button" class="btn btn-default" onclick="quit()">Go Back to HomePage</button></div>';
      var lalalala ='  <div class="modal-dialog modal-lg"><div  class="modal-content"><div class="modal-header">';
       lalalala +=  '<h4 class="modal-title" style="text-align:center">Good by <span id="name"></span></h4></div>';
       lalalala +=  '<div class="modal-body"><p>Thank you for your participation</p></div>';
       lalalala +=  '<div class="modal-footer"><button type="button" class="btn btn-primary" onclick="goToHPage()">HomePage</button></div></div></div>';
     $('#mod_cont').html(lalalala);
     $('#mod_cont').modal('show'); 

}
function goToHPage(){
window.location.pathname = './index.html';
}
function transition(){
$("#card").flip('toggle');
}

//https://nnattawat.github.io/flip/
function transitiontb(){
	 document.getElementsByClassName("front")[0].style.visibility = "hidden";
	      document.getElementsByClassName("back")[0].style.visibility = "visible";
$("#card").flip(true);
}
function transitiontf(){
	document.getElementsByClassName("back")[0].style.visibility = "hidden";
	document.getElementsByClassName("front")[0].style.visibility = "hidden";
	$("#card").flip(false);
}

//Autres
function FisherYates(array) {
  var currentIndex = array.length, temporaryValue, randomIndex;
  // While there remain elements to shuffle...
  while (0 !== currentIndex) {

    // Pick a remaining element...	
    randomIndex = Math.floor(Math.random() * currentIndex);
    currentIndex -= 1;

    // And swap it with the current element.
    temporaryValue = array[currentIndex];
    array[currentIndex] = array[randomIndex];
    array[randomIndex] = temporaryValue;
  }
  return array;
}

$('#hidewords').on('click', function() {
	$("#wordselected").hide();
});