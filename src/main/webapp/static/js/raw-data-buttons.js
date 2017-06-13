/**
 * Script for generating links to RunInfo tool
 */

$(document).ready(function() {
	var wbmLinkBase = "https://cmswbm.web.cern.ch/cmswbm/cmsdb/servlet/RunSummary?";
	var time = "";

	var getLink = function() {
		return wbmLinkBase + runKey + runValue;
	}

	$('#raw-data-snapshot-button').click(function() {
		getTime();
		if(time == ""){
			console.log("Time is invalid");
		} 
		
		// console.log("Clicked " +
		// JSON.stringify(properties['time']));
		var parameters = {};
		parameters['time'] = JSON.stringify(time);
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
	
	$('.raw-data-flashlist-button').click(function() {
		getTime();
		if(time == ""){
			console.log("Time is invalid");
		} 
		var flashlistType = $( this ).data("type");
		
		// console.log("Clicked " +
		// JSON.stringify(properties['time']));
		var parameters = {};
		parameters['time'] = JSON.stringify(time);
		parameters['type'] = flashlistType;
		$.getJSON("flashlist", parameters, function(data) {
			var preetified = JSON.stringify(data, null, 2);
			// console.log(data['lastUpdate']);
			$("#flashlist-json-body").html(preetified);
			$("#flashlist-date").html("?");
			$("#flashlist-name").html(flashlistType);
			$('#flashlist-popup').modal('show')
		}).error(function(jqXHR, textStatus, errorThrown) {
			console.log("error " + textStatus);
			console.log("incoming Text " + jqXHR.responseText);
		});
	});
	


	
	var getTime = function(){
		p = getWindowInformationForAPI();
		var start = moment(new Date(p['start']).toISOString());
		var end = moment(new Date(p['end']).toISOString());
		var diff = end.diff(start);
		
		var duration = moment.duration(diff);

		var center = start.add(diff / 2, 'milliseconds');
		console.log("centerf: " + center.toISOString() + ", duration= " + duration.format());
		time = new Date(center);
		console.log("Michail format: " + time);
	}

});