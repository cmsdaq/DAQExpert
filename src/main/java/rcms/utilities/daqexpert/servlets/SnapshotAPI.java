package rcms.utilities.daqexpert.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqexpert.ExpertPersistorManager;
import rcms.utilities.daqexpert.servlets.mixin.BUMixIn;
import rcms.utilities.daqexpert.servlets.mixin.BUSummaryMixIn;
import rcms.utilities.daqexpert.servlets.mixin.DAQMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FEDBuilderMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FEDBuilderSummaryMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FEDMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FMMApplicationMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FMMMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FRLMixIn;
import rcms.utilities.daqexpert.servlets.mixin.FRLPcMixIn;
import rcms.utilities.daqexpert.servlets.mixin.RUMixIn;
import rcms.utilities.daqexpert.servlets.mixin.SubFEDBuilderMixIn;
import rcms.utilities.daqexpert.servlets.mixin.SubSystemMixIn;
import rcms.utilities.daqexpert.servlets.mixin.TTCPartitionMixIn;

public class SnapshotAPI extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SnapshotAPI.class);

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String time = request.getParameter("time");
		logger.info("Requested snapshot date: " + time);
		Date timeDate = objectMapper.readValue(time, Date.class);
		logger.info("Parsed requested snapshot date: " + timeDate);

		DAQ result = ExpertPersistorManager.get().findSnapshot(timeDate);

		// Mixin for adding 'ref_' prefix to fields for details see
		// http://www.leveluplunch.com/java/tutorials/024-modifying-fields-external-domain-jackson-mixin/
		objectMapper.addMixIn(BU.class, BUMixIn.class);
		objectMapper.addMixIn(BUSummary.class, BUSummaryMixIn.class);
		objectMapper.addMixIn(DAQ.class, DAQMixIn.class);
		objectMapper.addMixIn(FED.class, FEDMixIn.class);
		objectMapper.addMixIn(FEDBuilder.class, FEDBuilderMixIn.class);
		objectMapper.addMixIn(FEDBuilderSummary.class, FEDBuilderSummaryMixIn.class);
		objectMapper.addMixIn(FMM.class, FMMMixIn.class);
		objectMapper.addMixIn(FMMApplication.class, FMMApplicationMixIn.class);
		objectMapper.addMixIn(FRL.class, FRLMixIn.class);
		objectMapper.addMixIn(FRLPc.class, FRLPcMixIn.class);
		objectMapper.addMixIn(RU.class, RUMixIn.class);
		objectMapper.addMixIn(SubFEDBuilder.class, SubFEDBuilderMixIn.class);
		objectMapper.addMixIn(SubSystem.class, SubSystemMixIn.class);
		objectMapper.addMixIn(TTCPartition.class, TTCPartitionMixIn.class);

		String json = objectMapper.writeValueAsString(result);
		// TODO: externalize the Allow-Origin
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		response.addHeader("Access-Control-Max-Age", "1728000");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

		logger.info(
				"Found snapshot with timestamp: " + new Date(result.getLastUpdate()) + ": " + json.substring(0, 1000));

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}