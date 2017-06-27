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
		});
