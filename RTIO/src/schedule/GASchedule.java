package schedule;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entity.Job;
import entity.PeriodicTask;
import ga.Configuration;
import ga.FitnessFunction;
import ga.MOEAD;
import ga.PopulationEntry;
import javafx.util.Pair;
import utils.AnalysisUtils;

public class GASchedule {

	int population = 500;
	int iteration = 300;
	double mutationRate = 0.5;
	int TOURNAMENT_SIZE = 3;

	public List<List<Double>> schedule(List<PeriodicTask> tasks, Random rng) {
		Pair<List<Job>, Long> pair = new AnalysisUtils().getJobsInHyperPeriod(tasks);

		List<Job> jobs = pair.getKey();
		long hyperperiod = pair.getValue();

		return runGAsolver(jobs, hyperperiod, rng);
	}

	private List<List<Double>> runGAsolver(List<Job> jobs, long hyperPeriod, Random rng) {

		FitnessFunction of = new FitnessFunction();
		List<Configuration> initials = generateInitialPopulation(jobs, population, rng);

		MOEAD moead = new MOEAD(jobs, initials, of, iteration, mutationRate, TOURNAMENT_SIZE, rng);

		List<PopulationEntry> bestFront = moead.apply();

		List<List<Double>> pfs = new ArrayList<>();

		for (int i = 0; i < bestFront.size(); i++) {
			List<Double> pf = new ArrayList<>();
			for(int j =0; j<bestFront.get(i).getObjectives().size();j++) {
				pf.add(bestFront.get(i).getObjectives().get(j));
			}
			pfs.add(pf);
		}
		
		boolean isFeasible = false;
		for (int i = 0; i < pfs.size(); i++) {
			if (pfs.get(i).get(0) != Double.MAX_VALUE && pfs.get(i).get(0) != Double.MAX_VALUE) {
				isFeasible = true;
				break;
			}
		}

		if (isFeasible) {
			DecimalFormat df = new DecimalFormat("#.##");
			
			double jobNumber = jobs.size();
			double totalValue = jobs.stream().mapToDouble(j -> j.task.Vmax).sum();

			for (int i = 0; i < pfs.size(); i++) {
				double numberV = (jobNumber - pfs.get(i).get(0)) / jobNumber;
				double qualityV = (totalValue - pfs.get(i).get(1)) / totalValue;
				
				pfs.get(i).set(0, Double.parseDouble(df.format(numberV)));
				pfs.get(i).set(1, Double.parseDouble(df.format(qualityV)));
			}
			
			pfs.sort((c1,c2) -> -Double.compare(c1.get(0), c2.get(0)));
			
			return pfs;
		}
		else
			return null;



	}

	private List<Configuration> generateInitialPopulation(List<Job> jobs, int population, Random rng) {
		List<Configuration> initial = new ArrayList<>();

		for (int i = 0; i < population; i++) {
			List<Long> startTimes = new ArrayList<>();

			for (int j = 0; j < jobs.size(); j++) {
				long startTime = rng.nextInt((int) (jobs.get(j).endQ - jobs.get(j).startQ)) + jobs.get(j).startQ;
				startTimes.add(startTime);
			}

			Configuration config = new Configuration(startTimes);
			initial.add(config);
		}
		return initial;
	}



}
