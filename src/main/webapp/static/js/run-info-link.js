/**
 * Script for generating links to RunInfo tool
 */

$(document).ready(function() {
	var runInfoLinkBase = "http://cmsrc-srv.cms:9500/RunInfoTimeline/?";
	var sessionIdKey = "sessionId=";
	var sessionIdValue = "285585";
	var timeKey = "&time=";
	var timeValue = "2016-10-17T13:41:00.000Z";
	var timezoneKey = "&timezone=";
	var timezoneValue = "local";
	var windowKey = "&window=";
	var windowValue = "00:23:21.926";
	var maxWindowKey = "&maxWindow=";
	var maxWindowValue = "06:00:00.0";

	var getLink = function() {

		p = getWindowInformationForAPI();
		var start = moment(p['start']);
		var end = moment(p['end']);
		var diff = end.diff(start);
		
		var duration = moment.duration(diff);

		var center = start.add(diff / 2, 'milliseconds');
		console.log("centerf: " + center.toISOString() + ", duration= " + duration.format());

		timeValue = center.toISOString();
		windowValue = duration.format();

		return runInfoLinkBase + sessionIdKey + sessionIdValue
				+ timeKey + timeValue + timezoneKey
				+ timezoneValue + windowKey + windowValue
				+ maxWindowKey + maxWindowValue;
	}

	$('#run-info-button').click(function() {
		var found = findSessionId();
		
		console.log("Session id found: " + found);
		
		if(found){
			var link = getLink();
			console.log("Got to " + link);
			window.open(link);
		} else{
			console.log("Could not found session id");
		}
	});
	

	var findSessionId = function() {
		
		var result = false;
		
		$.each(lastData['entries'], function(key, value) {
			if(value['group'] == 'session-no' && value['className'] != 'filtered'){
				sessionIdValue = value['content'];
				console.log("Found session id: " + sessionIdValue);
				result = true;
			}
		});
		return result;
	}

});