package ga;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.primes.Primes;

import entity.Job;

/**
 * This class provide the abstraction towards various versions of MOEA\D
 * algorithms.
 */

public class MOEAD {
	List<Job> jobs;

	/**
	 * The supported decomposition approaches.
	 */
	public static enum DecompositionType {
		WEIGHTEDSUM, TCHEBYCHEFF;
	}

	/********************* Configurable MOEA/D Parameters *********************/

	/** By default the Tchebycheff decomposition approach is applied. */
	static final DecompositionType decomposition = DecompositionType.TCHEBYCHEFF;

	/** The notion T in Zhang & Li paper, specifying the size of neighborhood */
	static final int neighborhoodSize = 30;

	/** The size of the external population i.e., the Pareto Front */
	static int SIZE_OF_EP = 100;

	/******************
	 * Configurable MOEA/D Parameters Ends
	 ********************/

	/************************ Fixed MOEA/D Parameters *************************/

	/* Weight vectors */
	List<double[]> lambda;

	/* The neighborhood of each individual */
	List<int[]> neighborhood;

	/*
	 * The fitness value of each individual calculated by a certain
	 * decomposition approach. This implementation supports the Weighted Sum and
	 * Tchebycheff decomposition approaches described in Zhang & Li paper.
	 */
	List<Double> fitness;

	/*
	 * The notation of Z in in Zhang & Li paper, stores the best results of each
	 * objective.
	 */
	List<Double> idealPoint;

	/*
	 * The external population stores the non-dominated solutions (PF) found
	 * during the search. The size of this list is strictly limited to @param
	 * SIZE_OF_EP.
	 */
	List<PopulationEntry> externalPopulation;

	/********************** Fixed MOEA/D Parameters Ends **********************/

	/************************** Generic GA Parameters *************************/
	int populationSize;
	int numberOfObjectives;
	int tournamentSize;

	Random rng;
	List<Configuration> initialPopulation;

	public List<PopulationEntry> currentPopulation;

	FitnessFunction of;

	int iteration;
	double pm;

	/************************
	 * Generic GA Parameters Ends
	 ***********************/

	public MOEAD(List<Job> jobs, List<Configuration> initialPopulation, FitnessFunction of, int iteration, double pm, int tournamentSize, Random rng) {
		this.jobs = jobs;
		this.initialPopulation = initialPopulation;
		this.iteration = iteration;
		this.pm = pm;

		this.of = of;

		this.populationSize = initialPopulation.size();
		this.numberOfObjectives = 2;
		this.tournamentSize = tournamentSize;
		this.rng = rng;

		this.lambda = new ArrayList<double[]>(populationSize);
		this.neighborhood = new ArrayList<int[]>(populationSize);
		this.fitness = new ArrayList<Double>(populationSize);
		this.idealPoint = new ArrayList<Double>(numberOfObjectives);
		this.externalPopulation = new ArrayList<PopulationEntry>();
	}

	/**
	 * Returns the first @param numOfPrimes prime numbers.
	 */
	private int[] generateFirstKPrimes(int numOfPrimes) {
		int[] primes = new int[numOfPrimes];
		primes[0] = 2;

		for (int i = 1; i < numOfPrimes; i++) {
			primes[i] = Primes.nextPrime(primes[i - 1]);
		}

		return primes;
	}

	/**
	 * Generates weights according to a uniform design of mixtures using the
	 * Hammersley low-discrepancy sequence generator. This algorithm is
	 * implemented by David Hadka from the MOEAFramework project at
	 * https://github.com/dhadka/MOEAFramework.
	 */
	void initializeUniformWeight() {
		if (numberOfObjectives == 1) {
			for (int n = 0; n < populationSize; n++) {
				lambda.add(new double[] { 1 });
			}
			return;
		}
		if (numberOfObjectives == 2) {
			for (int n = 0; n < populationSize; n++) {
				double a = 1.0 * n / (populationSize - 1);

				lambda.add(new double[] { a, 1 - a });
			}
			return;
		}

		/* generate uniform design using Hammersley method */
		List<double[]> designs = new ArrayList<double[]>();
		int[] primes = generateFirstKPrimes(numberOfObjectives - 2);

		for (int i = 0; i < populationSize; i++) {
			double[] design = new double[numberOfObjectives - 1];
			design[0] = (2.0 * (i + 1) - 1.0) / (2.0 * populationSize);

			for (int j = 1; j < numberOfObjectives - 1; j++) {
				double f = 1.0 / primes[j - 1];
				int d = i + 1;
				design[j] = 0.0;

				while (d > 0) {
					design[j] += f * (d % primes[j - 1]);
					d = d / primes[j - 1];
					f = f / primes[j - 1];
				}
			}

			designs.add(design);
		}

		/* transform designs into weight vectors (sum to 1) */
		for (double[] design : designs) {
			double[] weight = new double[numberOfObjectives];

			for (int i = 1; i <= numberOfObjectives; i++) {
				if (i == numberOfObjectives) {
					weight[i - 1] = 1.0;
				} else {
					weight[i - 1] = 1.0 - Math.pow(design[i - 1], 1.0 / (numberOfObjectives - i));
				}

				for (int j = 1; j <= i - 1; j++) {
					weight[i - 1] *= Math.pow(design[j - 1], 1.0 / (numberOfObjectives - j));
				}
			}
			lambda.add(weight);
		}

	}

