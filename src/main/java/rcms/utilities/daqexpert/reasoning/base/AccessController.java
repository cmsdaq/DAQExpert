package rcms.utilities.daqexpert.reasoning.base;

import org.apache.log4j.Logger;
import rcms.utilities.daqexpert.processing.Requiring;

public class AccessController {

    Logger logger = Logger.getLogger(AccessController.class);

    public boolean grantAccess(Requiring calling, Requiring called){

        logger.info(calling.getNodeName() + " requests access to results of: " + calling.getNodeName());
        if(calling.getRequired().contains(called)){

            logger.info("Granting access to output of " + called.getNodeName() + " to " + calling.getNodeName());
            return true;
        } else {
            return false;
        }

    }
}
