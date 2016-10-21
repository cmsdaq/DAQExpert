/**
 * 
 */

var stepsDef = [
		{
			title : "Introduction",
			orphan : true,
			content : "This is DAQ Expert interactive visualization tool.</br>It visualizes results of analysis in time.</br>You can freely move and zoom in by dragging and scrolling in the timelines."
		},
		{
			element : "#visualization",
			title : "Analysis result",
			placement : 'bottom',
			content : "This is main analysis panel. Results and intermediate steps of reasoning are displayed here."
		},
		{
			title : "Elements",
			element : "#visualization",
			placement : 'bottom',
			content : function() {
				return '<p>Each row is visualizing one Logic Module results (or multiple Logic Modules if group name in bold). The number indicates how many events are visible in current time span. You can find details when you hover the label.</p><img src="external/expert-row.png" />';
			}
		},
		{
			title : "Element hiding",
			element : "#visualization",
			placement : 'bottom',
			content : function() {
				return '<p>When you zoom out elements will get smaller. For the clarity they will be hidden and replaced by shadow indicating how many elements are underneath.</p><img src="external/expert-filter-explain.png" />';
			}
		},
		{
			title : "Element color coding",
			element : "#visualization",
			placement : 'bottom',
			content : function() {
				return '<p>Elements are color coded. Red indicates that event is important and notification was generated. Blue indicates regular events.</p>';
			}
		},
		{
			title : "Element details",
			element : "#visualization",
			placement : 'bottom',
			content : function() {
				return '<p>Click on element to show details.</p><img src="external/details.png" />';
			}
		},
		{
			element : "#raw",
			title : "Raw data",
			placement : 'top',
			content : "This is raw data panel. Some parameters from snapshots are displayed here (avarage RU rate, sum of events in BU).</br>Time range is always synchronized with Analysis result timeline above."
		},
		{
			element : "#raw",
			title : "Raw data",
			placement : 'top',
			content : function() {
				return '<p>Click at any point in time to get the full snapshot in JSON format.</p><img src="external/snapshot.png" />';
			}
		}, {
			element : "#extended-view",
			title : "View toggle",
			placement : 'right',
			content : "Toggle between simple and extended view here."
		}, {
			element : "#tour",
			title : "Tour",
			placement : 'left',
			content : "You can always start this tour again here."
		} ];

// Instance the tour
var tour = new Tour({
	container : "body",
	name : "expert-tour",
	smartPlacement : true,
	placement : "left",
	keyboard : true,
	storage : window.localStorage,
	debug : false,
	backdrop : true,
	backdropContainer : 'body',
	backdropPadding : 0,
	redirect : true,
	orphan : false,
	duration : false,
	delay : false,
	steps : stepsDef
});

$(document).ready(function() {

	$('#tour').click(function(e) {
		// console.log("Start tour");

		tour.restart();

		// it's also good practice to preventDefault on the click event
		// to avoid the click triggering whatever is within href:
		e.preventDefault();
	});

	// console.log("initializing tour");
	// Initialize the tour
	tour.init();

	// Start the tour
	tour.start();

});