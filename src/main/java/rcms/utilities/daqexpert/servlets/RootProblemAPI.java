package rcms.utilities.daqexpert.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.Application;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/**
 * Retrieves the root problem for given timeframe
 */
public class RootProblemAPI extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RootProblemAPI.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        /* requested range dates */
        String startRange = request.getParameter("start");
        String endRange = request.getParameter("end");

        /* parsed range dates */
        try {
            Date startDate = DatatypeConverter.parseDateTime(startRange).getTime();
            Date endDate = DatatypeConverter.parseDateTime(endRange).getTime();

            Collection<ConditionDetailedDTO> entries = Application.get().getPersistenceManager().
                    getRootProblemEntries(startDate, endDate);

            entries.stream().forEach(c->c.setDescription(c.getDescription()
                                                                 .replace("<sub>","")
                                                                 .replace("</sub>","")

                                                                 .replace("<sup>","")
                                                                 .replace("</sup>","")

                                                                 .replace("<strong>","")
                                                                 .replace("</strong>","")
            ));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = objectMapper.writeValueAsString(entries);
            logger.debug("Response JSON: " + json);
            response.getWriter().write(json);
        } catch (NullPointerException | IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }


    }
}