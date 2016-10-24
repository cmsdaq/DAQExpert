/**
 * 
 */

$(document).ready(function() {
	$('#run-experimental-lm-button').click(function(e) {
		requestListOfExperimentalLM();
		params = getWindowInformationForAPI();
		/*
		 * console.log("Experiment requested with parameters " +
		 * JSON.stringify(params));
		 */

		var start = moment(params['start']);
		var end = moment(params['end']);
		$('#experimental-time-span-start').html(start.format());
		$('#experimental-time-span-end').html(end.format());

		var diff = end.diff(start);
		$('#processing-warning0-message').hide();
		$('#processing-warning1-message').hide();
		$('#processing-error-message').hide();
		$('#processing-error-stacktrace').hide();
		$('#experimental-run-process-button').attr("disabled", false);
		if (diff > 1 * 60 * 60 * 1000) {
			$('#processing-warning0-message').show();
			$('#experimental-run-process-button').attr("disabled", true);
		} else if (diff > 30 * 60 * 1000) {
			$('#processing-warning1-message').show();
		}

		var duration = moment.duration(diff);

		console.log("Duration " + duration);
		$('.duration-in-message').html(duration.humanize());
		$('#duration-humanized').html(duration.humanize());

		$('#experimental-run-popup').modal('show')

	});
	$('#experimental-run-process-button').click(function(e) {
		// console.log("Run experimental logic modules!");
		requestRunExperimentalLM();

		// $('#experimental-run-popup').modal('hide')
	});

	$('#experimental-load-button').click(function(e) {
		// console.log("Load experimental logic modules!");
		var selected = $('#elm-select').find(":selected").text();
		// console.log("selected now " + selected);
		mode = selected;
		lastExperimentalMode = selected;
		params = getWindowInformationForAPI();
		getData(params['start'], params['end'], mode);
		$('#experimental-run-popup').modal('hide')

	});

});

var requestRunExperimentalLM = function() {
	params = getWindowInformationForAPI();
	var selected = $('#elm-select').find(":selected").text();
	// console.log("selected " + selected);
	params['experimental-lm'] = selected;
	mode = selected;
	lastExperimentalMode = selected;
	$('#loader-animation').show();
	$.getJSON("experiment", params, function(data) {
		console.log("Successfull call " + data);
		$('#loader-animation').hide();
		if (data['status'] == "success") {
			// console.log("Run without errors");
			$('#experimental-run-popup').modal('hide')
			getData(params['start'], params['end'], mode);
		} else {
			// console.log("Run with errors");
			// console.log("Error message: " + data['message']);
			$('#processing-error-stacktrace').html(data['message']);
			$('#processing-error-message').show();
			$('#processing-error-stacktrace').show();
		}

	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
		console.log("errorThrown " + errorThrown);
		console.log("incoming Text " + jqXHR.responseText);
	});
};

var requestListOfExperimentalLM = function() {
	$.getJSON("scripts", params, function(data) {
		// console.log("Successfull call " + data);

		$('#experimental-dir').text(data['directory']);
		$("#elm-select").empty();
		$.each(data['names'], function(index, lm) {
			// console.log("base: "+ lm);
			$("<option>").appendTo($('#elm-select')).text(lm)
		});

	}).error(function(jqXHR, textStatus, errorThrown) {
		console.log("error " + textStatus);
		console.log("errorThrown " + errorThrown);
		console.log("incoming Text " + jqXHR.responseText);
	});
};

var refreshBackgroundColor = (function(color) {
	$.each($('.vis-timeline'), function(key, value) {
		console.log(value);
		value.style.border = color;
	});
});