	/**
	 * Initialize neighborhoods of each individual.
	 */
	void initializeNeighborhood() {

		for (int i = 0; i < populationSize; i++) {
			double[] euclideanDistance = new double[populationSize];
			int[] potentialNeighbors = new int[populationSize];

			for (int j = 0; j < populationSize; j++) {

				/****************************************************************
				 * calculate the Euclidean Distances based on weight vectors.
				 * see https://en.wikipedia.org/wiki/Euclidean_distance for
				 * details.
				 ***************************************************************/
				int dimension = lambda.get(i).length;
				double sum = 0;
				for (int k = 0; k < dimension; k++) {
					sum += (lambda.get(i)[k] - lambda.get(j)[k]) * (lambda.get(i)[k] - lambda.get(j)[k]);
				}

				/* The Euclidean Distance between individual i and j. */
				euclideanDistance[j] = Math.sqrt(sum);
				potentialNeighbors[j] = j;
			}

			/*
			 * Now we apply the notion of T (the neighborhood size) and get set
			 * the T closet neighbors of individual i.
			 */
			for (int n = 0; n < neighborhoodSize; n++) {
				for (int m = n + 1; m < populationSize; m++) {
					if (euclideanDistance[n] > euclideanDistance[m]) {
						double neighborED = euclideanDistance[n];
						euclideanDistance[n] = euclideanDistance[m];
						euclideanDistance[m] = neighborED;

						int neighborID = potentialNeighbors[n];
						potentialNeighbors[n] = potentialNeighbors[m];
						potentialNeighbors[m] = neighborID;
					}
				}
			}

			int actualNeighborhoodSize = Math.min(neighborhoodSize, populationSize);
			int[] neighbors = new int[actualNeighborhoodSize];
			System.arraycopy(potentialNeighbors, 0, neighbors, 0, actualNeighborhoodSize);
			neighborhood.add(neighbors);
		}
	}

	/**
	 * By default the ideal point is initialized to the positive infinity.
	 */
	void initializeIdealPoint() {
		for (int i = 0; i < numberOfObjectives; i++) {
			idealPoint.add(Double.POSITIVE_INFINITY);
		}
	}

	/**
	 * Update the ideal point to the best observed values so far.
	 */
	void updateIdealPoint(List<PopulationEntry> population) {
		for (PopulationEntry entry : population)
			updateIdealPoint(entry);
	}

	void updateIdealPoint(PopulationEntry individual) {
		for (int i = 0; i < individual.getObjectives().size(); i++) {
			double ideal = idealPoint.get(i);
			double objective = individual.getObjectives().get(i);
			if (objective < ideal) {
				idealPoint.set(i, objective);
			}
			// idealPoint.set(i, Math.min(idealPoint.get(i),
			// individual.getObjectives().get(i)));
		}
	}

	/**
	 * Here we compute the fitness value for each individual in the given
	 * population via the defined decomposition approach.
	 */
	double getFitness(PopulationEntry individual, int index) {
		double Fitness = 0;

		switch (decomposition) {
		case WEIGHTEDSUM:
			Fitness = 0;
			for (int i = 0; i < individual.getObjectives().size(); i++) {
				Fitness += individual.getObjectives().get(i) * lambda.get(index)[i];
			}
			break;

		case TCHEBYCHEFF:
			Fitness = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < individual.getObjectives().size(); i++) {
				Fitness = Math.max(Fitness, Math.max(lambda.get(index)[i], 0.0001) * Math.abs(individual.getObjectives().get(i) - idealPoint.get(i)));
			}

			break;
		default:
			break;
		}

