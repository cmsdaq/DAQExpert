package rcms.utilities.daqexpert.processing;

import org.apache.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Finds the order of running the Logic Modules satisfying their 'require' declarations.
 */
public class OrderManager <T extends Requiring> {

    public static final Logger logger = Logger.getLogger(OrderManager.class);

    /**
     * Finds the running order based on given elements and elements that are already ordered. This method utilizes
     * recursive algorithm.
     * <p>
     * Exception will be thrown when cyclic dependency is detected.
     *
     * @param toOrder elements to order
     * @return ordered elements
     */
    public List<T> order(Set<T> toOrder) {
        return order(toOrder, new LinkedList<>());
    }

    private List<T> order(Set<T> toOrder, List<T> ordered) {
        logger.debug("Running with " + toOrder.size() + " and " + ordered.size());

        int toOrderSize = toOrder.size();
        int orderedSize = ordered.size();

        if (toOrder == null || toOrder.size() == 0) {
            logger.debug("Returning empty");
            return ordered;
        } else {
            List<T> toOrderThisRound = new LinkedList<>();
            Set<T> toWait = new LinkedHashSet<>();
            for (T lmToCheck : toOrder) {
                if (lmToCheck.getRequired().size() == 0 || ordered.containsAll(lmToCheck.getRequired())) {
                    toOrderThisRound.add(lmToCheck);

                } else {
                    toWait.add(lmToCheck);
                }
            }
            ordered.addAll(toOrderThisRound);

            if (toWait.size() == toOrderSize && ordered.size() == orderedSize) {
                throw new RuntimeException("Cyclic dependency detected for elements: " + toOrder);
            }

            List<T> orderedPart = order(toWait, ordered);

            logger.debug("Returning " + ordered);
            return orderedPart;
        }

    }

}
