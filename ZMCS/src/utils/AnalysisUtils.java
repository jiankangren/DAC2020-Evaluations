package utils;

import java.util.List;

import entity.PeriodicTask;

public class AnalysisUtils {
	public static final int MAX_PRIORITY = 1000;

	public long[] initResponseTime(List<PeriodicTask> tasks, int mode) {
		long[] response_times = new long[tasks.size()];

		tasks.sort((t1, t2) -> -Integer.compare(t1.priority, t2.priority));
		long[] Ri = new long[tasks.size()];

		for (int i = 0; i < tasks.size(); i++) {
			PeriodicTask t = tasks.get(i);
			Ri[i] = t.Ri = t.getWCET(mode);
		}
		return response_times;
	}

	public boolean isSystemSchedulable(List<PeriodicTask> tasks, long[] Ris) {
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
