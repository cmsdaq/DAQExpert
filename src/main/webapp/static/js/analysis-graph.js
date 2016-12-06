/**
 * 
 */

var groupsList = [ {
	id : 'lhc-beam',
	content : 'Beam (0)',
	name : 'Beam',
	title : 'LHC beam mode',
	primary : true
}, {
	id : 'lhc-machine',
	content : 'Machine (0)',
	name : 'Machine',
	title : 'LHC machine mode',
	primary : true
}, {
	id : 'session-no',
	content : 'Session (0)',
	name : 'Session',
	title : 'Session number',
	primary : true
}, {
	id : 'run-no',
	content : 'Run NO (0)',
	name : 'Run NO',
	title : 'Run number',
	primary : true
}, {
	id : 'daq-state',
	content : 'DAQ (0)',
	name : 'DAQ',
	title : 'DAQ state',
	primary : true
}, {
	id : 'level-zero',
	content : 'L0 (0)',
	name : 'L0',
	title : 'Level zero state',
	primary : false
},{
	id : 'tcds',
	content : 'TCDS (0)',
	name : 'TCDS',
	title : 'TCDS state',
	primary : false
}/**/, {
	id : 'expected',
	content : 'ER (0)',
	name : 'ER',
	title : 'Expected run',
	primary : false
}, {
	id : 'run-on',
	content : 'Run on (0)',
	name : 'Run on',
	title : 'Run ongoing',
	primary : false
}, {
	id : 'transition',
	content : 'Trans. (0)',
	name : 'Trans.',
	title : 'Transition period',
	primary : false
}, {
	id : 'no-rate',
	content : 'No rate (0)',
	name : 'No rate',
	title : 'No rate condition',
	primary : false
}, {
	id : 'nrwe',
	content : 'NRWE (0)',
	name : 'NRWE',
	title : 'No rate when expected',
	primary : false
}, {
	id : 'rate-oor',
	content : 'Rate OOR (0)',
	name : 'Rate OOR',
	title : 'Rate out of range',
	primary : false
}, {
	id : 'other',
	content : 'Other (0)',
	name : 'Other',
	title : 'Other conditions',
	primary : false
}, {
	id : 'dt',
	content : 'Downtime (0)',
	name : 'Downtime',
	title : 'Downtime = no rate during stable beams',
	primary : true
},/* {
	id : 'adt',
	content : 'Avoid. DT (0)',
	name : 'Avoid. DT ',
	title : 'Avoidable downtime',
	primary : true
}, */{
	id : 'flowchart',
	content : 'FC (0)',
	name : 'FC',
	style : "background-color: white;font-weight:bold;",
	title : 'Flowchart events',
	primary : true
}, {
	id : 'deadtime',
	content : 'Dead. (0)',
	name : 'Dead. ',
	title : 'Total deadtime',
	primary : false
}, {
	id : 'feddead',
	content : 'FEDD. (0)',
	name : 'FEDD. ',
	title : 'Individual FED deadtime',
	primary : false
},  {
	id : 'partition-dead',
	content : 'PDead. (0)',
	name : 'PDead. ',
	title : 'Partition deadtime',
	primary : false
},{
	id : 'ssdegraded',
	content : 'SSDegr. (0)',
	name : 'SSDegr. ',
	title : 'Subsystem running degraded',
	primary : false
},{
	id : 'ss-soft-err',
	content : 'SSSErr. (0)',
	name : 'SSSErr. ',
	title : 'Subsystem soft error',
	primary : false
},
{
	id : 'ss-err',
	content : 'SSErr. (0)',
	name : 'SSErr. ',
	title : 'Subsystem error',
	primary : false
},{
	id : 'warning',
	content : 'Warn (0)',
	name : 'Warn',
	title : 'Warnings',
	primary : false
} ];

var options = {
	editable : false,
	orientation : 'top',
	margin : {
		item : {
			horizontal : 0
		}
	}
};

