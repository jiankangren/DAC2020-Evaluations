package tsn;

import java.util.ArrayList;
import java.util.Arrays;

import entity.PeriodicTask;
import generationTools.SimpleSystemGenerator;

public class Run_RTA_TSN {

	public static void main(String args[]) {

		int schedulableCount = 0;

		SimpleSystemGenerator generator = new SimpleSystemGenerator(1, 1000, -1, 5, 1.0, true, 0.5, 1, false);

		for (int s = 0; s < 1000; s++) {
			ArrayList<PeriodicTask> tasks = generator.generateTasks();

			int[][] taskParameters = new int[tasks.size()][];
			for (int i = 0; i < taskParameters.length; i++) {
				int[] parameter = new int[5];
				parameter[0] = (int) tasks.get(i).WCET;
				parameter[1] = (int) tasks.get(i).period;
				parameter[2] = (int) tasks.get(i).deadline;
				parameter[3] = (int) tasks.get(i).priority;
				parameter[4] = (int) tasks.get(i).WCET;

				taskParameters[i] = parameter;

				System.out.println(Arrays.toString(parameter));
			}

			boolean schedulable = new RTA_TSN().schedulabilityTest(taskParameters);

			if (schedulable)
				schedulableCount++;

			System.out.println("is schedulable: " + schedulable + "\n");
		}
		System.out.println(schedulableCount);
	}
}
