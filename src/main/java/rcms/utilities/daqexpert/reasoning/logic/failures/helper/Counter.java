package rcms.utilities.daqexpert.reasoning.logic.failures.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for counting equal objects, similar to python's collections.Counter class
 *  */
public class Counter<T> {

	private final Map<T, Integer> counts = new HashMap<>();

  /** increase count of item by one */
	public void add(T item) {
		counts.put(item, counts.getOrDefault(item, 0) + 1);
	}

	public int getCount(T item) {

    return counts.getOrDefault(item, 0);

	}

	public boolean isEmpty() {
		return counts.isEmpty();
	}

	/** @return the element with the maximum count (if there are multiple
	 *  with the same count, an arbitrary element is returned). If the counter
	 *  is empty, null is returned.
	 */
	public Map.Entry<T, Integer> getMaximumEntry() {

		Map.Entry<T, Integer> result = null;

		for (Map.Entry<T, Integer> entry : counts.entrySet()) {

			if (result == null || entry.getValue() > result.getValue()) {
				result = entry;
			}
		}

		return result;
	}

	/** @return the elements sorted in decreasing order of counts */
	public List<Map.Entry<T, Integer> > getSortedCounts() {

		List<Map.Entry<T, Integer> > result = new ArrayList<>(counts.entrySet());

		Collections.sort(result, new Comparator<Map.Entry<T,Integer>>() {

			@Override
			public int compare(Map.Entry<T, Integer> e1, Map.Entry<T, Integer> e2)
			{
				// note the minus in front to have sorting in decreasing order
				// of counts

				return - (e1.getValue() - e2.getValue());
			}

		});

		return result;

	}

}
