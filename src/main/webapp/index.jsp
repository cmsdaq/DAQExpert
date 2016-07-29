<!DOCTYPE HTML>
<html>

<head>
<title>DAQ Expert</title>


<script src="external/jquery.min.js"></script>
<script src="external/vis.external.js"></script>
<link href="external/vis.min.css" rel="stylesheet" type="text/css" />

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
	integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
	crossorigin="anonymous">

<!-- Optional theme -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
	integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r"
	crossorigin="anonymous">

<!-- Latest compiled and minified JavaScript -->
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
	integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
	crossorigin="anonymous"></script>


<script
	src="https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.8.3/underscore-min.js"></script>


<script type="text/javascript"
	src="https://cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>


<link
	href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-tour/0.10.3/css/bootstrap-tour.min.css"
	rel="stylesheet">
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-tour/0.10.3/js/bootstrap-tour.min.js"></script>


<style type="text/css">
body, html {
	font-family: sans-serif;
}

.vis-item {
	height: 16px;
	font-size: 8pt;
}

/* create a custom sized dot at the bottom of the red item */
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

/* navbar */
.navbar-xs {
	min-height: 22px;
	border-radius: 0
}

.navbar-xs .navbar-brand {
	padding: 2px 8px;
	font-size: 14px;
	line-height: 14px;
}

.navbar-xs .navbar-nav>li>a {
	border-right: 1px solid #ddd;
	padding-top: 2px;
	padding-bottom: 2px;
	line-height: 16px
}

.navbar-nav>li>a, .navbar-brand {
	padding-top: 5px !important;
	padding-bottom: 0 !important;
	height: 30px;
}

