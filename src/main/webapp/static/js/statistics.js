/**
 * Get histogram data out of xy data
 * 
 * @param {Array}
 *            data Array of tuples [x, y]
 * @param {Number}
 *            step Resolution for the histogram
 * @returns {Array} Histogram data
 */
function histogram(data, step) {
	var histo = {}, x, i, arr = [];

	// Group down
	for (i = 0; i < data.length; i++) {
		x = Math.floor(data[i] / step) * step;
		if (!histo[x]) {
			histo[x] = 0;
		}
		histo[x]++;
	}

	// Make the histo group into an array
	for (x in histo) {
		if (histo.hasOwnProperty((x))) {
			arr.push([ parseFloat(x), histo[x] ]);
		}
	}

	// Finally, sort the array
	arr.sort(function(a, b) {
		return a[0] - b[0];
	});

	return arr;
}

function buildChart(containerId, title, step, factor, unit, max) {
	var container = document.getElementById(containerId);
	console.log("Container: " + container);
	data = $('#' + containerId).data("histogram");
	var dataNormalized = [];
	$(data).each(function(index) {
		if (this / factor < max) {
			dataNormalized.push(this / factor);
		}
	});
	console.log("Data: " + dataNormalized);
	Highcharts.chart(container, {
		chart : {
			type : 'column'
		},
		title : {
			text : title
		},
		xAxis : {
			gridLineWidth : 1,
			labels : {
				formatter : function() {
					return this.value + ' ' + unit;
				}
			},
		},
		tooltip : {
			formatter : function() {
				return '<b>' + this.y + '</b> occurrences of <b>' + this.x
						+ '-' + (this.x + step) + ' ' + unit + ' </b> events';
			}
		},
		yAxis : [ {
			title : {
				text : 'Count'
			}
		} ],

		series : [ {
			name : 'Duration',
			type : 'column',
			data : histogram(dataNormalized, step),

			pointPadding : 0,
			groupPadding : 0,
			pointPlacement : 'between'
		} ]
	});

}

$(document).ready(
		function() {
			buildChart('container-histogram-stable-beams',
					'Stable beams histogram', 2, 60 * 60, 'h', 50);
			buildChart('container-histogram-nrwe',
					'No rate when expected histogram', 5, 1, 'sec', 500);
			buildChart('container-histogram-run-ongoing',
					'Run ongoing duration histogram', 5, 60, 'min', 200);

			initDatePicker();
		});


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

	queryParameters['page'] = 1; // reset the page
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

	}

	$('#reportrange').on('apply.daterangepicker', function(ev, picker) {

		location.search = $.param(queryParameters);
	});

}
