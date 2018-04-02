function graphics() {

	$.getJSON("./js/donnees2.json", function (data) {
		var barycentre_data = barycentre(obj, data)
		data.push(['barycentre_x',barycentre_data[0]], ['barycentre',barycentre_data[1]]);
		//console.log(data);
		graphic_embedding(data);
	});


	$.getJSON("./js/donnees3.json", function (data) {
		graphic_coherenceevolution(data);
	});


	$.getJSON("./js/donnees4.json", function (data) {
		graphic_pulse(data);
	});


	$.getJSON("./js/donnees5.json", function (data) {
		graphic_trajectory(data);
	});
}






/******************************************
**************TOPIC COHERENCE**************
******************************************/








/******************************************
******************EMBEDDING****************
******************************************/

function graphic_embedding (dataword) {

	var chart = c3.generate({
	    bindto: '.embedding',
	    data: {
	        xs: {
	        	word: 'word_x',
	        	barycentre: 'barycentre_x'
	    },
	        columns: dataword,
	        colors: {
	            word: '#0000ff',
	            barycentre: '#149414',
	        },
	        type: 'scatter'
	    },
	    axis: {
	        x: {
	            //label: 'Sepal.Width',
	            tick: {
	                fit: false
	            }
	        },
	        y: {
	            //label: 'Petal.Width'
	        }
	    },
	    tooltip: {
	        show: false
	    }
	});


	//graphic large
	var chart = c3.generate({
	    bindto: '.embedding-lg',
	    data: {
	        xs: {
	        	word: 'word_x',
	        	barycentre: 'barycentre_x'
	    	},
	        columns: dataword,
	        colors: {
	            word: '#0000ff',
	            barycentre: '#149414',
	        },
	        type: 'scatter'
	    },
	    axis: {
	        x: {
	            //label: 'Sepal.Width',
	            tick: {
	                fit: false
	            }
	        },
	        y: {
	            //label: 'Petal.Width'
	        }
	    },
	    size: {
	        width: 1000,
	        height:500
	    }
	});

}








/******************************************
************COHERENCE EVOLUTION************
******************************************/

function graphic_coherenceevolution (data) {

	var chart = c3.generate({
	    bindto: '.coherence_evolution',
	    data: {
	        xs: {
	            umass: 'data_x',
	            uci: 'data_x',
	            cv: 'data_x',
	            ca: 'data_x'
	        },
	        columns: data
	    },
	    axis: {
	        x: {
	            type: 'category',
	            categories: ['p1', 'p2', 'p3', 'p4', 'p5', 'p6'],
	        },
	        y: {
	            show: false
	        }
	    },
	    tooltip: {
	        show: false
	    }
	});


	//graphic large
	var chart = c3.generate({
	    bindto: '.coherence_evolution-lg',
	    data: {
	        xs: {
	            umass: 'data_x',
	            uci: 'data_x',
	            cv: 'data_x',
	            ca: 'data_x'
	        },
	        columns: data
	    },
	    axis: {
	        x: {
	            type: 'category',
	            categories: ['p1', 'p2', 'p3', 'p4', 'p5', 'p6'],
	        },
	        y: {
	            show: false
	        }
	    },
	    size: {
	        width: 1000,
	        height:500
	    }
	});

}








/******************************************
*******************PULSE*******************
******************************************/

function graphic_pulse(radius) {
	var chart = c3.generate({
	    bindto: '.pulse',
	    data: {
	        xs: {
	            w1: 'periode',
	            w2: 'periode',
	            w3: 'periode',
	            w4: 'periode',
	            w5: 'periode',
	            w6: 'periode',
	            w7: 'periode',
	            w8: 'periode',
	            w9: 'periode',
	            w10: 'periode'
	        },
	        columns: [
	            ['w1',1, 1, 1, 1, 1, 1],
	            ['w2',2, 2, 2, 2, 2, 2],
	            ['w3',3, 3, 3, 3, 3, 3],
	            ['w4',4, 4, 4, 4, 4, 4],
	            ['w5',5, 5, 5, 5, 5, 5],
	            ['w6',6, 6, 6, 6, 6, 6],
	            ['w7',7, 7, 7, 7, 7, 7],
	            ['w8',8, 8, 8, 8, 8, 8],
	            ['w9',9, 9, 9, 9, 9, 9],
	            ['w10',10, 10, 10, 10, 10, 10],
	            ['periode','p1', 'p2', 'p3', 'p4', 'p5', 'p6']
	        ],
	        type: 'scatter'
	    },
	    point: {
	        r: function(d) {
	            //return getRadius(d);
	            return radius[d.value-1].data[d.index];
	        }
	    },
	    axis: {
	        x: {
	            type: 'category',
	            categories: ['p1', 'p2', 'p3', 'p4', 'p5', 'p6'],
	        },
	        y: {
	            show: false
	        }
	    },
	    tooltip: {
	        show: false
	    }
	});


	//graphic large
	var chart = c3.generate({
	    bindto: '.pulse-lg',
	    data: {
	        xs: {
	            w1: 'periode',
	            w2: 'periode',
	            w3: 'periode',
	            w4: 'periode',
	            w5: 'periode',
	            w6: 'periode',
	            w7: 'periode',
	            w8: 'periode',
	            w9: 'periode',
	            w10: 'periode'
	        },
	        columns: [
	            ['w1',1, 1, 1, 1, 1, 1],
	            ['w2',2, 2, 2, 2, 2, 2],
	            ['w3',3, 3, 3, 3, 3, 3],
	            ['w4',4, 4, 4, 4, 4, 4],
	            ['w5',5, 5, 5, 5, 5, 5],
	            ['w6',6, 6, 6, 6, 6, 6],
	            ['w7',7, 7, 7, 7, 7, 7],
	            ['w8',8, 8, 8, 8, 8, 8],
	            ['w9',9, 9, 9, 9, 9, 9],
	            ['w10',10, 10, 10, 10, 10, 10],
	            ['periode','p1', 'p2', 'p3', 'p4', 'p5', 'p6']
	        ],
	        type: 'scatter'
	    },
	    point: {
	        r: function(d) {
	            //return getRadius(d);
	            return radius[d.value-1].data[d.index];
	        }
	    },
	    axis: {
	        x: {
	            type: 'category',
	            categories: ['p1', 'p2', 'p3', 'p4', 'p5', 'p6'],
	        },
	        y: {
	            show: false
	        }
	    },
	    size: {
	        width: 1000,
	        height:500
	    }

	});
}







/******************************************
******************TRAJECTORY***************
******************************************/

function graphic_trajectory (data) {

	var chart = c3.generate({
	    bindto: '.trajectory',
	    data: {
	        xs: {
	            data: 'data_x'
	        },
	        columns: data,
	        colors: {
	            data: '#0000ff',
	        },
	        type: 'scatter'
	    },
	    axis: {
	        x: {
	            //label: 'Sepal.Width',
	            tick: {
	                fit: false
	            }
	        },
	        y: {
	            //label: 'Petal.Width'
	        }
	    },
	    tooltip: {
	        show: false
	    }
	});


	//graphic large
	var chart = c3.generate({
	    bindto: '.trajectory-lg',
	    data: {
	        xs: {
	            data: 'data_x'
	        },
	        columns: data,
	        colors: {
	            data: '#0000ff',
	        },
	        type: 'scatter'
	    },
	    axis: {
	        x: {
	            //label: 'Sepal.Width',
	            tick: {
	                fit: false
	            }
	        },
	        y: {
	            //label: 'Petal.Width'
	        }
	    },
	    	    size: {
	        width: 1000,
	        height:500
	    }
	});
}


