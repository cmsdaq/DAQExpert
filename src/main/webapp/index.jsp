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
</style>

<script src="external/jquery.min.js"></script>
<script src="external/vis.min.js"></script>
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
			content : 'lhc'
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
		} ]);
		var timeline = new vis.Timeline(container, items, groups, options);
		


		/** Refresh data on timeline event */
		var runDataUpdateFromTimeline = function(properties) {
			var byUser = properties["byUser"];
			if(byUser){
				loadNewData('rangechange', properties);
			}
		};

        	/** Refresh views on timeline event */
		var runSyncFromTimeline = function(properties) {
			var start = properties["start"];
			var end = properties["end"];

			var byUser = properties["byUser"];
			if(byUser){
				graph2d.setWindow(start, end, {
					animation : false
				});
			}
			else{
				//here event propagation is stopped			
			}
		};
		

		/** Refresh data on graph event */
		var runDataUpdateFromGraph = function(properties) {
			var byUser = properties["byUser"];
			if(byUser){
				loadNewData('rangechange', properties);
			}
		};

		/** Refresh views on graph event */
		var runSyncFromGraph = function(properties) {
			var start = properties["start"];
			var end = properties["end"];

			var byUser = properties["byUser"];
			if(byUser){
				timeline.setWindow(start, end, {
					animation : false
				});
			}
			else{
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

			$('#reasonModal').modal('show')
			var parameters = {};
			parameters['id'] = properties['item'];
			$.getJSON("raport", parameters, function(data) {
				var preetified = JSON.stringify(data['elements'], null, 2);
				document.getElementById("raport-name").innerHTML = data['name'];
				document.getElementById("raport-description").innerHTML = data['description'];
				document.getElementById("raport-action").innerHTML = data['action'];
				document.getElementById("raport-body").innerHTML = preetified;
			}).error(function(jqXHR, textStatus, errorThrown) {
				console.log("error " + textStatus);
				console.log("errorThrown " + errorThrown);
				console.log("incoming Text " + jqXHR.responseText);
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
		var rawoptions = {};
		var graph2d = new vis.Graph2d(rawcontainer, rawdataset, rawoptions);
		
		graph2d.on('rangechange',  _.throttle(runDataUpdateFromGraph, 500,{leading: false}));
        graph2d.on('rangechange',  _.throttle(runSyncFromGraph, 50,{leading: false}));

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
					<p>Snapshot <span id="snapshotDate">/date/</span> in JSON format:</p>
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
					<p id= "raport-description">/description/</p>

					<h4 >Action</h4>
					<p id= "raport-action">/action/</p>

					<h4 >Details</h4>
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