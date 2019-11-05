package generationTools;

import java.util.ArrayList;

import entity.PeriodicTask;
import utils.AnalysisUtils;

public class PriorityGeneator {

	public ArrayList<ArrayList<PeriodicTask>> assignPrioritiesByDM(ArrayList<ArrayList<PeriodicTask>> tasksToAssgin) {
		if (tasksToAssgin == null) {
			return null;
		}

		ArrayList<ArrayList<PeriodicTask>> tasks = new ArrayList<>(tasksToAssgin);
		/* For each partition, assign priorities */
		for (int i = 0; i < tasks.size(); i++) {
			new PriorityGeneator().deadlineMonotonicPriorityAssignment(tasks.get(i), tasks.get(i).size());
		}

		return tasks;
	}

	private void deadlineMonotonicPriorityAssignment(ArrayList<PeriodicTask> taskset, int NoT) {
		ArrayList<Integer> priorities = generatePriorities(NoT);
		/* deadline monotonic assignment */
		taskset.sort((t1, t2) -> Double.compare(t1.deadline, t2.deadline));
		priorities.sort((p1, p2) -> -Integer.compare(p1, p2));
		for (int i = 0; i < taskset.size(); i++) {
			taskset.get(i).priority = priorities.get(i);
		}
	}

	private ArrayList<Integer> generatePriorities(int number) {
		ArrayList<Integer> priorities = new ArrayList<>();
		for (int i = 0; i < number; i++)
			priorities.add(AnalysisUtils.MAX_PRIORITY - (i + 1) * 2);
		return priorities;
	}
}
