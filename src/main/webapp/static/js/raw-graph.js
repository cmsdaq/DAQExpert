/**
 * 
 */
var rawoptions = {
	drawPoints : true,
	height : '300px',
	interpolation : false,
	orientation : 'bottom',
    zoomMin: 1000 * 60,                   // one minute in milliseconds
    zoomMax: 1000 * 60 * 60 * 24 * 31 * 12,// about 12 months in milliseconds

	dataAxis : {
		width : '50px',
		icons : false,
		left : {
			format : function(value) {
				return '' + value.toFixed(2);
			},
			title : {
				text : "<span class='glyphicon glyphicon-stop'></span> Avg. RU rate [kHz]",
				style : "color: #4f81bd;"
			}
		},
		right : {
			format : function(value) {
				return '' + value.toPrecision(2);
			},
			title : {
				text : "<span class='glyphicon glyphicon-stop'></span> Sum events in BU",
				style : "color: #f79646;"
			}
		}
	},
	legend : {
		left : {
			position : "bottom-left"
		}
	}
};

var initRawGraph = function() {

	/* containers */
	var rawcontainer = document.getElementById('raw');

	var rawitems = [];

	var rawdataset = new vis.DataSet(rawitems);

	var rawgroups = new vis.DataSet();
	rawgroups.add({
		id : 0,
		content : "rate",
		options : {
			yAxisOrientation : 'left'
		}
	})
	rawgroups.add({
		id : 1,
		content : "events",
		options : {
			yAxisOrientation : 'right',
			shaded : {
				orientation : 'zero'
			}
		}
	})

	var graph2d = new vis.Graph2d(rawcontainer, rawdataset, rawgroups,
			rawoptions);

	graph2d.on('rangechange', _.throttle(runDataUpdateFromGraph, 500, {
		leading : false
	}));

	graph2d.on('rangechange', _.throttle(runSyncFromGraph, 50, {
		leading : false
	}));

	/* Raw data click event handling */
	graph2d.on('click', function(properties) {
		// console.log("Clicked " +
		// JSON.stringify(properties['time']));
		var parameters = {};
		parameters['time'] = JSON.stringify(properties['time']);
		$.getJSON("snapshot", parameters, function(data) {
			var preetified = JSON.stringify(data, null, 2);
			// console.log(data['lastUpdate']);
			$("#json-body").html(preetified);
			$("#snapshot-date").html(moment(data['lastUpdate']).format());
			$('#snapshot-popup').modal('show')
		}).error(function(jqXHR, textStatus, errorThrown) {
			console.log("error " + textStatus);
			console.log("incoming Text " + jqXHR.responseText);
		});

	});
	return graph2d;
}

function getRawData(start, end) {

	parameters = {};
	parameters['start'] = start + "";
	parameters['end'] = end + "";

	$.getJSON("raw", parameters, function(data) {
		rawload(data);
	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
		console.log("incoming Text " + jqXHR.responseText);
	});
};

/* Load raw data to grap chart */
function rawload(data) {
	
	var rawdataset = graph2d['itemsData'];
	rawdataset.clear();
	$.each(data, function(key, value) {

		// rate entries
		if (value['group'] == 0) {
			value['y'] = (value['y'] / 1000);
		}

		// events entries
		else if (value['group'] == 1) {
			value['y'] = (value['y']);
		}
	});

	rawdataset.add(data);

};



var getUrlParameter = function getUrlParameter(sParam) {
	var sPageURL = decodeURIComponent(window.location.search.substring(1)), sURLVariables = sPageURL
			.split('&'), sParameterName, i;

	for (i = 0; i < sURLVariables.length; i++) {
		sParameterName = sURLVariables[i].split('=');

		if (sParameterName[0] === sParam) {
			return sParameterName[1] === undefined ? true : sParameterName[1];
		}
	}
};
