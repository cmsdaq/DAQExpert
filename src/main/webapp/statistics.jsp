<!DOCTYPE HTML>
<%@page import="rcms.utilities.daqexpert.Setting"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
<script src="external/clipboard.min.js"></script>
<script src="external/highcharts.js"></script>

<!--  internal scripts -->
<script src="static/js/tour.js"></script>
<script src="static/js/share.js"></script>
<script src="static/js/statistics.js"></script>

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
		<p>
			Showing statistics for
			<fmt:formatDate pattern="yyyy-MM-dd'T'HH:mm:ss Z"
				value="${startdate}" />
			-
			<fmt:formatDate pattern="yyyy-MM-dd'T'HH:mm:ss Z" value="${enddate}" />
		</p>
		<p>${summary}</p>
		<div id="histogram-container" data-histogram="${downtimehistogram}"></div>
	</div>
</body>

</html>
