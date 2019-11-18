package analysis;

import java.util.List;

import entity.PeriodicTask;
import utils.AnalysisUtils;

public class RTAWithoutBlocking {
	long count = 0;

	public boolean getResponseTime(List<PeriodicTask> tasks, boolean printBebug) {
		long[] init_Ri = new AnalysisUtils().initResponseTime(tasks, 0);
		long[] response_time = new long[tasks.size()];
		boolean isEqual = false, missDeadline = false;
		count = 0;

		new AnalysisUtils().cloneList(init_Ri, response_time);

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[] response_time_plus = busyWindow(tasks, response_time);

			for (int i = 0; i < response_time_plus.length; i++) {

				if (response_time[i] != response_time_plus[i])
					isEqual = false;
				if (response_time_plus[i] > tasks.get(i).deadline)
					missDeadline = true;

			}

			count++;
			new AnalysisUtils().cloneList(response_time_plus, response_time);
			if (missDeadline)
				break;
		}

		return new AnalysisUtils().isSystemSchedulable(tasks, response_time);
	}

	private long[] busyWindow(List<PeriodicTask> tasks, long[] response_time) {
		long[] response_time_plus = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask task = tasks.get(i);
			long interference = highPriorityInterference(task, tasks, response_time[i]);
			response_time_plus[i] = task.Ri = task.getWCET(0) + interference;
			if (task.Ri > task.deadline)
				return response_time_plus;

		}
		return response_time_plus;

	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	protected long highPriorityInterference(PeriodicTask t, List<PeriodicTask> allTasks, long Ri) {
		long interference = 0;

		for (int i = 0; i < allTasks.size(); i++) {
			if (allTasks.get(i).priority > t.priority) {
				PeriodicTask hpTask = allTasks.get(i);
				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.getWCET(0));
			}
		}
		return interference;
	}

}
