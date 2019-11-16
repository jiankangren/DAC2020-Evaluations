package evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entity.PeriodicTask;
import generationTools.SimpleSystemGenerator;
import schedule.GASchedule;
import schedule.StaticSchedule;

public class compareAllMethods {

	static int minT = 1;
	static int maxT = 20;
	static int totalTasks = 15;
	static double totalUtil = 0.7;
	static double valueRange = 0.5;
	static int LCM = 1440;

	static boolean isPeriodLogUni = true;
	static int seed = 10;

	static int NoS = 1000;

	public static void main(String args[]) {
		EP1_SchedulabilityTest();
	}

	public static void EP1_SchedulabilityTest() {
		List<Integer> res = new ArrayList<Integer>();

		for (int NoT = 5; NoT <= 18; NoT++) {
			SimpleSystemGenerator generator = new SimpleSystemGenerator(minT, maxT, LCM, NoT, NoT * 0.05, isPeriodLogUni, valueRange, seed, true);
			int static_schedulables = 0;
			int ga_schedulables = 0;
			
			
			
			List<List<Double>> static_rs = new ArrayList<>();
			List<List<Double>> ga_rs = new ArrayList<>();

			for (int j = 0; j < NoS; j++) {
				System.out.println("NoT: " + NoT + " times: " + j);

				List<PeriodicTask> tasks = generator.generateTasks();

				List<List<Double>> result_static = new StaticSchedule().schedule(tasks, true);
				if (result_static != null) {
					static_schedulables++;
					static_rs.addAll(result_static);
					System.out.println("static: \n" + result_static.get(0));
				}

				List<List<Double>> result_ga = new GASchedule().schedule(tasks, new Random(seed));
				if (result_ga != null) {
					ga_schedulables++;
					ga_rs.addAll(result_ga);
					System.out.println("GA");
					for (int i = 0; i < result_ga.size(); i++) {
						System.out.println(result_ga.get(i));
					}
				}

			}
			res.add(static_schedulables);
			res.add(ga_schedulables);
		}

		System.out.println(res);
	}
}

// System.out.println("task generated");
// System.out.println("Period: " + tasks.stream().map(t ->
// t.period).collect(Collectors.toList()));

// Pair<List<Job>, Long> pair = new AnalysisUtils().getJobsInHyperPeriod(tasks);
// List<Job> jobs = pair.getKey();
// jobs.sort((c1, c2) -> Long.compare(c1.delta, c2.delta));
// System.out.println("number of jobs: " + jobs.size() + " HyperPeriod: " +
// pair.getValue());
