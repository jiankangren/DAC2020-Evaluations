package analysis;

import java.util.ArrayList;
import java.util.List;

import entity.PeriodicTask;
import evluate.AllExperiments;

public class ZASwitch {
	long count = 0;

	public boolean schedulabilityTest(List<PeriodicTask> allTasks, double HVSpeedup, double taskSpeedup, double SASpeedUp) {
		allTasks.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));

		List<PeriodicTask> tasks = new ArrayList<>(allTasks);
		
		long[] response_time_low = getResponseTimeLow(tasks, HVSpeedup, taskSpeedup);

		if (!isSchedulable(response_time_low, tasks))
			return false;

		return getResponseTime(tasks, response_time_low, HVSpeedup, taskSpeedup, SASpeedUp);
	}

	private long[] getResponseTimeLow(List<PeriodicTask> tasks, double HVSpeedup, double taskSpeedup) {

		long[] response_time = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask task = tasks.get(i);

			long R = (long) Math.ceil((double) task.getWCET(0) * taskSpeedup);

			boolean isEqual = false, missDeadline = false;

			while (!isEqual) {
				isEqual = true;

				long newR = (long) Math.ceil((double) task.getWCET(0) * taskSpeedup);

				for (int j = 0; j < tasks.size(); j++) {
					if (tasks.get(j).priority > task.priority) {
						newR += Math.ceil((double) R / (double) tasks.get(j).period) * (long) Math.ceil((double) tasks.get(j).getWCET(0) * taskSpeedup);
					}
				}

				newR += Math.ceil((double) R / (double) AllExperiments.periodHV)
						* (long) Math.ceil((double) AllExperiments.compuationHV * HVSpeedup);

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

	private boolean getResponseTime(List<PeriodicTask> allTasks, long[] responseTimeLow, double HVSpeedup, double taskSpeedup, double SASpeedUp) {

		List<PeriodicTask> tasks = new ArrayList<>();
		List<PeriodicTask> lowTasks = new ArrayList<>();

		for (int i = 0; i < allTasks.size(); i++) {
			if (allTasks.get(i).criticaility == 1) {
				tasks.add(allTasks.get(i));
			} else {
				lowTasks.add(allTasks.get(i));
			}
		}

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).dependencyTask != null) {
				lowTasks.remove(tasks.get(i).dependencyTask);
				if (!tasks.contains(tasks.get(i).dependencyTask))
					tasks.add(tasks.get(i).dependencyTask);
			}
		}

		long[] response_time = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask task = tasks.get(i);

			long R = (long) Math.ceil((double) task.getWCET(1) * taskSpeedup);

			boolean isEqual = false, missDeadline = false;

			while (!isEqual) {
				isEqual = true;

				long newR = getNewR(R, responseTimeLow[allTasks.indexOf(task)], task, tasks, lowTasks, HVSpeedup, taskSpeedup, SASpeedUp);

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

	private long getNewR(long R, long lowR, PeriodicTask task, List<PeriodicTask> tasks, List<PeriodicTask> lowTasks, double HVSpeedup, double taskSpeedup,
			double SASpeedUp) {
		long newR = (long) Math.ceil((double) task.getWCET(1) * taskSpeedup);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).criticaility == 0)
				newR += (long) Math.ceil((double) AllExperiments.computationSA * SASpeedUp);
		}
		for (int i = 0; i < lowTasks.size(); i++) {
			newR += (long) Math.ceil((double) AllExperiments.computationSA * SASpeedUp);
		}

		newR += Math.ceil((double) R / (double) AllExperiments.periodHV) * (long) Math.ceil((double) AllExperiments.compuationHV * HVSpeedup);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > task.priority) {
				newR += Math.ceil((double) R / (double) tasks.get(i).period) *  (long) Math.ceil((double) tasks.get(i).getWCET(1) * taskSpeedup);
			}
		}

		for (int i = 0; i < lowTasks.size(); i++) {
			if (lowTasks.get(i).priority > task.priority) {
				newR += Math.ceil((double) lowR / (double) tasks.get(i).period) * (long) Math.ceil((double) tasks.get(i).getWCET(0) * taskSpeedup);
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
