var bucketSizeInMin = 5;

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

$(document).ready(
		function() {
			var containerId = 'histogram-container';
			var container = document.getElementById(containerId);
			console.log("Container: " + container);
			data = $('#' + containerId).data("histogram");
			console.log("Data: " + data);
			Highcharts.chart(container, {
				chart : {
					type : 'column'
				},
				title : {
					text : 'Run ongoing duration histogram'
				},
				xAxis : {
					gridLineWidth : 1,
					labels : {
						formatter : function() {
							return this.value + ' min';
						}
					},
				},
				tooltip : {
					formatter : function() {
						return '<b>' + this.y + '</b> occurrences of <b>'
								+ this.x + '-' + (this.x + bucketSizeInMin)
								+ ' min </b> events';
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
					data : histogram(data, 5),

					pointPadding : 0,
					groupPadding : 0,
					pointPlacement : 'between'
				} ]
			});
		});
