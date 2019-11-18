package entity;

public class PeriodicTask {
	public int id;
	
	public long period;
	public long deadline;
	public int priority;
	
	private long[] WCET = new long[2];
	public double util;
	
	public int criticaility;
	
	public PeriodicTask dependencyTask = null;

	public long Ri = 0;

	public PeriodicTask(int p, long t, long d, long c, int l, int id) {
		this(p, t, d, c, l, id, -1);
	}

	public PeriodicTask(int p, long t, long d, long c, int l, int id, double util) {
		this.priority = p;
		this.period = t;
		this.WCET[0] = c;
		this.WCET[1] = c * 2;
		this.deadline = d;
		this.id = id;
		this.util = util;
		
		this.criticaility = l;
		
		Ri = 0;
	}

	public long getWCET(int mode) {
		if(mode > criticaility )
			return WCET[0];
		else
			return WCET[mode];
	}
}
