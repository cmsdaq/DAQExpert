package rcms.utilities.daqexpert.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SortedArrayList<T> extends ArrayList<T> {

	@SuppressWarnings("unchecked")
	public boolean add(T value) {
		super.add(value);
		Comparable<T> cmp = (Comparable<T>) value;
		for (int i = size() - 1; i > 0 && cmp.compareTo(get(i - 1)) < 0; i--)
			Collections.swap(this, i, i - 1);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T value : c) {
			this.add(value);
		}
		return true;
	}
}
