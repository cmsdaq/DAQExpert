package rcms.utilities.daqexpert.segmentation;

public class Point {

	public final long x;
	public final float y;

	public Point(long x, float y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return "(" + x + "," + y + ")";
	}
}