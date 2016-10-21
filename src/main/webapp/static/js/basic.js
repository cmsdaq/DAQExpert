/**
 * 
 */

var graph2d;
var timeline;

var filtering = true;
var mode = "standard";
var lastTimeSpan = {};
var lastData = [];

var lastExperimentalMode = "test";


$(document).ready(function() {
	$('.mode-btn').click(function() {
		console.log("Changing mode");
		$('.mode-btn').removeClass('btn-primary');
		$(this).addClass('btn-primary')
	
	});
	
	$('#mode1').click(function() {
		filtering = true;
		mode = "standard";
		refreshBackgroundColor("2px solid #bfbfbf");
		params = getWindowInformationForAPI();
		params['mode'] = mode;
		getDatap(params);
		$('#run-experimental-lm-button').hide();
	});
	$('#mode2').click(function() {
		filtering = false;
		mode = "standard";
		refreshBackgroundColor("2px solid #bfbfbf");
		params = getWindowInformationForAPI();
		params['mode'] = mode;
		getDatap(params);
		$('#run-experimental-lm-button').hide();
	});
	$('#mode3').click(function() {
		filtering = false;
		mode = lastExperimentalMode;
		refreshBackgroundColor("2px solid #aa6708");
		params = getWindowInformationForAPI();
		params['mode'] = mode;
		getDatap(params);
		$('#run-experimental-lm-button').show();
	});
});

var getWindowInformationForAPI = function() {
	parameters = {};
	parameters['start'] = timeline.getWindow()['start'].toISOString();
	parameters['end'] = timeline.getWindow()['end'].toISOString();
	return parameters;
}

/** Refresh data on timeline event */
var runDataUpdateFromTimeline = function(properties) {
	var byUser = properties["byUser"];
	if (byUser) {
		loadNewData('rangechange', properties);
	}
};

/** Refresh data on graph event */
var runDataUpdateFromGraph = function(properties) {
	var byUser = properties["byUser"];
	if (byUser) {
		loadNewData('rangechange', properties);
	}
};

/** Refresh views on timeline event */
var runSyncFromTimeline = function(properties) {
	var start = properties["start"];
	var end = properties["end"];

	var byUser = properties["byUser"];
	if (byUser) {
		graph2d.setWindow(start, end, {
			animation : false
		});
	} else {
		// here event propagation is stopped
	}
};

/** Refresh views on graph event */
var runSyncFromGraph = function(properties) {
	var start = properties["start"];
	var end = properties["end"];

	var byUser = properties["byUser"];
	if (byUser) {
		timeline.setWindow(start, end, {
			animation : false
		});
	} else {
		// here event propagation is stopped
	}
};

/* Initialize */

$(document).ready(function() {
	
	graph2d = initRawGraph();
	timeline = initAnalysisGraph();
	
	var defaultEnd = moment().add(1, 'hours');
	var defaultStart = moment().subtract(2, 'days');
	var useDefault = true;

	var requestedStart = getUrlParameter('start');
	var requestedEnd = getUrlParameter('end');
	var parsedStart = new Date(requestedStart);
	var parsedEnd = new Date(requestedEnd);

	// console.log("requested params: " +
	// parsedStart + ", " + parsedEnd);

	if (Object.prototype.toString.call(parsedStart) === "[object Date]"
			&& Object.prototype.toString.call(parsedEnd) === "[object Date]") {
		// it is a date
		if (isNaN(parsedStart.getTime())
				|| isNaN(parsedEnd.getTime())) {
			// date is not valid
			useDefault = true;
		} else {
			useDefault = false;
		}
	} else {
		// not a date
		useDefault = true;
	}

	// console.log("Initing with using
	// default ranges: " + useDefault);
	properties = {};
	if (useDefault) {
		properties['start'] = defaultStart;
		properties['end'] = defaultEnd;
	} else {
		properties['start'] = parsedStart;
		properties['end'] = parsedEnd;
	}

	var filterParam = getUrlParameter('filter');
	// console.log("filter param: " +
	// filterParam);
	if (filterParam == 'false') {
		filtering = false;
	}
	// console.log("filtering: " +
	// filtering);

	loadNewData('rangechange', properties);
	timeline.setWindow(properties['start'], properties['end'],
			{
				animation : false
			});
	graph2d.setWindow(properties['start'], properties['end'], {
		animation : false
	});

});