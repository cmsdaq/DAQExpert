<!DOCTYPE HTML>
<html>

<head>
<title>DAQ Expert</title>


<script src="external/jquery.min.js"></script>
<script src="external/vis.external.js"></script>
<link href="external/vis.min.css" rel="stylesheet" type="text/css" />

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet"
	href="external/bootstrap-3.3.7-dist/css/bootstrap.min.css">

<!-- Optional theme -->
<link rel="stylesheet"
	href="external/bootstrap-3.3.7-dist/css/bootstrap-theme.min.css">

<!-- Latest compiled and minified JavaScript -->
<script src="external/bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
<script src="external/underscore-min.js"></script>
<script type="text/javascript" src="external/moment.min.js"></script>
<link href="external/bootstrap-tour.min.css" rel="stylesheet">
<script src="external/bootstrap-tour.min.js"></script>
<script src="external/moment-duration-format.min.js"></script>
<script src="static/js/run-info-link.js"></script>


<style type="text/css">
.vis-item {
	height: 16px;
	font-size: 8pt;
}

.vis-timeline {
	border: 2px solid #bfbfbf;
}

.vis-item.critical {
	background-color: red;
	border-color: darkred;
	color: white;
	font-family: monospace;
	box-shadow: 0 0 10px gray;
}

.vis-item.vis-background.filtered {
	background-color: rgba(0, 136, 204, 0.1);
}

.vis-item.vis-background.filtered-important {
	background-color: rgba(189, 54, 47, 0.1);
}

.vis-item.important {
	background-color: darkblue;
	border-color: darkblue;
	color: white;
	font-family: monospace;
	box-shadow: 0 0 10px gray;
}

.vis-item.warning {
	background-color: #f79646;
	border-color: #f79646;
	border-color: #E38D13;
	font-family: monospace;
	box-shadow: 0 0 10px gray;
}

.vis-item.experimental {
	color: white;
	background-color: #5bc0de;
	border-color: #2aabd2;
	font-family: monospace;
	box-shadow: 0 0 10px gray;
}

/* gray background in weekends, white text color */
.vis-time-axis .vis-grid.vis-saturday, .vis-time-axis .vis-grid.vis-sunday
	{
	background: rgba(244, 244, 244, .4);
}

.vis-time-axis .vis-grid.vis-h0-h4, .vis-time-axis .vis-grid.vis-h4-h8 {
	background: rgba(244, 244, 244, .4);
}

.vis-time-axis .vis-grid.vis-h0, .vis-time-axis .vis-grid.vis-h1,
	.vis-time-axis .vis-grid.vis-h2, .vis-time-axis .vis-grid.vis-h3,
	.vis-time-axis .vis-grid.vis-h4, .vis-time-axis .vis-grid.vis-h5,
	.vis-time-axis .vis-grid.vis-h6, .vis-time-axis .vis-grid.vis-h7 {
	background: rgba(244, 244, 244, .4);
}

/* The max width is dependant on the container (more info below) */
.popover {
	max-width: 350px;
	/* Max Width of the popover (depending on the container!) */
}

.vis-labelset .vis-inner {
	width: 8em;
}

.vis-left .vis-content {
	width: 8em;
}

.vis-right .vis-content {
	width: 8em;
}

.vis-panel.vis-background.vis-horizontal .vis-grid {
	left: 7.5em !important;
	right: 7.5em !important;
	width: auto !important;
}
</style>


<style type="text/css">
.loader {
	border: 16px solid #f3f3f3;
	border-radius: 50%;
	border-top: 16px solid #3498db;
	width: 120px;
	height: 120px;
	-webkit-animation: spin 0.5s linear infinite;
	animation: spin 0.5s linear infinite;
}

@
-webkit-keyframes spin { 0% {
	-webkit-transform: rotate(0deg);
}

100%{
-webkit-transform
:rotate(360deg)
;


}
}
@
keyframes spin { 0% {
	transform: rotate(0deg);
}
100%{
transform
:rotate(360deg)
;


}
}
</style>

</head>

