
var defaultRange;

$(document).ready(
		function() {

			$('#loader-animation').show();
			defaultRange = [
				moment().subtract(1, 'isoWeek').startOf('isoWeek'),
				moment().subtract(1, 'isoWeek').endOf('isoWeek')];
			initDatePicker();
			
			console.log("Getting data");
			getData();
		});


function getData(){
	$.getJSON("stats", queryParameters, function(data) {
		buildHistograms(data);
		buildPieCharts(data);
		$('#loader-animation').hide();
	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
		console.log("incoming Text " + jqXHR.responseText);
		$('#loader-animation').hide();
	});
}
/*
 * queryParameters -> handles the query string parameters
 * queryString -> the query string without the fist '?' character
 * re -> the regular expression
 * m -> holds the string matching the regular expression
 */
var queryParameters = {}, queryString = location.search.substring(1);
queryParameters = $.deparam(queryString);

//refresh main form with choosen date range
function cb(start, end) {
	$('#reportrange span').html(
			start.format('YYYY-MM-DD HH:mm') + ' - '
					+ end.format('YYYY-MM-DD HH:mm'));

	queryParameters['start'] = start.format();
	queryParameters['end'] = end.format();
}


function initDatePicker() {
	// init date range
	$('#reportrange').daterangepicker(
			{
				timePicker : true,
				timePickerIncrement : 10, // for timepicker minutes
				timePicker24Hour : true,
				locale : {
					format : 'YYYY-MM-DD HH:mm' // iso 8601
				},
				opens : 'left',
				ranges : {
					'Yesterday' : [ moment().startOf('day'),
							moment().endOf('day') ],
					'Last Hour' : [ moment().subtract(1, 'hours'), moment() ],
					'Last 24h' : [ moment().subtract(1, 'days'), moment() ],
					'Yesterday' : [
							moment().subtract(1, 'days').startOf('day'),
							moment().subtract(1, 'days').endOf('day') ],
					'Last Week' : defaultRange,
					'Last 7 Days' : [
							moment().subtract(6, 'days').startOf('day'),
							moment().endOf('day') ],
					'Last 30 Days' : [
							moment().subtract(29, 'days').startOf('day'),
							moment().endOf('day') ],
					'This Month' : [ moment().startOf('month'),
							moment().endOf('month') ],
					'Last Month' : [
							moment().subtract(1, 'month').startOf('month'),
							moment().subtract(1, 'month').endOf('month') ],
					'Last 12 Months' : [
							moment().subtract(11, 'month').startOf('month'),
							moment().endOf('month') ]
				}
			}, cb);

	if (queryParameters['start']) {
		$('#reportrange span').html(
				moment(queryParameters['start']).format('YYYY-MM-DD HH:mm')
						+ ' - '
						+ moment(queryParameters['end']).format(
								'YYYY-MM-DD HH:mm'));

	} else{
		queryParameters['start'] = defaultRange[0].format();
		queryParameters['end'] = defaultRange[1].format();
		location.search = $.param(queryParameters);
	}

	$('#reportrange').on('apply.daterangepicker', function(ev, picker) {

		location.search = $.param(queryParameters);
	});

}