		return Fitness;
	}

	void getFitnessAll(List<PopulationEntry> population) {
		for (int i = 0; i < populationSize; i++)
			fitness.add(getFitness(population.get(i), i));
	}

	/**
	 * Return true if @param individual1 strictly dominates @param individual2
	 * on each objective. Return false otherwise.
	 */
	boolean dominate(PopulationEntry individual1, PopulationEntry individual2) {
		boolean isDominate = false;

		if (individual1.getObjectives().size() != individual2.getObjectives().size()) {
			System.out.println("error");
		}

		for (int i = 0; i < individual1.getObjectives().size(); i++) {
			if (individual1.getObjectives().get(i) > individual2.getObjectives().get(i))
				return false;
			if (individual1.getObjectives().get(i) <= individual2.getObjectives().get(i))
				isDominate = true;
		}

		return isDominate;
	}

	/**
	 * Update the external population list. This list will be returned as the
	 * final optimization result. A new @param candidate can join into the list
	 * if and only if no members in the list can dominate the @param candidate.
	 * The members that are dominated by the @param candidate will be removed
	 * from the list.
	 */
	void updateExternalPopulation(PopulationEntry candidate) {

		if (externalPopulation.size() == 0)
			externalPopulation.add(candidate);
		else {
			boolean eligibleToJoin = true;

			/*
			 * remove the members that are dominated by the candidate and check
			 * the eligibility of the candidate.
			 */
			for (int i = 0; i < externalPopulation.size(); i++) {
				PopulationEntry member = externalPopulation.get(i);
				if (dominate(candidate, member)) {
					/* the candidate dominates a member */
					externalPopulation.remove(i);
					i--;
				} else if (dominate(member, candidate))
					/* the candidate is dominated by a member */
					eligibleToJoin = false;
			}

			if (eligibleToJoin) {
				if (externalPopulation.size() < SIZE_OF_EP)
					externalPopulation.add(candidate);
				else {
					boolean isVIP = false;

					for (int i = 0; i < idealPoint.size(); i++) {
						if (idealPoint.get(i) >= candidate.getObjectives().get(i)) {
							isVIP = true;
							break;
						}
					}
					if (isVIP) {
						externalPopulation.add(candidate);
						SIZE_OF_EP += 1;
					}
				}
			}
		}

	}

	/**
	 * Step 1: Initialization: initialize everything, includes EP, weights,
	 * neighborhood and ideal point based on the given @param initialPopulation
	 * and @param of.
	 */
	List<PopulationEntry> initialize() {

		/* Step 1.1 initialize EP. Performed in apply() function. */

		/*
		 * Step 1.2 Initialize weights, compute the Euclidean distance and
		 * generate neighborhood.
		 */
		initializeUniformWeight();
		initializeNeighborhood();

		/*
		 * Step 1.3 generate initial population. NOTE: this step is finished
		 * beforehand, the initial population is passed via @param
		 * initialPopulation.
		 */

		/* Step 1.4 initialize Z vector, the ideal point. */
		initializeIdealPoint();

		/*
		 * Calculate the objective values of initial population based on
		 * objective functions @param of
		 */

		final Stream<PopulationEntry> populationWithObjectiveValues = initialPopulation.stream().map((Configuration c) -> {

			List<Double> fitness = of.getFitness(jobs, c);

			return new PopulationEntry(c, fitness);
		});

		List<PopulationEntry> populationList = populationWithObjectiveValues.collect(Collectors.toList());

		/* Update FV, Z and EP values based on the initial population. */
		updateIdealPoint(populationList);
		getFitnessAll(populationList);
		for (int i = 0; i < populationList.size(); i++) {
			updateExternalPopulation(populationList.get(i));
		}

		return populationList;
	}

	void evolve() {
		/* for each individual in the current population */
		for (int i = 0; i < populationSize; i++) {
			/*
			 * Step 2.1 Reproduction. Get two random neighbors of individual i
			 * and let them crossover and mutate.
			 */
			int[] neighborIndexes = neighborhood.get(i);

			/*
			 * The randomly selection applied in the original MOEA/D algorithm.
			 */
			PopulationEntry corssover1 = currentPopulation.get(neighborIndexes[rng.nextInt(neighborIndexes.length)]);
			PopulationEntry corssover2 = currentPopulation.get(neighborIndexes[rng.nextInt(neighborIndexes.length)]);

			List<Configuration> offsprings = new ArrayList<>();
			// List<Configuration> offSpring = new ArrayList<>();
			offsprings.add(hyperMutation(onePointCrossover(corssover1.getConfiguration(), corssover2.getConfiguration()), jobs));
			offsprings.add(hyperMutation(onePointCrossover(corssover1.getConfiguration(), corssover2.getConfiguration()), jobs));

			for (int k = 0; k < offsprings.size(); k++) {
				Configuration c = offsprings.get(k);
				/*
				 * Step 2.2 Repair and Improvement. Apply the problem-specific
				 * heuristic to improve the new individual. For our application,
				 * this should check whether the solution is schedulable.
				 */

				/* Calculate the objective values of the new individual */
				PopulationEntry newIndividualEntry = new PopulationEntry(c, of.getFitness(jobs, c));

				/*
				 * Step 2.3 Update of Z. Update the Z vector if the new
				 * individual contains better values for any subproblems.
				 */
				updateIdealPoint(newIndividualEntry);

				/*
				 * Step 2.4 Update of Neighboring Solution. Iterates through all
				 * neighbors of individual i and replaces the neighbors with the
				 * new individual if they have a lower fitness.
				 */
				for (int j = 0; j < neighborhood.get(i).length; j++) {
					double neighborFitness = getFitness(currentPopulation.get(neighborIndexes[j]), neighborIndexes[j]);

					double newFitness = getFitness(newIndividualEntry, neighborIndexes[j]);
					if (newFitness <= neighborFitness) {
						currentPopulation.set(neighborIndexes[j], newIndividualEntry);
						fitness.set(neighborIndexes[j], newFitness);
					}
				}

				/*
				 * Step 2.5 Update of EP. (1) Remove from EP all the individuals
				 * dominated by the new individual; (2) Add new individual to EP
				 * if no individuals in EP dominates it.
				 */
				updateExternalPopulation(newIndividualEntry);
			}
		}
	}

	private Configuration hyperMutation(Configuration c, List<Job> jobs) {

		if (pm < 0.0 || pm > 1.0)
			throw new IllegalArgumentException();

		List<Long> startTimes = c.startTimes;

		for (int i = 0; i < startTimes.size(); i++) {
			if (rng.nextDouble() < pm) {
				long newStart = rng.nextInt((int) (jobs.get(i).endQ - jobs.get(i).startQ)) + jobs.get(i).startQ;
				startTimes.set(i, newStart);
			}
		}

		return new Configuration(startTimes);

	}

	private Configuration onePointCrossover(Configuration c1, Configuration c2) {
		assert (c1.startTimes.size() == c2.startTimes.size());

		int index = rng.nextInt(c1.startTimes.size());

		List<Long> corssed = new ArrayList<>();
		for (int i = 0; i < c1.startTimes.size(); i++) {
			if (i < index)
				corssed.add(c1.startTimes.get(i));
			else
				corssed.add(c2.startTimes.get(i));
		}

		// List<Long> crossed = c1.startTimes.subList(0, index);
		// crossed.addAll(c2.startTimes.subList(index, c2.startTimes.size()));

		assert (corssed.size() == c1.startTimes.size());

		return new Configuration(corssed);

	}

	/**
	 * The MOEA/D algorithm.
	 */
	public List<PopulationEntry> apply() {

		if (initialPopulation.size() < 2)
			throw new IllegalArgumentException();

		/* Step 1 Initialization */
		currentPopulation = initialize();

		assert (fitness.size() > 0 && neighborhood.size() > 0 && idealPoint.size() > 0 && lambda.size() > 0);

		// /* A recorder that stores all the evolution process. */
		// final List<List<PopulationEntry>> history = new ArrayList<>();
		// history.add(currentPopulation);

		int current_iteration = 0;
		/* Step 2 Update */
		while (current_iteration < iteration) {

			if (current_iteration % 100 == 0) {
				System.out.println("now " + current_iteration + " generations");
//				printResult();
			}

			evolve();
			current_iteration++;
		}

		return externalPopulation;
	}

	private void printResult() {

		externalPopulation.sort((c1, c2) -> c1.getObjectives().get(0).compareTo(c2.getObjectives().get(0)));
		for (PopulationEntry entry : externalPopulation) {
			String[] results = new String[entry.getObjectives().size()];
			String[] resultsArray = entry.getObjectives().stream().map(d -> d + "").collect(Collectors.toList()).toArray(results);
			System.out.println(String.join(" ", resultsArray));
		}
	}

}
