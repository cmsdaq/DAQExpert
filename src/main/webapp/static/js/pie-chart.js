$(document).ready(function () {

	console.log("Building pie-charts:");
    buildGraph('container-piechart-1', 'Problems causing downtime');
    buildGraph('container-piechart-2', 'Subsystems causing downtime');
	console.log("Built pie-charts.");
    

});

function buildGraph(containerId, title) {


	var data = $('#' + containerId).data("piechart");
	console.log("Data loaded :" + JSON.stringify(data));
	//data = JSON.stringify(data);
	
	
    var chartDef = {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
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

}