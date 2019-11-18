package analysis;

import java.util.ArrayList;
import java.util.List;

import entity.PeriodicTask;

public class AMCSteady {
	long count = 0;

	public boolean schedulabilityTest(List<PeriodicTask> allTasks, int mode) {
		allTasks.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
		
		List<PeriodicTask> tasks = new ArrayList<>(allTasks);
		return getResponseTime(tasks, mode);
	}

	private boolean getResponseTime(List<PeriodicTask> allTasks, int mode) {

		List<PeriodicTask> tasks = new ArrayList<>();
		for (int i = 0; i < allTasks.size(); i++) {
			if (allTasks.get(i).criticaility >= mode) {
				tasks.add(allTasks.get(i));
			}
		}

		long[] response_time = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask task = tasks.get(i);

			long R = task.getWCET(mode);

			boolean isEqual = false, missDeadline = false;

			while (!isEqual) {
				isEqual = true;

				long newR = getNewR(R, task, tasks, mode);

				if (newR != R)
					isEqual = false;
				if (newR > task.deadline)
					missDeadline = true;

				R = newR;

				if (missDeadline)
					return false;
			}

			response_time[i] = R;
		}

		return isSchedulable(response_time, tasks);
	}

	private long getNewR(long R, PeriodicTask task, List<PeriodicTask> tasks, int mode) {
		long newR = task.getWCET(mode);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > task.priority) {
				newR += Math.ceil((double) R / (double) tasks.get(i).period) * tasks.get(i).getWCET(mode);
			}
		}

		return newR;
	}

	private boolean isSchedulable(long[] response_time, List<PeriodicTask> tasks) {
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).deadline < response_time[i])
				return false;
		}
		return true;
	}

}
