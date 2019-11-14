package entity;

public class PeriodicTask {
	public int id;
	public long period;
	public long deadline;
	public int priority;
	public long WCET;
	public double util;

	public long delta;
	public long theta;

	public double Vmax;
	public double Vmin;

	public long start = 0;
	public long Ri = 0;

	public PeriodicTask(int p, long t, long d, long c, int id, long detla, long theta, int Vmax, int Vmin) {
		this(p, t, d, c, id, detla, theta, Vmax, Vmin, -1);
	}

	public PeriodicTask(int p, long t, long d, long c, int id, long detla, long theta, int Vmax, int Vmin, double util) {
		this.priority = p;
		this.period = t;
		this.WCET = c;
		this.deadline = d;
		this.id = id;
		this.util = util;

		this.delta = detla;
		this.theta = theta;

		this.Vmax = Vmax;
		this.Vmin = Vmin;
	}

	@Override
	public String toString() {
		return "ID: " + id + ", WCET: " + WCET + ", priority: " + priority;
	}

}
