package analysis;

import java.util.ArrayList;

import entity.PeriodicTask;
import utils.AnalysisUtils;

public class RTAWithoutBlocking {
	long count = 0;

	public long[] getResponseTime(ArrayList<PeriodicTask> tasks) {
		long[] init_Ri = new AnalysisUtils().initResponseTime(tasks);
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

		return response_time;
	}

	private long[] busyWindow(ArrayList<PeriodicTask> tasks, long[] response_time) {
		long[] response_time_plus = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
//			for (int j = 0; j < tasks.get(i).size(); j++) {
				PeriodicTask task = tasks.get(i);
				long interference = highPriorityInterference(task, tasks, response_time[i]);
				response_time_plus[i] = task.Ri = task.WCET + interference;
				if (task.Ri > task.deadline)
					return response_time_plus;
//			}
		}
		return response_time_plus;

	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	protected long highPriorityInterference(PeriodicTask t, ArrayList<PeriodicTask> tasks, long Ri) {
		long interference = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				PeriodicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * hpTask.WCET;
			}
		}
		return interference;
	}

}
