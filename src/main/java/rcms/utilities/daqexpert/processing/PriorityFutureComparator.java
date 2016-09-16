package rcms.utilities.daqexpert.processing;

import java.util.Comparator;

/**
 * Comparator for priority jobs
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class PriorityFutureComparator implements Comparator<Runnable> {
	public int compare(Runnable o1, Runnable o2) {
		if (o1 == null && o2 == null)
			return 0;
		else if (o1 == null)
			return -1;
		else if (o2 == null)
			return 1;
		else {
			int p1 = ((PriorityFuture<?>) o1).getPriority();
			int p2 = ((PriorityFuture<?>) o2).getPriority();

			return p1 > p2 ? 1 : (p1 == p2 ? 0 : -1);
		}
	}
}