var initAnalysisGraph = function() {

	var items = new vis.DataSet([]);
	var container = document.getElementById('visualization');
	var groups = new vis.DataSet(groupsList);
	var timeline = new vis.Timeline(container, items, groups, options);

	/** Register event listener and throttle firing */
	timeline.on('rangechange', _.throttle(runDataUpdateFromTimeline, 500, {
		leading : false
	}));
	timeline.on('rangechange', _.throttle(runSyncFromTimeline, 50, {
		leading : false
	}));

	timeline.on('click', function(properties) {

		// console.log("Properties: " +
		// properties['what']);

		if (properties['what'] == 'item') {
			$('#reasonModal').modal('show');
			var parameters = {};
			parameters['id'] = properties['item'];

			$.getJSON("raport", parameters, function(data) {

				$("#raport-name").html(data['name']);
				$("#raport-description").html(data['description']);
				$("#raport-duration").html(moment.duration(data['duration']).format());
				$("#raport-duration-humanized").html(moment.duration(data['duration']).humanize());

				if (data['elements'] == null) {
					$("#context-section").addClass("hidden");
				} else {
					$("#context-section").removeClass("hidden");
					var preetified = JSON.stringify(data['elements'], null, 2);
					$("#raport-body").html(preetified);
				}

				if (data['action'] == null) {
					$("#action-section").addClass("hidden");
				} else {
					$("#action-section").removeClass("hidden");
					$("#raport-action").html("<ol id='curr-action'></ol>");
					// console.log(data['action']);
					$.each(data['action'], function(key, value) {
						$("#curr-action").append($("<li>").text(value))
					});
				}

			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("errorThrown " + errorThrown);
				console.log("incoming Text " + jqXHR.responseText);
			});

		} else {
			console.log("No event selected...");
			return;
		}

	});
	return timeline;
}

function getData(start, end, mode) {

	parameters = {};
	parameters['start'] = start + "";
	parameters['end'] = end + "";
	parameters['mode'] = mode + "";

	getDatap(parameters);

};

function getDatap(parameters) {
	$.getJSON(
			"reasons",
			parameters,
			function(data) {
				load(data['entries']);
				$.each(data['durations'], function(key, value) {
					console.log(key + ": " + moment.duration(value).format()
							+ ", humanized: "
							+ moment.duration(value).humanize());
				});
				lastData = data;
			}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
		console.log("errorThrown " + errorThrown);
		console.log("incoming Text " + jqXHR.responseText);
	});
}

/** Load new data on event */
function loadNewData(event, properties) {
	getData(properties["start"].toISOString(), properties["end"].toISOString(),
			mode);
	getRawData(properties["start"].toISOString(), properties["end"]
			.toISOString());
};

function load(data) {
	var visibleData = [];
	countPerGroup = {};
	
	var groups = timeline['groupsData'];
	var items = timeline['itemsData'];

	// console.log(data);

	/* Traverse new data to count events per group */
	$.each(data, function(index, value) {
		var groupName = value['group'];
		var currCount = 0;
		

//		for (prop in timeline) {
//			console.log("groups: " + prop);
//		}

		var current = groups.get(groupName);
		if (filtering == false) {
			visibleData.push(value);
		} else {
			if (current['primary'] == true)
				visibleData.push(value);
		}

		/* Get current count */
		if (groupName in countPerGroup) {
			currCount = countPerGroup[groupName];
		}

		/* add current element */
		if (value['className'] == 'filtered'
				|| value['className'] == 'filtered-important') {
			countPerGroup[groupName] = currCount + parseInt(value['content']);
			value['type'] = 'background';
		} else {
			countPerGroup[groupName] = currCount + 1;
		}
	});

	/* If no data put zero in countPerGroup map */
	$.each(groupsList, function(index, value) {
		var groupName = value['id'];
		if (!(groupName in countPerGroup)) {
			countPerGroup[groupName] = 0;
		}
	});

	// console.log(JSON.stringify(data));

	/* Update groups content */
	$.each(countPerGroup, function(index, value) {
		// console.log("Current: "+JSON.stringify(index));
		var current = groups.get(index);
		// console.log("Current: "+JSON.stringify(current));

		var newContent = "";

		if (filtering == false) {
			newContent = current['name'] + " (" + value + ")";
		} else {
			if (current['primary'] == true)
				newContent = current['name'] + " (" + value + ")";
		}

		groups.update({
			id : index,
			content : newContent
		});

	});

	items.clear();
	items.add(visibleData);

};

