<!DOCTYPE HTML>
<%@page import="rcms.utilities.daqexpert.Setting"%>
<html>

<head>
<title>DAQ Expert</title>

<!-- external scripts and stylesheets -->
<script src="external/jquery.min.js"></script>
<script src="external/vis.external.js"></script>
<link href="external/vis.min.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet"
	href="external/bootstrap-3.3.7-dist/css/bootstrap.min.css">
<link rel="stylesheet"
	href="external/bootstrap-3.3.7-dist/css/bootstrap-theme.min.css">
<script src="external/bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
<script src="external/underscore-min.js"></script>
<script type="text/javascript" src="external/moment.min.js"></script>
<link href="external/bootstrap-tour.min.css" rel="stylesheet">
<script src="external/bootstrap-tour.min.js"></script>
<script src="external/moment-duration-format.min.js"></script>


<!--  internal scripts -->
<script src="static/js/basic.js"></script>
<script src="static/js/raw-graph.js"></script>
<script src="static/js/analysis-graph.js"></script>
<script src="static/js/tour.js"></script>
<script src="static/js/experimental.js"></script>
<script src="static/js/run-info-link.js"></script>
<script src="static/js/wbm-link.js"></script>

<!--  internal stylesheets -->
<link rel="stylesheet" href="static/css/experimental.css">
<link rel="stylesheet" href="static/css/graphs.css">



</head>

<body>
	<%@  page import="rcms.utilities.daqexpert.Application"%>
	<%@  page import="rcms.utilities.daqexpert.Setting"%>

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
				href="<%out.println(Application.get().getProp(Setting.LANDING));%>"><b>DAQ</b>
				Expert</a>
		</div>



		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse"
			id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav">


				<!-- NM DASHBOARD -->
				<li><a
					href="<%out.println(Application.get().getProp(Setting.NM_DASHBOARD));%>"><i
						class="glyphicon glyphicon-bell"></i> Dashboard</a></li>
						
				<!-- EXPERT BROWSER -->
				<li class="active"><a href="#"><i
						class="glyphicon glyphicon-tasks"></i> Browser</a></li>


				<!-- NM NOTIFICATIONS -->
				<li><a
					href="<%out.println(Application.get().getProp(Setting.NM_ARCHIVE));%>"><i
						class="glyphicon glyphicon-calendar"></i> Archive</a></li>

			</ul>
		</div>
		<!-- /.navbar-collapse -->
	</nav>



	<div class="container">

		<!-- <div id="guide-available-message" class="alert alert-success"
			role="alert">
			<button type="button" class="close" data-dismiss="alert">×</button>
			<strong>Experimental mode now available!</strong> Learn more about
			experimental mode in this <a
				href="http://daq-expert.cern.ch/logic-contributing.html">step by
				step guide</a>.
		</div> -->

		<div class="btn-group btn-toggle" id="extended-view">
			<button id="mode1" type="button"
				class="mode-btn btn btn-sm btn-default btn-primary">Simple</button>
			<button id="mode2" type="button"
				class="mode-btn btn btn-sm btn-default">Extended</button>
			<button id="mode3" type="button"
				class="mode-btn btn btn-sm btn-default">
				LMFactory <span><sup><b>beta</b></sup></span>
			</button>
		</div>

		<div class="btn-group pull-right ">

			<div class="btn-group" role="group" aria-label="...">
				<div class="btn-group" role="group">
					<button type="button"
						class="btn btn-default btn-sm dropdown-toggle"
						data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
						External Tools <span class="caret"></span>
					</button>
					<ul class="dropdown-menu">
						<li><a id="run-info-button" target="_blank"> RunInfo <span
								class="glyphicon glyphicon-chevron-right"></span>
						</a></li>
						<li><a id="wbm-button" target="_blank"> WBM run summary <span
								class="glyphicon glyphicon-chevron-right"></span>
						</a></li>
					</ul>
				</div>
				<button type="button" class="btn btn-sm btn-info"
					id="run-experimental-lm-button" href="#" style="display: none;">
					Run experimental LMs <i class="glyphicon glyphicon-play"></i>
				</button>

				<button type="button" class="btn btn-sm btn-warning" id="tour"
					href="#">
					Help <i class="glyphicon glyphicon-question-sign"></i>
				</button>
			</div>
		</div>

		<div id="visualization" style="margin-top: 15px;"></div>
		<div id="raw" style="margin-top: 5px;"></div>
		<p></p>
		<div id="log"></div>
	</div>

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
					<p>Duration: <span id="raport-duration" class= "badge">/duration/</span> <small class="text-muted" id="raport-duration-humanized">aa</small></p>


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

	<div id="runinfo-popup" class="modal fade" tabindex="-1" role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">Go to run info</h4>
				</div>
				<div class="modal-body">
					<p>Session id ambiguous. Please set the time span so that there
						is exactly one session id.</p>
				</div>
				<div class="modal-footer"></div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->

	<div id="wbm-popup" class="modal fade" tabindex="-1" role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
					<h4 class="modal-title">Go to WBM</h4>
				</div>
				<div class="modal-body">
					<p>Run number ambiguous. Please set the time span so that there
						is exactly one run number.</p>
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
