package utils;

import java.util.ArrayList;

import entity.PeriodicTask;

public class AnalysisUtils {
	public static final int MAX_PRIORITY = 1000;

	public long[] initResponseTime(ArrayList<PeriodicTask> tasks) {
		long[] response_times = new long[tasks.size()];

		tasks.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
		long[] Ri = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {

			PeriodicTask t = tasks.get(i);
			Ri[i] = t.Ri = t.WCET;

		}
		return response_times;
	}

	public boolean isSystemSchedulable(ArrayList<PeriodicTask> tasks, long[] Ris) {
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).deadline < Ris[i])
				return false;
		}
		return true;
	}

	public void cloneList(long[] oldList, long[] newList) {
		for (int i = 0; i < oldList.length; i++) {
			newList[i] = oldList[i];
		}
	}

	public boolean isArrayContain(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value)
				return true;
		}
		return false;
	}

}
