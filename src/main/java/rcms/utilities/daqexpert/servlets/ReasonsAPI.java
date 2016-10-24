package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.reasoning.base.Entry;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

public class ReasonsAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ReasonsAPI.class);

	int maxDuration = 1000000;
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String experimentalKey = request.getParameter("mode");

		/* requested range dates */
		String startRange = request.getParameter("start");
		String endRange = request.getParameter("end");
		logger.debug("Getting reasons from : " + startRange + " to " + endRange);

		/* parsed range dates */

		Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
		Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();
		logger.trace("Parsed range from : " + startDate + " to " + endDate);
		Map<String, Object> result = new HashMap<>();

		List<Entry> entryList = new ArrayList<>();

		Map<String, Set<Entry>> groupedMap = new HashMap<>();

		Map<String, Long> durations = new HashMap<>();

		/*
		 * minimum width for filtering the blocks is calculated based on this.
		 * This amount of sequential blocks of the same width is the threshold
		 */
		int elementsInRow = 100;

		/*
		 * Filter entries based on the duration and requested range
		 */
		long rangeInMs = endDate.getTime() - startDate.getTime();
		long durationThreshold = rangeInMs / elementsInRow;
		logger.debug("Duration thresshold: " + durationThreshold);

		Collection<Entry> allElements = null;
		if (experimentalKey == null || experimentalKey.equals("standard")) {
			logger.debug("API runs in standard mode");
			allElements = Application.get().getDataManager().getResult();
		} else {
			logger.debug("API runs in experimental mode: " + experimentalKey);
			allElements = Application.get().getDataManager().experimental.get(experimentalKey);
		}

		if (allElements != null) {

			logger.debug("There are " + allElements.size() + " in DataManager");
			synchronized (allElements) {

				for (Entry entry : allElements) {

					if (entry.getEventFinder().getGroup() != EventGroup.HIDDEN) {

						// this needs to be optimized
						try {
							if (entry.getStart().before(endDate) && entry.getEnd().after(startDate) && entry.isShow()) {

								if ((entry.getGroup() == EventGroup.LHC_BEAM.getCode()
										&& entry.getContent().equals("STABLE BEAMS"))
										|| entry.getGroup() == EventGroup.DOWNTIME.getCode()
										|| entry.getGroup() == EventGroup.AVOIDABLE_DOWNTIME.getCode()) {
									long current = 0;
									if (durations.containsKey(entry.getGroup())) {
										current = durations.get(entry.getGroup());
									}
									durations.put(entry.getGroup(), current + entry.getDuration());
								}

								if (entry.getDuration() > durationThreshold) {
									entryList.add(entry);
								} else {
									logger.debug("Entry " + entry.getId() + " with duration " + entry.getDuration()
											+ " will be filtered");
									if (!groupedMap.containsKey(entry.getGroup())) {
										groupedMap.put(entry.getGroup(), new HashSet<Entry>());
									}

									// find existing group to merge to
									Entry base = null;
									for (Entry potentialBase : groupedMap.get(entry.getGroup())) {

										if (potentialBase.getStart().getTime() - durationThreshold <= entry.getStart()
												.getTime()
												&& potentialBase.getEnd().getTime() + durationThreshold >= entry
														.getEnd().getTime()) {
											base = potentialBase;
											break;
										}
									}

									// create base from this if not found
									if (base == null) {
										base = new Entry(entry);
										base.setId(-base.getId());
										if (EventPriority.CRITICAL.getCode().equals(entry.getClassName())) {
											base.setClassName(EventPriority.FILTERED_IMPORTANT.getCode());
										} else {
											base.setClassName(EventPriority.FILTERED.getCode());
										}
										// type: 'background',
										base.setContent("1");
									}

									// merge
									else {
										if (base.getStart().after(entry.getStart()))
											base.setStart(entry.getStart());
										if (base.getEnd().before(entry.getEnd())) {
											base.setEnd(entry.getEnd());
										}
										base.calculateDuration();
										int filteredElements = Integer.parseInt(base.getContent());
										base.setContent((filteredElements + 1) + "");

										if (EventPriority.CRITICAL.getCode().equals(entry.getClassName())) {
											base.setClassName(EventPriority.FILTERED_IMPORTANT.getCode());
										}
									}

									groupedMap.get(entry.getGroup()).add(base);
								}
							}
						} catch (NullPointerException e) {
							// it means that some of reasons are being builed by
							// event
							// builder, just dont show them yet, they will be
							// ready
							// on
							// next
							// request
						}
					}

				}
			}
			for (Set<Entry> groupedEntries : groupedMap.values()) {

				for (Entry groupedEntry : groupedEntries) {
					if (groupedEntry.getDuration() < durationThreshold) {
						int append = (int) (durationThreshold - groupedEntry.getDuration());

						Date alteredStart = new Date(groupedEntry.getStart().getTime() - append / 2);
						Date alteredEnd = new Date(groupedEntry.getEnd().getTime() + append / 2);
						groupedEntry.setStart(alteredStart);
						groupedEntry.setEnd(alteredEnd);
						groupedEntry.calculateDuration();
					}
				}

				entryList.addAll(groupedEntries);

			}
		}else {
			logger.warn("There is no data for reasons API. It will return nothing.");
		}

		/*
		 * Remove these headers in production (only for accessing from external
		 * scripts)
		 */
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		/* necessary headers */
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		result.put("entries", entryList);
		result.put("durations", durations);
		/* return the response */
		String json = objectMapper.writeValueAsString(result);
		logger.debug("Response JSON: " + json);
		response.getWriter().write(json);

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}