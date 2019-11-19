package schedule;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import entity.Job;
import entity.PeriodicTask;
import javafx.util.Pair;
import utils.AnalysisUtils;

public class GPIOCP {

	public List<List<Double>> schedule(List<PeriodicTask> tasks) {
		Pair<List<Job>, Long> pair = new AnalysisUtils().getJobsInHyperPeriod(tasks);

		List<Job> jobs = pair.getKey();

		jobs.sort((c1, c2) -> Long.compare(c1.delta, c2.delta));

		for (int i = 0; i < jobs.size(); i++) {
			jobs.get(i).startTime = jobs.get(i).delta;
		}

		for (int i = 1; i < jobs.size(); i++) {
			Job preJ = jobs.get(i - 1);
			Job job = jobs.get(i);

			if (preJ.startTime + preJ.task.WCET > job.startTime)
				job.startTime = preJ.startTime + preJ.task.WCET;
		}

		// check deadline miss
		for (int i = 0; i < jobs.size(); i++) {
			if (jobs.get(i).startTime + jobs.get(i).task.WCET > jobs.get(i).deadline) {
				return null;
			}
		}
		
		// check correctness
		for(int i=0; i<jobs.size();i++) {
			
		}

		// get fitness
		DecimalFormat df = new DecimalFormat("#.##");

		int numOfExact = 0;
		double totalValue = 0;

		for (int i = 0; i < jobs.size(); i++) {
			Job j = jobs.get(i);
			assert (j.startTime >= 0);
			if (j.delta == j.startTime)
				numOfExact++;
			totalValue += new AnalysisUtils().getValue(j);
		}

		double exact = Double.parseDouble(df.format((double) numOfExact / (double) jobs.size()));
		totalValue = (double) totalValue / (double) jobs.stream().mapToDouble(j -> j.task.Vmax).sum();
		totalValue = Double.parseDouble(df.format(totalValue));

		List<List<Double>> pfs = new ArrayList<>();
		List<Double> pf = new ArrayList<>();
		pf.add(exact);
		pf.add(totalValue);
		pfs.add(pf);

		return pfs;
	}

}
