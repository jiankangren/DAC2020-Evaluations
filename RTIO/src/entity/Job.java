package entity;

import java.util.ArrayList;
import java.util.List;

public class Job {

	public PeriodicTask task;

	public long startTime = -1;
	public double qulaity = -1;

	public long releaseTime;
	public long deadline;
	public long delta;
	public long idealFinish;

	public int numberOfRelease;

	public List<Job> interferingJobs = new ArrayList<>();

	public List<Space> allocatableSpace = new ArrayList<>();

	public List<Space> spaceInRange = new ArrayList<>();

	public Job(int numberOfRelease, PeriodicTask task) {
		this.numberOfRelease = numberOfRelease;
		this.releaseTime = task.period * numberOfRelease;
		this.deadline = releaseTime + task.deadline;
		this.delta = releaseTime + task.delta;
		this.idealFinish = this.delta + task.WCET;

		this.task = task;
	}

	@Override
	public String toString() {
		return "ID: " + task.id + "_" + numberOfRelease + ", releaseTime: " + releaseTime + ", deadline: " + deadline + ", delta: " + delta + ", idealFinish: "
				+ idealFinish;
	}

}