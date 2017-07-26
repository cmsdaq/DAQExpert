<!DOCTYPE HTML>
<%@page import="rcms.utilities.daqexpert.Setting"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>

<head>
<title>DAQ Expert</title>

<!-- external scripts and stylesheets -->
<script src="external/jquery.min.js"></script>


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
<script src="external/clipboard.min.js"></script>
<script src="external/highcharts.js"></script>
<script type="text/javascript" src="external/deparam.min.js"></script>



<!-- datetime range picker -->
<script type="text/javascript" src="external/momentjs/moment.min.js"></script>
<script type="text/javascript"
	src="external/daterangepicker-2/daterangepicker.js"></script>
<link rel="stylesheet" type="text/css"
	href="external/daterangepicker-2/daterangepicker.css" />


<link rel="stylesheet" href="static/css/animation.css">

<!--  internal scripts -->
<script src="static/js/tour.js"></script>
<script src="static/js/share.js"></script>
<script src="static/js/statistics.js"></script>
<script src="static/js/statistics-histogram.js"></script>
<script src="static/js/statistics-pie-chart.js"></script>
<link rel="shortcut icon" href="">


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
				<li><a id="browser-link" href="index.jsp"
					data-url="${pageContext.request.requestURL}"><i
						class="glyphicon glyphicon-tasks"></i> Browser</a></li>


				<!-- NM NOTIFICATIONS -->
				<li><a
					href="<%out.println(Application.get().getProp(Setting.NM_ARCHIVE));%>"><i
						class="glyphicon glyphicon-calendar"></i> Archive</a></li>

				<!-- EXPERT STATISTICS -->
				<li class="active"><a id="reports-link" href="statistics"><i
						class="glyphicon glyphicon-tasks"></i> Reports</a></li>


			</ul>
		</div>
		<!-- /.navbar-collapse -->
	</nav>





	<div class="container">


		<div class="row" style="margin-bottom: 15px;">
			<div class="col-md-12">

				<span id="curr-params"></span>

				<form class="form-inline pull-right " method="POST">

					<div class="input-group" id="date-range-picker-group">
						<span class="input-group-addon">date range</span>
						<div id="reportrange" class=" btn btn-default">
							<i class="glyphicon glyphicon-calendar fa fa-calendar"></i> <span>
								last week (<span id="week-no"></span>): <span id="exact-date"></span>
							</span></span> <b class="caret"></b>
						</div>
					</div>
					<div class="btn-group ">
						<button class="btn btn-warning" id="tour-statistics" href="#">
							<i class="glyphicon glyphicon-question-sign"></i> Help
						</button>
					</div>

				</form>
			</div>
		</div>

		<div style="text-align: center">
			<div style="display: inline-block;">
				<div id="loader-animation" class="loader text-centered"></div>
			</div>
		</div>

		<div class="row">
			<div class="col-md-6" id="container-piechart-1"
				style="width: 550px; height: 400px; margin: 0 auto;"></div>
			<div class="col-md-6" id="container-piechart-2"
				style="width: 550px; height: 400px; margin: 0 auto;"></div>
		</div>

		<div class="row">
			<div class="col-md-6" id="container-piechart-3"
				style="width: 550px; height: 400px; margin: 0 auto;"></div>
			<div class="col-md-6" id="container-piechart-4"
				style="width: 550px; height: 400px; margin: 0 auto;"></div>
		</div>

		<div id="container-histogram-stable-beams"></div>
		<div id="container-histogram-run-ongoing"></div>
		<div id="container-histogram-nrwe"></div>

	</div>
</body>

</html>
