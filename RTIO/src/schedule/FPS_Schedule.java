package schedule;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import entity.Job;
import entity.PeriodicTask;
import javafx.util.Pair;
import utils.AnalysisUtils;

public class FPS_Schedule {

	public List<List<Double>> schedule(List<PeriodicTask> tasks) {

		Pair<List<Job>, Long> pair = new AnalysisUtils().getJobsInHyperPeriod(tasks);

		List<Job> jobsInHP = pair.getKey();

		List<Job> jobs = new ArrayList<Job>(jobsInHP);
		List<Job> exectued = new ArrayList<>();

		while (jobs.size() > 0) {
			long currentTime = getCurrentTime(jobs);
			List<Job> releasedJobs = getReleasedJobs(jobs, currentTime);

			Job j = releasedJobs.get(0);
			j.startTime = currentTime;

			exectued.add(j);
			jobs.remove(j);

		}

		assert (exectued.size() == jobsInHP.size());

		// check deadline miss
		for (int i = 0; i < exectued.size(); i++) {
			if (exectued.get(i).startTime + exectued.get(i).task.WCET > exectued.get(i).deadline) {
				return null;
			}
		}

		DecimalFormat df = new DecimalFormat("#.##");

		int numOfExact = 0;
		double totalValue = 0;

		for (int i = 0; i < exectued.size(); i++) {
			Job j = exectued.get(i);
			assert (j.startTime >= 0);
			if (j.delta == j.startTime)
				numOfExact++;
			totalValue += new AnalysisUtils().getValue(j);
		}

		double exact = Double.parseDouble(df.format((double) numOfExact / (double) exectued.size()));
		totalValue = (double) totalValue / (double) exectued.stream().mapToDouble(j -> j.task.Vmax).sum();
		totalValue = Double.parseDouble(df.format(totalValue));

		List<List<Double>> pfs = new ArrayList<>();
		List<Double> pf = new ArrayList<>();
		pf.add(exact);
		pf.add(totalValue);
		pfs.add(pf);

		return pfs;
	}

	private long getCurrentTime(List<Job> jobs) {
		long time = jobs.stream().mapToLong(j -> j.releaseTime).min().getAsLong();
		return time;
	}

	private List<Job> getReleasedJobs(List<Job> jobs, long time) {
		List<Job> released = new ArrayList<>();

		for (int i = 0; i < jobs.size(); i++) {
			if (jobs.get(i).releaseTime <= time)
				released.add(jobs.get(i));
		}

		released.sort((j1, j2) -> Integer.compare(j1.task.priority, j2.task.priority));

		return released;
	}

}
