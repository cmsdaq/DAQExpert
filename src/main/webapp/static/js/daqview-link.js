/**
 * Script for generating links to DAQView tool
 */

$(document).ready(function() {
	var daqViewBase = "http://daq-expert-dev.cms/daq2view-react/index.html?setup=cdaq";
	var timeKey = "&time=";
	var time = "";

	var getLink = function(realtime) {
		
		if(realtime === true){
			return daqViewBase ;
		} else{
			return daqViewBase +timeKey+ time ;
		}
	}

	$('#daqview-button').click(function() {
		console.log("Clicked DAQView button");
		
		getTime();
		if(time == ""){
			console.log("Time is invalid");
		} if( moment(new Date(time).toISOString()).isAfter(moment())){
			time = moment();
			var link = getLink(true);
			console.log("Got to realtime " + link);
			window.open(link);
		} else{

			var link = getLink(false);
			console.log("Got to archive " + link);
			window.open(link);
		}
	});
	
	var getTime = function(){
		p = getWindowInformationForAPI();
		var start = moment(new Date(p['start']).toISOString());
		var end = moment(new Date(p['end']).toISOString());
		var diff = end.diff(start);
		
		var duration = moment.duration(diff);

		var center = start.add(diff / 2, 'milliseconds');
		console.log("centerf: " + center.toISOString() + ", duration= " + duration.format());
		time = center;
	}
	


});