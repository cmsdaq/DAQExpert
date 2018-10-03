package rcms.utilities.daqexpert.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.mockito.internal.matchers.Null;
import rcms.utilities.daqexpert.Application;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class ConditionsAPI extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ConditionsAPI.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{


        /* requested range dates */
        String startRange = request.getParameter("start");
        String endRange = request.getParameter("end");
        String[] typeInput = request.getParameterValues("type");




        /* parsed range dates */
        try {
            Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
            Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();


            logger.info("Requested entries by type " + Arrays.asList(typeInput));

            Collection<LogicModuleRegistry> types = new ArrayList<>();
            for (String typeName : typeInput) {
                LogicModuleRegistry type = LogicModuleRegistry.searchEnum(typeName);
                if (type != null) {
                    types.add(type);
                }
            }
            Collection<ConditionDTO> entries = Application.get().getPersistenceManager().
                    getSpecificEntries(startDate, endDate, types);

            /* necessary headers */
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");


            /* return the response */
            String json = objectMapper.writeValueAsString(entries);
            logger.debug("Response JSON: " + json);
            response.getWriter().write(json);
        } catch (NullPointerException | IllegalArgumentException  e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }


    }
}