/**
 * Script for generating links to share expert browser view
 */

$(document).ready(
		function() {

			var shareLinkBase = $('#browser-link').data('url');
			var startKey = "?start=";
			var endKey = "&end=";
			var startValue = null;
			var endValue = null;

			var getLink = function() {
				p = getWindowInformationForAPI();
				startValue = new Date(p['start']).toISOString();
				endValue = new Date(p['end']).toISOString();
				return shareLinkBase + startKey + startValue + endKey
						+ endValue;
			}
			
			var clipboard = new Clipboard('.btn-copy', {
				text : function() {
					return getLink();
				}
			});

			

			clipboard.on('success', function(e) {
				setTooltip(e.trigger, 'Link copied!');
				hideTooltip(e.trigger);
			});

			clipboard.on('error', function(e) {
				setTooltip(e.trigger, 'Failed!');
				hideTooltip(e.trigger);
			});

			$('#share-button').tooltip({
				trigger : 'click',
				placement : 'left'
			});

			function setTooltip(btn, message) {
				$(btn).tooltip('hide').attr('data-original-title', message)
						.tooltip('show');
			}

			function hideTooltip(btn) {
				setTimeout(function() {
					$(btn).tooltip('hide');
				}, 1000);
			}

		});