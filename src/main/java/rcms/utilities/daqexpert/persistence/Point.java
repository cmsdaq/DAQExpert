package rcms.utilities.daqexpert.persistence;

public class Point {

	public final int group;
	public final int resolution;
	public final long x;
	public final float y;

	public Point(long x, float y, int group, int resolution) {
		this.x = x;
		this.y = y;
		this.group = group;
		this.resolution = resolution;
	}

	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public int getGroup() {
		return group;
	}

	public int getResolution() {
		return resolution;
	}

	public long getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}