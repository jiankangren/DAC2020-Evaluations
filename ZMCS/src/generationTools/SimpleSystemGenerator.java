package generationTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entity.PeriodicTask;

public class SimpleSystemGenerator {
	
	public boolean isLogUni;
	public int maxT;
	public int minT;

	public int total_tasks;
	public double totalUtil;
	boolean print;
	Random ran;

	public SimpleSystemGenerator(int minT, int maxT, int totalTasks, boolean isPeriodLogUni, int seed) {
		this.minT = minT;
		this.maxT = maxT;
		this.totalUtil = 0.05 * (double) totalTasks;
		this.total_tasks = totalTasks;
		this.isLogUni = isPeriodLogUni;
		this.print = false;
		this.ran = new Random(seed);
	}

	/*
	 * generate task sets for multiprocessor fully partitioned fixed-priority
	 * system
	 */
	public ArrayList<PeriodicTask> generateTasks() {
		ArrayList<PeriodicTask> tasks = null;
		while (tasks == null) {
			tasks = generateT();
		}
		return tasks;
	}

	private ArrayList<PeriodicTask> generateT() {
		int task_id = 1;
		ArrayList<PeriodicTask> tasks = new ArrayList<>(total_tasks);
		ArrayList<Long> periods = new ArrayList<>(total_tasks);

		/* generates random periods */
		while (true) {
			if (!isLogUni) {
				long period = (ran.nextInt(maxT - minT) + minT) * 10;
				if (!periods.contains(period))
					periods.add(period);
			} else {
				double a1 = Math.log(minT);
				double a2 = Math.log(maxT + 1);
				double scaled = ran.nextDouble() * (a2 - a1);
				double shifted = scaled + a1;
				double exp = Math.exp(shifted);

				int result = (int) exp;
				result = Math.max(minT, result);
				result = Math.min(maxT, result);

				long period = result * 10;
				if (!periods.contains(period))
					periods.add(period);
			}

			if (periods.size() >= total_tasks)
				break;
		}
		periods.sort((p1, p2) -> Double.compare(p1, p2));

		/* generate utils */
		UUnifastDiscard unifastDiscard = new UUnifastDiscard(totalUtil, total_tasks, 1000, ran);
		ArrayList<Double> utils = null;
		while (true) {
			utils = unifastDiscard.getUtils();

			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
			}

			if (utils != null)
				if (utils.size() == total_tasks && tt <= totalUtil)
					break;
		}
		if (print) {
			System.out.print("task utils: ");
			double tt = 0;
			for (int i = 0; i < utils.size(); i++) {
				tt += utils.get(i);
				System.out.print(tt + "   ");
			}
			System.out.println("\n total uitls: " + tt);
		}

		/* generate sporadic tasks */
		for (int i = 0; i < utils.size(); i++) {
			long computation_time = (long) (periods.get(i) * utils.get(i));
			if (computation_time == 0) {
				return null;
			}

			PeriodicTask t = new PeriodicTask(-1, periods.get(i), periods.get(i), computation_time, -1, task_id, utils.get(i));
			task_id++;
			tasks.add(t);
		}

		new PriorityGeneator().assignPandQbyDMPO(tasks);
		tasks.sort((p1, p2) -> -Double.compare(p1.util, p2.util));

		// assign criticality level
		int numberOfLowTasks = tasks.size() / 2;

		List<PeriodicTask> lowTaks = new ArrayList<>();

		for (int i = 0; i < numberOfLowTasks; i++) {
			int index = ran.nextInt(tasks.size());
			PeriodicTask t = tasks.get(index);
			t.criticaility = 0;
			tasks.remove(t);
			lowTaks.add(t);
		}
		
		for(int i=0; i<tasks.size();i++)
			tasks.get(i).criticaility = 1;

		// generate dependency
		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask HT = tasks.get(i);
			if (ran.nextDouble() < 0.2) {
				PeriodicTask LT = lowTaks.get(ran.nextInt(lowTaks.size()));
				HT.dependencyTask = LT;
			}
		}
		
		ArrayList<PeriodicTask> allTasks = new ArrayList<>();
		allTasks.addAll(tasks);
		allTasks.addAll(lowTaks);

		return allTasks;
	}

}
