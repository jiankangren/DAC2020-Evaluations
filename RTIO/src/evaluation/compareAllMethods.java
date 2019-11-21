package evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entity.PeriodicTask;
import generationTools.SimpleSystemGenerator;
import schedule.FPS_Schedule;
import schedule.FPS_analysis;
import schedule.GASchedule;
import schedule.GPIOCP;
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
		// EP1_SchedulabilityTest();
		EP2_IOPerformance();

	}

	public static void EP1_SchedulabilityTest() {
		List<Integer> best_sched = new ArrayList<>();
		List<Integer> fps_sched = new ArrayList<>();
		List<Integer> gpiocp_sched = new ArrayList<>();
		List<Integer> static_sched = new ArrayList<>();
		List<Integer> ga_sched = new ArrayList<>();

		for (int NoT = 4; NoT <= 18; NoT++) {
			SimpleSystemGenerator generator = new SimpleSystemGenerator(minT, maxT, LCM, NoT, NoT * 0.05, isPeriodLogUni, valueRange, seed, true);

			int best_schedulables = 0;
			int fps_schedulables = 0;
			int gpiocp_schedulables = 0;
			int static_schedulables = 0;
			int ga_schedulables = 0;

			for (int j = 0; j < NoS; j++) {
				System.out.println("NoT: " + NoT + " times: " + j);

				List<PeriodicTask> tasks = generator.generateTasks();

				List<List<Double>> result_best = new FPS_Schedule().schedule(tasks);
				if (result_best != null)
					best_schedulables++;

				boolean fps_schedulable = new FPS_analysis().schedule(tasks);
				if (fps_schedulable) {
					fps_schedulables++;
				}

				List<List<Double>> result_gpiocp = new GPIOCP().schedule(tasks);
				if (result_gpiocp != null) {
					gpiocp_schedulables++;
				}

				List<List<Double>> result_static = new StaticSchedule().schedule(tasks, true);
				if (result_static != null) {
					static_schedulables++;
				}

				List<List<Double>> result_ga = new GASchedule().schedule(tasks, new Random(seed));
				if (result_ga != null) {
					ga_schedulables++;
				}
			}

			best_sched.add(best_schedulables);
			fps_sched.add(fps_schedulables);
			gpiocp_sched.add(gpiocp_schedulables);
			static_sched.add(static_schedulables);
			ga_sched.add(ga_schedulables);

		}

		System.out.println("Schedulability");
		System.out.println(best_sched);
		System.out.println(fps_sched);
		System.out.println(gpiocp_sched);
		System.out.println(static_sched);
		System.out.println(ga_sched);

	}

	public static void EP2_IOPerformance() {
		int max = 1000;
		List<List<Result>> fps = new ArrayList<>();
		List<List<Result>> gpiocp = new ArrayList<>();
		List<List<Result>> stat = new ArrayList<>();
		List<List<Result>> ga = new ArrayList<>();

		for (int NoT = 6; NoT <= 14; NoT += 2) {
			SimpleSystemGenerator generator = new SimpleSystemGenerator(minT, maxT, LCM, NoT, NoT * 0.05, isPeriodLogUni, valueRange, seed, true);

			List<Result> fps_res = new ArrayList<>();
			List<Result> gpiocp_res = new ArrayList<>();
			List<Result> stat_res = new ArrayList<>();
			List<Result> ga_res = new ArrayList<>();

			int count = 0;
			int countMax = 0;

			while (count < 1000) {
				System.out.println("NoT: " + NoT + " times: " + count + " count max: " + countMax + ", " + fps_res.size() + " " + gpiocp_res.size() + " "
						+ stat_res.size() + " " + ga_res.size());

				List<PeriodicTask> tasks = generator.generateTasks();

				if (fps_res.size() < max) {
					List<List<Double>> result_best = new FPS_Schedule().schedule(tasks);
					if (result_best != null) {
						Result res = new Result(result_best);
						fps_res.add(res);
					}

				}

				if (gpiocp_res.size() < max) {
					List<List<Double>> result_gpiocp = new GPIOCP().schedule(tasks);
					if (result_gpiocp != null) {
						Result res = new Result(result_gpiocp);
						gpiocp_res.add(res);
					}
				}

				if (stat_res.size() < max) {
					List<List<Double>> result_static = new StaticSchedule().schedule(tasks, true);
					if (result_static != null) {
						Result res = new Result(result_static);
						stat_res.add(res);
					}
				}

				if (ga_res.size() < max) {
					List<List<Double>> result_ga = new GASchedule().schedule(tasks, new Random(seed));
					if (result_ga != null) {
						Result res = new Result(result_ga);
						ga_res.add(res);
					}
				}

				count = Math.min(Math.min(fps_res.size(), gpiocp_res.size()), Math.min(stat_res.size(), ga_res.size()));
				countMax = Math.max(Math.max(fps_res.size(), gpiocp_res.size()), Math.max(stat_res.size(), ga_res.size()));
			}

			fps.add(fps_res);
			gpiocp.add(gpiocp_res);
			stat.add(stat_res);
			ga.add(ga_res);
		}

		// NoT, value for each method: fps, gpiocp, static, ga
		List<List<List<Double>>> numOfExacts = new ArrayList<>();
		List<List<List<Double>>> values = new ArrayList<>();

		for (int i = 0; i < fps.size(); i++) {
			// value for each method: fps, gpiocp, static, ga
			List<List<Double>> oneSettingNum = new ArrayList<>();
			List<List<Double>> oneSettingVal = new ArrayList<>();

			List<Result> fps1 = fps.get(i);
			List<Result> gpiocp1 = gpiocp.get(i);
			List<Result> static1 = stat.get(i);
			List<Result> ga1 = ga.get(i);

			List<Double> fps1Res = flatList(fps1, 0);
			List<Double> gpiocp1Res = flatList(gpiocp1, 0);
			List<Double> static1Res = flatList(static1, 0);
			List<Double> ga1Res = flatList(ga1, 0);

			List<Double> fps2Res = flatList(fps1, 1);
			List<Double> gpiocp2Res = flatList(gpiocp1, 1);
			List<Double> static2Res = flatList(static1, 1);
			List<Double> ga2Res = flatList(ga1, 1);

			oneSettingNum.add(fps1Res);
			oneSettingNum.add(gpiocp1Res);
			oneSettingNum.add(static1Res);
			oneSettingNum.add(ga1Res);

			oneSettingVal.add(fps2Res);
			oneSettingVal.add(gpiocp2Res);
			oneSettingVal.add(static2Res);
			oneSettingVal.add(ga2Res);

			numOfExacts.add(oneSettingNum);
			values.add(oneSettingVal);
		}

		System.out.println("Number of exact jobs");
		for (int i = 0; i < numOfExacts.size(); i++) {
			System.out.println("For one NoT Setting: ");
			List<List<Double>> oneSetting = numOfExacts.get(i);

			for (int j = 0; j < oneSetting.size(); j++) {
				for (int k = 0; k < oneSetting.get(j).size(); k++) {
					System.out.print(oneSetting.get(j).get(k) + " ");
				}
				System.out.println();
			}

		}

		System.out.println("\n\n Values");
		for (int i = 0; i < values.size(); i++) {
			System.out.println("For one NoT Setting: ");
			List<List<Double>> oneSetting = values.get(i);

			for (int j = 0; j < oneSetting.size(); j++) {
				for (int k = 0; k < oneSetting.get(j).size(); k++) {
					System.out.print(oneSetting.get(j).get(k) + " ");
				}
				System.out.println();
			}

		}

	}

	private static List<Double> flatList(List<Result> list, int index) {
		List<Double> flat = new ArrayList<>();

		for (int i = 0; i < list.size(); i++) {
			Result s = list.get(i);
			for (int j = 0; j < s.res.size(); j++) {
				List<Double> pf = s.res.get(j);
				flat.add(pf.get(index));
			}
		}

		return flat;
	}
}

class Result {
	List<List<Double>> res;

	public Result(List<List<Double>> res) {
		this.res = res;
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