.navbar {
	min-height: 30px !important;
	margin: 0px;
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
			<a class="navbar-brand" href="#"><b>DAQ</b> Expert</a>
		</div>

		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse"
			id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav">
				<li><a
					href="<%out.println(Application.get().getProp().getProperty(Application.NM_URL));%>"><i
						class="glyphicon glyphicon-bell"></i> Notification Manager</a></li>
				<li><a href="https://github.com/cmsdaq/DAQExpert"><i
						class="glyphicon glyphicon-tags"></i> Project repo</a></li>
				<li><a id="tour" href="#"><i
						class="glyphicon glyphicon-question-sign"></i> Tour</a></li>

			</ul>
		</div>
		<!-- /.navbar-collapse -->
	</nav>

	<div id="visualization"></div>
	<div id="raw"></div>
	<p></p>
	<div id="log"></div>

	<script type="text/javascript">
		var items = new vis.DataSet([]);


		/* containers */
		var container = document.getElementById('visualization');
		var rawcontainer = document.getElementById('raw');
		
		var rawitems = [];
		
		
		var options = {
			editable : false,
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
				orientation : 'top',
				throttleRedraw : 100,
				dataAxis : {
					title : {
						text : "aa"
					},
					width : '50px',
					icons : false
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
			title : 'LHC beam mode'
		}, {
			id : 'lhc-machine',
			content : 'Machine (0)',
			name : 'Machine',
			title : 'LHC machine mode'
		}, {
			id : 'daq-state',
			content : 'DAQ (0)',
			name : 'DAQ',
			title : 'DAQ state'
		}, {
			id : 'level-zero',
			content : 'L0 (0)',
			name : 'L0',
			title : 'Level zero state'
		}, {
			id : 'session-no',
			content : 'Session (0)',
			name : 'Session',
			title : 'Session number'
		}, {
			id : 'run-no',
			content : 'Run NO (0)',
			name : 'Run NO',
			title : 'Run number'
		}, {
			id : 'run-on',
			content : 'Run on (0)',
			name : 'Run on',
			title : 'Run ongoing'
		},  {
			id : 'error',
			content : 'Error (0)',
			name : 'Error',
			title : 'Errors'
		}, {
			id : 'warning',
			content : 'Warn (0)',
			name : 'Warn',
			title : 'Warnings'
		}, {
			id : 'no-rate',
			content : 'No rate (0)',
			name : 'No rate',
			title : 'No rate condition'
		}, {
			id : 'rate-oor',
			content : 'Rate OOR (0)',
			name : 'Rate OOR',
			title : 'Rate out of range'
		}, {
			id : 'other',
			content : 'Other (0)',
			name : 'Other',
			title : 'Other conditions'
		}, {
			id : 'fl1',
			content : 'FC1 (0)',
			name : 'FC1',
			title : 'Flowchart events, case 1'
		}, {
			id : 'fl2',
			content : 'FC2 (0)',
			name : 'FC2',
			title : 'Flowchart events, case 2'
		}, {
			id : 'fl3',
			content : 'FC3 (0)',
			name : 'FC3',
			title : 'Flowchart events, case 3'
		}, {
			id : 'fl4',
			content : 'FC4 (0)',
			name : 'FC4',
			title : 'Flowchart events, case 4'
		}, {
			id : 'fl5',
			content : 'FC5 (0)',
			name : 'FC5',
			title : 'Flowchart events, case 5'
		}, {
			id : 'fl6',
			content : 'FC6 (0)',
			name : 'FC6',
			title : 'Flowchart events, case 6'
		} ];

		var groups = new vis.DataSet(groupsList);
		


		var rawdataset = new vis.DataSet(rawitems);

		var rawgroups = new vis.DataSet();
		rawgroups.add({
			id : 0,
			content : "rate [kHz]",
			options : {
				yAxisOrientation : 'left'
			}
		})
		rawgroups.add({
			id : 1,
			content : "events (x10^6)",
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

			getData(JSON.stringify(properties["start"]), JSON
					.stringify(properties["end"]));
			getRawData(JSON.stringify(properties["start"]), JSON
					.stringify(properties["end"]));
		};

		/** Register event listener and throttle firing */
		timeline.on('rangechange', _.throttle(runDataUpdateFromTimeline, 500, {
			leading : false
		}));
		timeline.on('rangechange', _.throttle(runSyncFromTimeline, 50, {
			leading : false
		}));

		timeline.on('click', function(properties) {
			
			console.log("Properties: "  + properties['what']);
			
			if(properties['what'] == 'item'){
				$('#reasonModal').modal('show');
				var parameters = {};
				parameters['id'] = properties['item'];
				
				$.getJSON("raport", parameters, function(data) {
	
					$("#raport-name").html(data['name']);
					$("#raport-description").html(data['description']);
	
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
						$.each(data['action'], function(key, value) {
							$("#curr-action").append($("<li>").text(value))
						});
					}
	
				}).error(function(jqXHR, textStatus, errorThrown) {
					console.log("error " + textStatus);
					console.log("errorThrown " + errorThrown);
					console.log("incoming Text " + jqXHR.responseText);
				});

			} else{
				console.log("No event selected...");	
				return;
			}

		});

		function load(data) {

			countPerGroup = {};

			/* Traverse new data to count events per group */
			$.each(data, function(index, value) {
				var groupName = value['group'];
				var currCount = 0;

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

			$.each(countPerGroup, function(index, value) {
				var current = groups.get(index);
				//console.log("Current: "+JSON.stringify(current));
				groups.update({
					id : index,
					content : current['name'] + " (" + value + ")"
				});

			});

			items.clear();
			items.add(data);

		};

		function getData(start, end) {

			parameters = {};
			parameters['start'] = start + "";
			parameters['end'] = end + "";

			$.getJSON("reasons", parameters, function(data) {
				load(data);
			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("errorThrown " + errorThrown);
				console.log("incoming Text " + jqXHR.responseText);
			});
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
			console.log("Clicked " + JSON.stringify(properties['time']));
			var parameters = {};
			parameters['time'] = JSON.stringify(properties['time']);
			$.getJSON("snapshot", parameters, function(data) {
				var preetified = JSON.stringify(data, null, 2);
				document.getElementById("json-body").innerHTML = preetified;
				$('#myModal').modal('show')
			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("incoming Text " + jqXHR.responseText);
			});

		});

		/* Load raw data to grap chart */
		function rawload(data) {
			rawdataset.clear();
			rawdataset.add(data);

		};

		/* Initialize */
		$(document).ready(function() {
			var defaultEnd = moment().add(1, 'hours');
			var defaultStart = moment().subtract(2, 'days');
			var useDefault = true;

			var requestedStart = getUrlParameter('start');
			var requestedEnd = getUrlParameter('end');
			var parsedStart = new Date(requestedStart);
			var parsedEnd = new Date(requestedEnd);

			console.log("requested params: " + parsedStart
					+ ", " + parsedEnd);

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

			console.log("Initing with using default ranges: "
					+ useDefault);
			properties = {};
			if (useDefault) {
				properties['start'] = defaultStart;
				properties['end'] = defaultEnd;
			} else {
				properties['start'] = parsedStart;
				properties['end'] = parsedEnd;
			}

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
		var tour = new Tour({
			container : "body",
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
			    	content : "The DAQExpert provides interactive visualization tool.</br>It visualizes DAQ data and expert analysis in time.</br>You can freely move and zoom in the timeline by dragging and scrolling in the timelines"
			    },
				{
					element : "#visualization",
					title : "Analysis result",
					placement : 'bottom',
					content : "Results of the Expert analysis will be displayed here.</br>You can click on each block to get more details."
				},
				{
					element : "#raw",
					title : "Raw data",
					placement : 'top',
					content : "Raw data from DAQAggregator will be displayed here. </br>Time range is always synchronized with Analysis result timeline above.</br>You can click at any point in time to get the full snapshot. "
				} ]
			});
		$('#tour').click(function(e) {
			console.log("Start tour");

			tour.restart();

			// it's also good practice to preventDefault on the click event
			// to avoid the click triggering whatever is within href:
			e.preventDefault();
		});

		$(document).ready(function() {
			console.log("initializing tour");
			// Initialize the tour
			tour.init();

			// Start the tour
			tour.start();

		});
	</script>



	<div id="myModal" class="modal fade" tabindex="-1" role="dialog">
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
						Snapshot <span id="snapshotDate">/date/</span> in JSON format:
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

						<div id="context-section" >
						
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
</body>

</html>