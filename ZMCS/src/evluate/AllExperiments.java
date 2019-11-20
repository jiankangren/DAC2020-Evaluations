package evluate;

import java.util.ArrayList;
import java.util.List;

import analysis.AMCSteady;
import analysis.AMCSwitch;
import analysis.RTAWithoutBlocking;
import analysis.ZASteady;
import analysis.ZASwitch;
import entity.PeriodicTask;
import generationTools.SimpleSystemGenerator;

public class AllExperiments {

	static int minT = 10;
	static int maxT = 1000;
	static boolean isPeriodLogUni = true;
	static int seed = 10;
	static int NoS = 10000;

	static double HVSpeedup = 0.1;
	static double taskSpeedup = 1 - 0.129;  // max 23.2
	static double SASpeedup = 0.1;

	public static int compuationHV = 20;
	public static int periodHV = 200;
	public static int computationSA = 10;

	public static void main(String args[]) {
		lowMode();
	}

	public static void lowMode() {
		List<Integer> AMC_sched_low = new ArrayList<>();
		List<Integer> ZSV_sched_low = new ArrayList<>();
		List<Integer> ZTZ_sched_low = new ArrayList<>();
		List<Integer> ZHA_sched_low = new ArrayList<>();
		
		List<Integer> AMC_sched_high = new ArrayList<>();
		List<Integer> ZSV_sched_high = new ArrayList<>();
		List<Integer> ZTZ_sched_high = new ArrayList<>();
		List<Integer> ZHA_sched_high = new ArrayList<>();
		
		List<Integer> AMC_sched_switch = new ArrayList<>();
		List<Integer> ZSV_sched_switch = new ArrayList<>();
		List<Integer> ZTZ_sched_switch = new ArrayList<>();
		List<Integer> ZHA_sched_switch = new ArrayList<>();
		
		List<Integer> RTA = new ArrayList<>();

		for (int NoT = 4; NoT <= 20; NoT++) {
			SimpleSystemGenerator generator = new SimpleSystemGenerator(minT, maxT, NoT, isPeriodLogUni, seed);

			int AMC_schedulables_low = 0;
			int ZSV_schedulables_low = 0;
			int ZTZ_schedulables_low = 0;
			int ZHA_schedulables_low = 0;
			
			int AMC_schedulables_high = 0;
			int ZSV_schedulables_high = 0;
			int ZTZ_schedulables_high = 0;
			int ZHA_schedulables_high = 0;
			
			int AMC_schedulables_switch = 0;
			int ZSV_schedulables_switch = 0;
			int ZTZ_schedulables_switch = 0;
			int ZHA_schedulables_switch = 0;
			
			int rta_schedulables = 0;

			for (int j = 0; j < NoS; j++) {
				 System.out.println("NoT: " + NoT + " times: " + j);

				List<PeriodicTask> tasks = generator.generateTasks();
				
				if(new RTAWithoutBlocking().getResponseTime(tasks, false)) {
					rta_schedulables++;
				}
				
				/*
				 * Low mode
				 */

				if (new AMCSteady().schedulabilityTest(tasks, 0)) {
					AMC_schedulables_low++;
				}

				if (new ZASteady().schedulabilityTest(tasks, 0, 1, 1.304)) {
					ZSV_schedulables_low++;
				}

				if (new ZASteady().schedulabilityTest(tasks, 0, 1, 1.117)) {
					ZTZ_schedulables_low++;
				}

				if (new ZASteady().schedulabilityTest(tasks, 0, HVSpeedup, taskSpeedup)) {
					ZHA_schedulables_low++;
				}
				
				/*
				 * High Mode
				 */
				
				if (new AMCSteady().schedulabilityTest(tasks, 1)) {
					AMC_schedulables_high++;
				}

				if (new ZASteady().schedulabilityTest(tasks, 1, 1, 1.304)) {
					ZSV_schedulables_high++;
				}

				if (new ZASteady().schedulabilityTest(tasks, 1, 1, 1.117)) {
					ZTZ_schedulables_high++;
				}

				if (new ZASteady().schedulabilityTest(tasks, 1, HVSpeedup, taskSpeedup)) {
					ZHA_schedulables_high++;
				}
				
				/*
				 * System switch
				 */
				
				if (new AMCSwitch().schedulabilityTest(tasks)) {
					AMC_schedulables_switch++;
				}

				if (new ZASwitch().schedulabilityTest(tasks, 1, 1.304, 1)) {
					ZSV_schedulables_switch++;
				}

				if (new ZASwitch().schedulabilityTest(tasks, 1, 1.117,1)) {
					ZTZ_schedulables_switch++;
				}

				if (new ZASwitch().schedulabilityTest(tasks, HVSpeedup, taskSpeedup, SASpeedup)) {
					ZHA_schedulables_switch++;
				}
			}

			AMC_sched_low.add(AMC_schedulables_low);
			ZSV_sched_low.add(ZSV_schedulables_low);
			ZTZ_sched_low.add(ZTZ_schedulables_low);
			ZHA_sched_low.add(ZHA_schedulables_low);
			
			AMC_sched_high.add(AMC_schedulables_high);
			ZSV_sched_high.add(ZSV_schedulables_high);
			ZTZ_sched_high.add(ZTZ_schedulables_high);
			ZHA_sched_high.add(ZHA_schedulables_high);
			
			AMC_sched_switch.add(AMC_schedulables_switch);
			ZSV_sched_switch.add(ZSV_schedulables_switch);
			ZTZ_sched_switch.add(ZTZ_schedulables_switch);
			ZHA_sched_switch.add(ZHA_schedulables_switch);

			RTA.add(rta_schedulables);
		}

		System.out.println("Low Mode Schedulability");
		System.out.println("AMC: " + AMC_sched_low);
		System.out.println("ZSV: " + ZSV_sched_low);
		System.out.println("ZTZ: " + ZTZ_sched_low);
		System.out.println("ZHA: " + ZHA_sched_low);
		
		System.out.println("\n\n High Mode Schedulability");
		System.out.println("AMC: " + AMC_sched_high);
		System.out.println("ZSV: " + ZSV_sched_high);
		System.out.println("ZTZ: " + ZTZ_sched_high);
		System.out.println("ZHA: " + ZHA_sched_high);
		
		System.out.println("\n\n System Switch Schedulability");
		System.out.println("AMC: " + AMC_sched_switch);
		System.out.println("ZSV: " + ZSV_sched_switch);
		System.out.println("ZTZ: " + ZTZ_sched_switch);
		System.out.println("ZHA: " + ZHA_sched_switch);
		
		
		System.out.println("\n\n RTA");
		System.out.println(RTA);
	}
	
	

}
