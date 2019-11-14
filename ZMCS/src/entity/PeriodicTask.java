package entity;

public class PeriodicTask {
	public int id;
	public long period;
	public long deadline;
	public int priority;
	public long WCET;
	public double util;

	public long delta;
	public double quality;

	public long start;

	public long Ri = 0;

	public PeriodicTask(int p, long t, long d, long c, int id, int detla, int quality) {
		this(p, t, d, c, id, detla, quality, -1);
	}

	public PeriodicTask(int p, long t, long d, long c, int id, int detla, int quality, double util) {
		this.priority = p;
		this.period = t;
		this.WCET = c;
		this.deadline = d;
		this.id = id;
		this.util = util;

		this.delta = detla;
		this.quality = quality;

		start = 0;
		Ri = 0;
	}

}
