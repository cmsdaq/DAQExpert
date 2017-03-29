/**
 * Script for generating links to share expert browser view
 */

$(document).ready(
		function() {
			
			var shareLinkBase = null;
			var startKey = "?start=";
			var endKey = "&end=";
			var startValue = null;
			var endValue = null;

			var getLink = function() {
				
				var oldURL = $(location).attr('href');
				var index = 0;
				var newURL = oldURL;
				index = oldURL.indexOf('?');
				if(index == -1){
				    index = oldURL.indexOf('#');
				}
				if(index != -1){
				    newURL = oldURL.substring(0, index);
				}
				shareLinkBase = newURL;
				
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