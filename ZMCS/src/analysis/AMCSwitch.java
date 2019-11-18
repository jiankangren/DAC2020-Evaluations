package analysis;

import java.util.ArrayList;
import java.util.List;

import entity.PeriodicTask;

public class AMCSwitch {
	long count = 0;

	public boolean schedulabilityTest(List<PeriodicTask> allTasks) {
		allTasks.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
		
		List<PeriodicTask> tasks = new ArrayList<>(allTasks);

		long[] response_time_low = getResponseTimeLow(tasks);

		if (!isSchedulable(response_time_low, tasks))
			return false;

		return getResponseTime(tasks, response_time_low);
	}

	private long[] getResponseTimeLow(List<PeriodicTask> tasks) {

		long[] response_time = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask task = tasks.get(i);

			long R = task.getWCET(0);

			boolean isEqual = false, missDeadline = false;

			while (!isEqual) {
				isEqual = true;

				long newR = task.getWCET(0);

				for (int j = 0; j < tasks.size(); j++) {
					if (tasks.get(j).priority > task.priority) {
						newR += Math.ceil((double) R / (double) tasks.get(j).period) * tasks.get(j).getWCET(0);
					}
				}

				if (newR != R)
					isEqual = false;
				if (newR > task.deadline)
					missDeadline = true;

				R = newR;

				if (missDeadline)
					break;
			}

			response_time[i] = R;
		}

		return response_time;
	}

	private boolean getResponseTime(List<PeriodicTask> allTasks, long[] responseTimeLow) {

		List<PeriodicTask> tasks = new ArrayList<>();
		List<PeriodicTask> lowTasks = new ArrayList<>();

		for (int i = 0; i < allTasks.size(); i++) {
			if (allTasks.get(i).criticaility == 1) {
				tasks.add(allTasks.get(i));
			} else {
				lowTasks.add(allTasks.get(i));
			}
		}

		long[] response_time = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask task = tasks.get(i);

			long R = task.getWCET(1);

			boolean isEqual = false, missDeadline = false;

			while (!isEqual) {
				isEqual = true;

				long newR = getNewR(R, responseTimeLow[allTasks.indexOf(task)], task, tasks, lowTasks);

				if (newR != R)
					isEqual = false;
				if (newR > task.deadline)
					missDeadline = true;

				R = newR;

				if (missDeadline)
					break;
			}

			response_time[i] = R;
		}

		return isSchedulable(response_time, tasks);
	}

	private long getNewR(long R, long lowR, PeriodicTask task, List<PeriodicTask> tasks, List<PeriodicTask> lowTasks) {
		long newR = task.getWCET(1);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > task.priority) {
				newR += Math.ceil((double) R / (double) tasks.get(i).period) * tasks.get(i).getWCET(1);
			}
		}

		for (int i = 0; i < lowTasks.size(); i++) {
			if (lowTasks.get(i).priority > task.priority) {
				newR += Math.ceil((double) lowR / (double) tasks.get(i).period) * tasks.get(i).getWCET(0);
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
