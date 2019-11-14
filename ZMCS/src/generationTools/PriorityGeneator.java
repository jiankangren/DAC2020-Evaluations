package generationTools;

import java.util.ArrayList;

import entity.PeriodicTask;
import utils.AnalysisUtils;

public class PriorityGeneator {

	public ArrayList<PeriodicTask> assignPandQbyDMPO(ArrayList<PeriodicTask> tasksToAssgin) {
		if (tasksToAssgin == null) {
			return null;
		}

		ArrayList<PeriodicTask> tasks = new ArrayList<>(tasksToAssgin);
		deadlineMonotonicPriorityAssignment(tasks, tasks.size());

		return tasks;
	}

	private void deadlineMonotonicPriorityAssignment(ArrayList<PeriodicTask> taskset, int NoT) {
		ArrayList<Integer> priorities = generatePriorities(NoT);
		/* deadline monotonic assignment */
		taskset.sort((t1, t2) -> Double.compare(t1.deadline, t2.deadline));
		priorities.sort((p1, p2) -> -Integer.compare(p1, p2));
		for (int i = 0; i < taskset.size(); i++) {
			taskset.get(i).priority = priorities.get(i);
			taskset.get(i).quality = priorities.get(i);
		}
	}

	private ArrayList<Integer> generatePriorities(int number) {
		ArrayList<Integer> priorities = new ArrayList<>();
		for (int i = 0; i < number; i++)
			priorities.add(AnalysisUtils.MAX_PRIORITY - (i + 1) * 2);
		return priorities;
	}
}
