/**
 * Script for generating links to RunInfo tool
 */

$(document).ready(function() {
	var wbmLinkBase = "https://cmswbm.web.cern.ch/cmswbm/cmsdb/servlet/RunSummary?";
	var runKey = "RUN=";
	var runValue = "285585";

	var getLink = function() {
		return wbmLinkBase + runKey + runValue;
	}

	$('#wbm-button').click(function() {
		console.log("Clicked wbm button");
		var found = findRunId();
		
		console.log("Run id found: " + found);
		
		if(found){
			var link = getLink();
			console.log("Got to " + link);
			window.open(link);
		} else{
			$('#wbm-popup').modal('show');
			console.log("Could not found session id");
		}
	});
	

	var findRunId = function() {
		
		var found = 0;
		
		$.each(lastData['entries'], function(key, value) {
			if(value['group'] == 'run-no' && value['className'] != 'filtered'){
				runValue = value['content'];
				console.log("Found run no: " + runValue);
				found += 1;
			}
		});
		
		console.log("Visible sessions: " + found);
		if(found == 1){
			return true;
		} else {
			return false;
		}
	}

});