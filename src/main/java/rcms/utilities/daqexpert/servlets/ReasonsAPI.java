package rcms.utilities.daqexpert.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.*;

public class ReasonsAPI extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ReasonsAPI.class);

    int maxDuration = 1000000;
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        Date fakeEnd = Application.get().getDataManager().getLastUpdate();

        /* requested range dates */
        String startRange = request.getParameter("start");
        String endRange = request.getParameter("end");
        logger.debug("Getting reasons from : " + startRange + " to " + endRange);

        /* parsed range dates */
        Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
        Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();

        logger.trace("Parsed range from : " + startDate + " to " + endDate);

        Collection<Condition> allElements = Application.get().getPersistenceManager().getEntriesWithMask(startDate, endDate);

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


        Map<String, Object> result = new HashMap<>();
        result.put("entries", allElements);
        result.put("fake-end", fakeEnd);
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