<body>
	<%@  page import="rcms.utilities.daqexpert.Application"%>

	<nav class="navbar navbar-default navbar-xs" role="navigation">




		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse"
				data-target="#bs-example-navbar-collapse-1">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand"
				href="<%out.println(Application.get().getProp().getProperty(Application.LANDING));%>"><b>DAQ</b>
				Expert</a>
		</div>



		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse"
			id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav">

				<!-- EXPERT BROWSER -->
				<li class="active"><a href="#"><i
						class="glyphicon glyphicon-tasks"></i> Browser</a></li>

				<!-- NM DASHBOARD -->
				<li><a
					href="<%out.println(Application.get().getProp().getProperty(Application.NM_DASHBOARD));%>"><i
						class="glyphicon glyphicon-bell"></i> Dashboard</a></li>

				<!-- NM NOTIFICATIONS -->
				<li><a
					href="<%out.println(Application.get().getProp().getProperty(Application.NM_NOTIFICATIONS));%>"><i
						class="glyphicon glyphicon-calendar"></i> Notifications</a></li>

			</ul>
		</div>
		<!-- /.navbar-collapse -->
	</nav>

	<div class="container">

		<div class="btn-group btn-toggle" id="extended-view">
			<button id="mode1" type="button"
				class="mode-btn btn btn-sm btn-default btn-primary">Simple</button>
			<button id="mode2" type="button"
				class="mode-btn btn btn-sm btn-default">Extended</button>
			<button id="mode3" type="button"
				class="mode-btn btn btn-sm btn-default">Experiment</button>
		</div>


		<div class="btn-group pull-right ">
			<button style="display: none;" class="btn btn-sm btn-info"
				id="run-experimental-lm-button" href="#">
				Run experimental LMs <i class="glyphicon glyphicon-play"></i>
			</button>
			<button class="btn btn-sm btn-default" id="run-info-button"
				target="_blank">
				RunInfo <span class="glyphicon glyphicon-chevron-right"></span>
			</button>
			<button class="btn btn-sm btn-warning" id="tour" href="#">
				Help <i class="glyphicon glyphicon-question-sign"></i>
			</button>
		</div>

		<div id="visualization" style="margin-top: 15px;"></div>
		<div id="raw" style="margin-top: 5px;"></div>
		<p></p>
		<div id="log"></div>
	</div>

	<script type="text/javascript">

		var filtering = true;
		var mode = "standard";
		var lastExperimentalMode = "test";
		var lastTimeSpan = {};

		var lastData = [];

		$('.mode-btn').click(function() {
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

		var refreshBackgroundColor = (function(color) {
			$.each($('.vis-timeline'), function(key, value) {
				console.log(value);
				value.style.border = color;
			});
		});

		var items = new vis.DataSet([]);

		/* containers */
		var container = document.getElementById('visualization');
		var rawcontainer = document.getElementById('raw');

		var rawitems = [];

		var options = {
			editable : false,
			orientation : 'top',
			margin : {
				item : {
					horizontal : 0
				}
			}
		};

		var rawoptions = {
			drawPoints : true,
			height : '300px',
			interpolation : false,
			orientation : 'bottom',

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
			id : 'run-on',
			content : 'Run on (0)',
			name : 'Run on',
			title : 'Run ongoing',
			primary : false
		}, {
			id : 'warning',
			content : 'Warn (0)',
			name : 'Warn',
			title : 'Warnings',
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
		}, {
			id : 'adt',
			content : 'Avoid. DT (0)',
			name : 'Avoid. DT ',
			title : 'Avoidable downtime',
			primary : true
		}, {
			id : 'flowchart',
			content : 'FC (0)',
			name : 'FC',
			style : "background-color: white;font-weight:bold;",
			title : 'Flowchart events',
			primary : true
		}, {
			id : 'experimental',
			content : 'Exp. (0)',
			name : 'Exp. ',
			style : "background-color: white;font-weight:bold;",
			title : 'Experimental logic modules',
			primary : false
		} ];

		var groups = new vis.DataSet(groupsList);

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

		var timeline = new vis.Timeline(container, items, groups, options);

		var graph2d = new vis.Graph2d(rawcontainer, rawdataset, rawgroups,
				rawoptions);

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
				//here event propagation is stopped			
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
				//here event propagation is stopped			
			}
		};

		/** Load new data on event */
		function loadNewData(event, properties) {

			getData(properties["start"].toISOString(), properties["end"]
					.toISOString(), mode);
			getRawData(properties["start"].toISOString(), properties["end"]
					.toISOString());
		};

		/** Register event listener and throttle firing */
		timeline.on('rangechange', _.throttle(runDataUpdateFromTimeline, 500, {
			leading : false
		}));
		timeline.on('rangechange', _.throttle(runSyncFromTimeline, 50, {
			leading : false
		}));

		timeline.on('click', function(properties) {

			//console.log("Properties: "  + properties['what']);

			if (properties['what'] == 'item') {
				$('#reasonModal').modal('show');
				var parameters = {};
				parameters['id'] = properties['item'];

				$.getJSON(
						"raport",
						parameters,
						function(data) {

							$("#raport-name").html(data['name']);
							$("#raport-description").html(data['description']);

							if (data['elements'] == null) {
								$("#context-section").addClass("hidden");
							} else {
								$("#context-section").removeClass("hidden");
								var preetified = JSON.stringify(
										data['elements'], null, 2);
								$("#raport-body").html(preetified);
							}

							if (data['action'] == null) {
								$("#action-section").addClass("hidden");
							} else {
								$("#action-section").removeClass("hidden");
								$("#raport-action").html(
										"<ol id='curr-action'></ol>");
								//console.log(data['action']);
								$.each(data['action'], function(key, value) {
									$("#curr-action").append(
											$("<li>").text(value))
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

		function load(data) {
			var visibleData = [];
			countPerGroup = {};

			//console.log(data);

			/* Traverse new data to count events per group */
			$.each(data, function(index, value) {
				var groupName = value['group'];
				var currCount = 0;

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
					countPerGroup[groupName] = currCount
							+ parseInt(value['content']);
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

			//console.log(JSON.stringify(data));

			/* Update groups content */
			$.each(countPerGroup, function(index, value) {
				//console.log("Current: "+JSON.stringify(index));
				var current = groups.get(index);
				//console.log("Current: "+JSON.stringify(current));

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

		function getDatap(parameters) {
			$.getJSON(
					"reasons",
					parameters,
					function(data) {
						load(data['entries']);
						$.each(data['durations'], function(key, value) {
							console.log(key + ": "
									+ moment.duration(value).format()
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

		function getData(start, end, mode) {

			parameters = {};
			parameters['start'] = start + "";
			parameters['end'] = end + "";
			parameters['mode'] = mode + "";

			getDatap(parameters);

		};
	</script>



	<script type="text/javascript">
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

		graph2d.on('rangechange', _.throttle(runDataUpdateFromGraph, 500, {
			leading : false
		}));

		graph2d.on('rangechange', _.throttle(runSyncFromGraph, 50, {
			leading : false
		}));

		/* Raw data click event handling */
		graph2d.on('click', function(properties) {
			//console.log("Clicked " + JSON.stringify(properties['time']));
			var parameters = {};
			parameters['time'] = JSON.stringify(properties['time']);
			$.getJSON("snapshot", parameters, function(data) {
				var preetified = JSON.stringify(data, null, 2);
				//console.log(data['lastUpdate']);
				$("#json-body").html(preetified);
				$("#snapshot-date").html(moment(data['lastUpdate']).format());
				$('#snapshot-popup').modal('show')
			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("incoming Text " + jqXHR.responseText);
			});

		});

		/* Load raw data to grap chart */
		function rawload(data) {
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

		/* Initialize */
		$(document)
				.ready(
						function() {
							var defaultEnd = moment().add(1, 'hours');
							var defaultStart = moment().subtract(2, 'days');
							var useDefault = true;

							var requestedStart = getUrlParameter('start');
							var requestedEnd = getUrlParameter('end');
							var parsedStart = new Date(requestedStart);
							var parsedEnd = new Date(requestedEnd);

							//console.log("requested params: " + parsedStart + ", " + parsedEnd);

							if (Object.prototype.toString.call(parsedStart) === "[object Date]"
									&& Object.prototype.toString
											.call(parsedEnd) === "[object Date]") {
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

							//console.log("Initing with using default ranges: " + useDefault);
							properties = {};
							if (useDefault) {
								properties['start'] = defaultStart;
								properties['end'] = defaultEnd;
							} else {
								properties['start'] = parsedStart;
								properties['end'] = parsedEnd;
							}

							var filterParam = getUrlParameter('filter');
							//console.log("filter param: " + filterParam);
							if (filterParam == 'false') {
								filtering = false;
							}
							//console.log("filtering: " + filtering);

							loadNewData('rangechange', properties);
							timeline.setWindow(properties['start'],
									properties['end'], {
										animation : false
									});
							graph2d.setWindow(properties['start'],
									properties['end'], {
										animation : false
									});

						});

		var getUrlParameter = function getUrlParameter(sParam) {
			var sPageURL = decodeURIComponent(window.location.search
					.substring(1)), sURLVariables = sPageURL.split('&'), sParameterName, i;

			for (i = 0; i < sURLVariables.length; i++) {
				sParameterName = sURLVariables[i].split('=');

				if (sParameterName[0] === sParam) {
					return sParameterName[1] === undefined ? true
							: sParameterName[1];
				}
			}
		};

		// Instance the tour
		var tour = new Tour(
				{
					container : "body",
					name : "expert-tour",
					smartPlacement : true,
					placement : "left",
					keyboard : true,
					storage : window.localStorage,
					debug : false,
					backdrop : true,
					backdropContainer : 'body',
					backdropPadding : 0,
					redirect : true,
					orphan : false,
					duration : false,
					delay : false,
					steps : [

							{
								title : "Introduction",
								orphan : true,
								content : "This is DAQ Expert interactive visualization tool.</br>It visualizes results of analysis in time.</br>You can freely move and zoom in by dragging and scrolling in the timelines."
							},
							{
								element : "#visualization",
								title : "Analysis result",
								placement : 'bottom',
								content : "This is main analysis panel. Results and intermediate steps of reasoning are displayed here."
							},
							{
								title : "Elements",
								element : "#visualization",
								placement : 'bottom',
								content : function() {
									return '<p>Each row is visualizing one Logic Module results (or multiple Logic Modules if group name in bold). The number indicates how many events are visible in current time span. You can find details when you hover the label.</p><img src="external/expert-row.png" />';
								}
							},
							{
								title : "Element hiding",
								element : "#visualization",
								placement : 'bottom',
								content : function() {
									return '<p>When you zoom out elements will get smaller. For the clarity they will be hidden and replaced by shadow indicating how many elements are underneath.</p><img src="external/expert-filter-explain.png" />';
								}
							},
							{
								title : "Element color coding",
								element : "#visualization",
								placement : 'bottom',
								content : function() {
									return '<p>Elements are color coded. Red indicates that event is important and notification was generated. Blue indicates regular events.</p>';
								}
							},
							{
								title : "Element details",
								element : "#visualization",
								placement : 'bottom',
								content : function() {
									return '<p>Click on element to show details.</p><img src="external/details.png" />';
								}
							},
							{
								element : "#raw",
								title : "Raw data",
								placement : 'top',
								content : "This is raw data panel. Some parameters from snapshots are displayed here (avarage RU rate, sum of events in BU).</br>Time range is always synchronized with Analysis result timeline above."
							},
							{
								element : "#raw",
								title : "Raw data",
								placement : 'top',
								content : function() {
									return '<p>Click at any point in time to get the full snapshot in JSON format.</p><img src="external/snapshot.png" />';
								}
							},
							{
								element : "#extended-view",
								title : "View toggle",
								placement : 'right',
								content : "Toggle between simple and extended view here."
							},
							{
								element : "#tour",
								title : "Tour",
								placement : 'left',
								content : "You can always start this tour again here."
							} ]
				});
		$('#tour').click(function(e) {
			//console.log("Start tour");

			tour.restart();

			// it's also good practice to preventDefault on the click event
			// to avoid the click triggering whatever is within href:
			e.preventDefault();
		});

		$(document).ready(function() {
			//console.log("initializing tour");
			// Initialize the tour
			tour.init();

			// Start the tour
			tour.start();

		});
	</script>



	<div id="snapshot-popup" class="modal fade" tabindex="-1" role="dialog">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">Snapshot</h4>
				</div>
				<div class="modal-body">
					<p>
						Snapshot <span id="snapshot-date">/date/</span> in JSON format:
					</p>
					<pre class="prettyprint lang-json" id="json-body"></pre>
				</div>
				<div class="modal-footer"></div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

	<div id="experimental-run-popup" class="modal fade" tabindex="-1"
		role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">Summary of experimental run</h4>
				</div>
				<div class="modal-body">

					<label for="timespan-summary">Timespan</label>
					<p id="timespan-summary">
						<span class="badge" id="experimental-time-span-start">{}</span> -
						<span class="badge" id="experimental-time-span-end">{}</span>
					</p>

					<label for="duration-summary">Duration</label>
					<p id="duration-summary">
						<span class="badge" id="duration-humanized">{}</span>
					</p>
					<p id="processing-warning0-message" class="bg-danger">
						You are trying to process <span class='duration-in-message'></span>
						of snapshots. Processing will take too long. Please zoom in to
						interesting period of few minutes.
					</p>
					<p id="processing-warning1-message" class="bg-warning">
						You are about to process <span class='duration-in-message'></span>
						of snapshots. Processing may take few seconds. Be patient or
						zoom-in to interesting period.
					</p>

					<label for="elm-select">Select Logic Module from <code
							id='experimental-dir' class="inlinecode">/current/dir/to/lms/</code></label>
					<select class="form-control" id="elm-select">
						<option>sketch-file-name.java</option>
					</select>

					<div id="loader-animation" style="display: none;"
						class="loader text-centered"></div>

					<p id="processing-error-message" class="bg-danger">Error
						occurred while processing your Logic Module.</p>
					<pre id="processing-error-stacktrace"></pre>

				</div>

				<div class="modal-footer">
					<button id="experimental-load-button" type="button"
						class="btn btn-default">
						<i class="glyphicon glyphicon-repeat"></i> Load
					</button>
					<button id="experimental-run-process-button" type="button"
						class="btn btn-info">
						<i class="glyphicon glyphicon-play"></i> Run
					</button>

				</div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->


	<div id="reasonModal" class="modal fade" tabindex="-1" role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">Event details</h4>
				</div>
				<div class="modal-body">
					<h4 id="raport-name">/Name/</h4>
					<p id="raport-description">/description/</p>


					<div id="action-section">
						<h4>Action</h4>
						<p id="raport-action">/action/</p>
					</div>

					<div id="context-section">

						<button type="button" class="btn btn-info" data-toggle="collapse"
							data-target="#context-collapse">Show raw context</button>
						<div id="context-collapse" class="collapse">
							<h4>Context</h4>
							<pre id="raport-body"></pre>
						</div>
					</div>

				</div>
				<div class="modal-footer"></div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

	<script>
		/* Show modal experimental */
		$('#run-experimental-lm-button').click(function(e) {
			requestListOfExperimentalLM();
			params = getWindowInformationForAPI();
			/*console.log("Experiment requested with parameters "
					+ JSON.stringify(params));*/

			var start = moment(params['start']);
			var end = moment(params['end']);
			$('#experimental-time-span-start').html(start.format());
			$('#experimental-time-span-end').html(end.format());

			var diff = end.diff(start);
			$('#processing-warning0-message').hide();
			$('#processing-warning1-message').hide();
			$('#processing-error-message').hide();
			$('#processing-error-stacktrace').hide();
			$('#experimental-run-process-button').attr("disabled", false);
			if (diff > 1 * 60 * 60 * 1000) {
				$('#processing-warning0-message').show();
				$('#experimental-run-process-button').attr("disabled", true);
			} else if (diff > 30 * 60 * 1000) {
				$('#processing-warning1-message').show();
			}

			var duration = moment.duration(diff);

			console.log("Duration " + duration);
			$('.duration-in-message').html(duration.humanize());
			$('#duration-humanized').html(duration.humanize());

			$('#experimental-run-popup').modal('show')

		});

		var getWindowInformationForAPI = function() {
			parameters = {};
			parameters['start'] = timeline.getWindow()['start'].toISOString();
			parameters['end'] = timeline.getWindow()['end'].toISOString();
			return parameters;
		}

		var requestRunExperimentalLM = function() {
			params = getWindowInformationForAPI();
			var selected = $('#elm-select').find(":selected").text();
			//console.log("selected " + selected);
			params['experimental-lm'] = selected;
			mode = selected;
			lastExperimentalMode = selected;
			$('#loader-animation').show();
			$.getJSON("experiment", params, function(data) {
				console.log("Successfull call " + data);
				$('#loader-animation').hide();
				if (data['status'] == "success") {
					//console.log("Run without errors");
					$('#experimental-run-popup').modal('hide')
					getData(params['start'], params['end'], mode);
				} else {
					//console.log("Run with errors");
					//console.log("Error message: " + data['message']);
					$('#processing-error-stacktrace').html(data['message']);
					$('#processing-error-message').show();
					$('#processing-error-stacktrace').show();
				}

			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("errorThrown " + errorThrown);
				console.log("incoming Text " + jqXHR.responseText);
			});
		};

		var requestListOfExperimentalLM = function() {
			$.getJSON("scripts", params, function(data) {
				//console.log("Successfull call " + data);

				$('#experimental-dir').text(data['directory']);
				$("#elm-select").empty();
				$.each(data['names'], function(index, lm) {
					//console.log("base: "+ lm);
					$("<option>").appendTo($('#elm-select')).text(lm)
				});

			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("errorThrown " + errorThrown);
				console.log("incoming Text " + jqXHR.responseText);
			});
		};

		$('#experimental-run-process-button').click(function(e) {
			//console.log("Run experimental logic modules!");
			requestRunExperimentalLM();

			//$('#experimental-run-popup').modal('hide')
		});

		$('#experimental-load-button').click(function(e) {
			//console.log("Load experimental logic modules!");
			var selected = $('#elm-select').find(":selected").text();
			//console.log("selected now " + selected);
			mode = selected;
			lastExperimentalMode = selected;
			params = getWindowInformationForAPI();
			getData(params['start'], params['end'], mode);
			$('#experimental-run-popup').modal('hide')

		});
	</script>
</body>

</html>