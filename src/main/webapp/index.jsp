<!DOCTYPE HTML>
<html>

<head>
<title>DAQ Expert</title>

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

.vis-item.filtered {
	background-color: darkorange;
	height: 4px; //
	box-shadow: 0 0 10px gray;
	color: white; /* text color */
	font-size: 0pt; /* there is no text */
	border-width: 0px; /* there is no border */
}

.vis-item.important {
	background-color: darkblue;
	border-color: darkblue;
	color: white;
	font-family: monospace;
	box-shadow: 0 0 10px gray;
}
</style>

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


</head>

<body>
	<div id="visualization"></div>
	<div id="raw"></div>
	<p></p>
	<div id="log"></div>

	<script type="text/javascript">
		var items = new vis.DataSet([]);

		var container = document.getElementById('visualization');
		var options = {
			editable : false,
			margin : {
				item : {
					horizontal : 0
				}
			}
		
		};

		var groups = new vis.DataSet([ {
			id : 'lhc',
			content : 'LHC'
		}, {
			id : 'daq',
			content : 'daq'
		}, {
			id : 'run',
			content : 'run'
		}, {
			id : 'error',
			content : 'error'
		}, {
			id : 'warning',
			content : 'warning'
		}, {
			id : 'info',
			content : 'info'
		}, {
			id : 'fl1',
			content : 'flowchart M1'
		}, {
			id : 'fl2',
			content : 'flowchart M2'
		}, {
			id : 'fl3',
			content : 'flowchart M3'
		}, {
			id : 'fl4',
			content : 'flowchart M4'
		}, {
			id : 'fl5',
			content : 'flowchart M5'
		}, {
			id : 'fl6',
			content : 'flowchart M6'
		} ]);
		var timeline = new vis.Timeline(container, items, groups, options);

		/** Refresh data on timeline event */
		var runDataUpdateFromTimeline = function(properties) {
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

		/** Refresh data on graph event */
		var runDataUpdateFromGraph = function(properties) {
			var byUser = properties["byUser"];
			if (byUser) {
				loadNewData('rangechange', properties);
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

		timeline
				.on(
						'click',
						function(properties) {

							$('#reasonModal').modal('show')
							var parameters = {};
							parameters['id'] = properties['item'];
							$
									.getJSON(
											"raport",
											parameters,
											function(data) {
												var preetified = JSON
														.stringify(
																data['elements'],
																null, 2);
												document
														.getElementById("raport-name").innerHTML = data['name'];
												document
														.getElementById("raport-description").innerHTML = data['description'];
												document
														.getElementById("raport-action").innerHTML = data['action'];
												document
														.getElementById("raport-body").innerHTML = preetified;
											}).error(
											function(jqXHR, textStatus,
													errorThrown) {
												console.log("error "
														+ textStatus);
												console.log("errorThrown "
														+ errorThrown);
												console.log("incoming Text "
														+ jqXHR.responseText);
											});

						});

		function load(data) {

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

		var rawcontainer = document.getElementById('raw');
		var rawitems = [];

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
				shaded: {
	                orientation: 'zero'
	            }
			}
		})
		var rawoptions = {
			drawPoints : true,
	        orientation:'top',
			dataAxis : {
				title : { text : "aa" },
				width:  '50px',
				icons : false
			},
			legend: {left:{position:"bottom-left"}},

		};

		var graph2d = new vis.Graph2d(rawcontainer, rawdataset, rawgroups,
				rawoptions);

		graph2d.on('rangechange', _.throttle(runDataUpdateFromGraph, 500, {
			leading : false
		}));
		graph2d.on('rangechange', _.throttle(runSyncFromGraph, 50, {
			leading : false
		}));

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

		function rawload(data) {
			rawdataset.clear();
			rawdataset.add(data);

		};

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
		var tour = new Tour(
				{

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
								element : "#visualization",
								title : "Analysis result",
								placement : 'bottom',
								content : "Results of the Expert analysis will be displayed here.</br>For zooming in/out use scroll. For changing time range use click&drop. </br> You can click on each block to get more details."
							},
							{
								element : "#raw",
								title : "Raw data",
								placement : 'top',
								content : "Raw data from DAQAggregator will be displayed here. </br>For zooming in/out use scroll. For changing time range use click&drop. Time range is always synchronized with Analysis result block above. </br> You can click at any point in time to get the full snapshot. "
							} ]
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
					<h4 class="modal-title">Event report</h4>
				</div>
				<div class="modal-body">
					<h4 id="raport-name">/Name/</h4>
					<p id="raport-description">/description/</p>

					<h4>Action</h4>
					<p id="raport-action">/action/</p>

					<h4>Details</h4>
					<pre id="raport-body"></pre>

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