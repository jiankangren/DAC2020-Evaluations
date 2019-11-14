package evaluation;

import java.util.List;
import java.util.stream.Collectors;

import entity.Job;
import entity.PeriodicTask;
import generationTools.SimpleSystemGenerator;
import javafx.util.Pair;
import schedule.StaticIOSchedule;
import utils.AnalysisUtils;

public class compareAllMethods {

	static int minT = 1;
	static int maxT = 20;
	static int totalTasks = 15;
	static double totalUtil = 0.7;
	static double valueRange = 0.5;
	static int LCM = 720;

	static boolean isPeriodLogUni = true;
	static int seed = 10;

	public static void main(String args[]) {
		seed = 146;
		System.out.println("seed: " +seed);
		SimpleSystemGenerator generator = new SimpleSystemGenerator(minT, maxT, LCM, totalTasks, totalUtil, isPeriodLogUni, valueRange, seed, true);

		List<PeriodicTask> tasks = generator.generateTasks();

		System.out.println("task generated");
		System.out.println("Period: " + tasks.stream().map(t -> t.period).collect(Collectors.toList()));

		Pair<List<Job>, Long> pair = new AnalysisUtils().getJobsInHyperPeriod(tasks);
		List<Job> jobs = pair.getKey();
		jobs.sort((c1, c2) -> Long.compare(c1.delta, c2.delta));
		System.out.println("number of jobs: " + jobs.size() + "   HyperPeriod: " + pair.getValue());

		Pair<Double, Double> result = new StaticIOSchedule().schedule(tasks, true);

		if (result != null)
			System.err.println("Number of Exact: " + result.getKey() + "  Total Qualtiy: " + result.getValue());
		else
			System.err.println("not schedulable");

	}
}
