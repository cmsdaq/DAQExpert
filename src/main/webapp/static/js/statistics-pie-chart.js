function buildPieCharts (data) {

	console.log("Building pie-charts:");
    buildGraph('container-piechart-1', 'Efficiency during stable beams',data['piechart1']);
    buildGraph('container-piechart-2', 'Time spent during downtime',data['piechart2']);
    buildGraph('container-piechart-3', 'Problems causing downtime',data['piechart3']);
    buildGraph('container-piechart-4', 'Subsystems causing downtime',data['piechart4']);
	console.log("Built pie-charts.");
    

}

function buildGraph(containerId, title,data) {

	console.log("Data loaded :" + JSON.stringify(data));
	
	if(data.length > 0){  
	
	
	    var chartDef = {
	        chart: {
	            plotBackgroundColor: null,
	            plotBorderWidth: null,
	            plotShadow: false,
	            type: 'pie'
	        },
	        exporting: {
	            chartOptions: { // specific options for the exported image
	                plotOptions: {
	                    series: {
	                        dataLabels: {
	                            enabled: true
	                        }
	                    }
	                }
	            },
	            fallbackToExportServer: false
	        },
	        title: {
	            text: title
	        },
	        tooltip: {
	            pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
	        },
	        plotOptions: {
	            pie: {
	                allowPointSelect: true,
	                cursor: 'pointer',
	                dataLabels: {
	                    enabled: true,
	                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
	                    style: {
	                        color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
	                    }
	                }
	            }
	        },
	        series: [{
	            name: '% of time stable beams',
	            colorByPoint: true,
	            data: data
	        }]
	    };

    $('#'+containerId).highcharts(chartDef);
	} else{
		$('#'+containerId).html("No data available in selected period to generate '" +title+ "' chart");
	}

}