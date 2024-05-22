package org.moeaframework.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.util.Vector;

public class AnD extends AbstractEvolutionaryAlgorithm {

	/**
	 * The name of the attribute for storing the normalized objectives.
	 */
	private static final String NORMALIZED_OBJECTIVES = "Normalized Objectives";

	/**
	 * The name of the attribute for storing the Shift normalized objectives.
	 */
	private static final String SHIFT_NORMALIZED_OBJECTIVES = "Shift Normalized Objectives";

	/**
	 * The variation operator.
	 */
	private final Variation variation;

	/**
	 * minimum objective of all individuals.
	 */
	private double[] minObjBounds;

	/**
	 * maximum objective of all individuals.
	 */
	private double[] maxOBjBounds;

	public AnD(Problem problem, Population population, Variation variation, Initialization initialization) {
		super(problem, population, null, initialization);
		this.variation = variation;
	}

	@Override
	protected void initialize() {
		super.initialize();
		minObjBounds = new double[problem.getNumberOfObjectives()];
		maxOBjBounds = new double[problem.getNumberOfObjectives()];
	}

	/**
	 * Returns the cosine between two objective vectors. This method assumes the two
	 * objective vectors are normalized.
	 * 
	 * @param point the point
	 * @param point the point
	 * @return the cosine
	 */
	protected static double cosine(double[] point_1, double[] point_2) {
		return Vector.dot(point_1, point_2) / (Vector.magnitude(point_1) * Vector.magnitude(point_2));
	}

	/**
	 * Returns the angle between two objective vectors. This method assumes the two
	 * objective vectors are normalized.
	 * 
	 * @param point the point
	 * @param point the point
	 * @return the angle (acosine)
	 */
	protected static double acosine(double[] point_1, double[] point_2) {
		return Math.acos(cosine(point_1, point_2));
	}

	/**
	 * Returns the euclidean distance in objective space between the two objective
	 * vectors.
	 * 
	 * @param point the point
	 * @param point the point
	 * @return the euclidean distance
	 */
	public static double euclideanDistance(Problem problem, double[] point_1, double[] point_2) {
		double distance = 0.0;

		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			distance += Math.pow(Math.abs(point_1[i] - point_2[i]), 2.0);
		}

		return Math.pow(distance, 1.0 / 2.0);
	}

	/**
	 * Updates the minimum and maximum values of each objective.
	 */
	protected void updateBounds() {
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			minObjBounds[i] = Double.POSITIVE_INFINITY;
			maxOBjBounds[i] = Double.NEGATIVE_INFINITY;
		}

		for (Solution solution : this.population) {
			for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
				minObjBounds[i] = Math.min(minObjBounds[i], solution.getObjective(i));
				maxOBjBounds[i] = Math.max(maxOBjBounds[i], solution.getObjective(i));
			}
		}
	}

	/**
	 * Compute the normalized objective values for the given solution.
	 * 
	 * @param solution the solution
	 */
	protected void normalizedObjectives(Solution solution) {

		double[] objectives = solution.getObjectives();

		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			objectives[i] = ((solution.getObjective(i) - this.minObjBounds[i])
					/ (this.maxOBjBounds[i] - this.minObjBounds[i]));
		}

		solution.setAttribute(NORMALIZED_OBJECTIVES, objectives);

	}

	/**
	 * Compute the normalized objective values for all population.
	 */
	protected void normalizedObjectivesPopulation() {
		for (Solution solution : this.population) {
			normalizedObjectives(solution);
		}
	}

	/**
	 * Computes the smallest angle between the given reference vector and all
	 * remaining vectors.
	 * 
	 * @param index the index of the reference vector
	 * @return the smallest angle between the given reference vector and all
	 *         remaining vectors
	 */
	protected int[] smallestAngleBetweenIndividuals() {

		double smallestAngle = Double.POSITIVE_INFINITY;
		int solutionIndex1 = -1, solutionIndex2 = -1;
		int populationSize = population.size();

		for (int i = 0; i < populationSize; i++) {
			double[] normalizedObjectives1 = (double[]) population.get(i).getAttribute(NORMALIZED_OBJECTIVES);

			for (int j = i + 1; j < populationSize; j++) {
				double[] normalizedObjectives2 = (double[]) population.get(j).getAttribute(NORMALIZED_OBJECTIVES);

				double angle = acosine(normalizedObjectives1, normalizedObjectives2);
				if (angle < smallestAngle) {
					smallestAngle = angle;
					solutionIndex1 = i;
					solutionIndex2 = j;
				}
			}
		}

		return new int[] { solutionIndex1, solutionIndex2 };
	}

	/**
	 * Shift the objective values for all population.
	 * 
	 * @param index the index of the individual
	 */
	protected void shiftIndividuals(int index) {
		int populationSize = population.size();

		double[] normalizedObjectivesRef = (double[]) population.get(index).getAttribute(NORMALIZED_OBJECTIVES);

		for (int i = 0; i < populationSize; i++) {
			if (i == index)
				continue;

			double[] normalizedObjectives = (double[]) population.get(i).getAttribute(NORMALIZED_OBJECTIVES);
			double[] shiftNormalizedObjectives = new double[problem.getNumberOfObjectives()];

			for (int j = 0; j < problem.getNumberOfObjectives(); j++) {
				if (normalizedObjectives[j] < normalizedObjectivesRef[j])
					shiftNormalizedObjectives[j] = normalizedObjectivesRef[j];
				else {
					shiftNormalizedObjectives[j] = normalizedObjectives[j];
				}
			}

			population.get(i).setAttribute(SHIFT_NORMALIZED_OBJECTIVES, shiftNormalizedObjectives);
		}
	}

	/**
	 * Calculate the Euclidian distances between the other shifted normalized
	 * objective vectors and the individual normalized objective vector
	 * 
	 * @param index the index of the individual
	 * @return euclidean distances
	 */
	protected List<Double> computeEuclideanDistance(int index) {
		int populationSize = population.size();
		List<Double> distances = new ArrayList<Double>();

		double[] normalizedObjectivesRef = (double[]) population.get(index).getAttribute(NORMALIZED_OBJECTIVES);

		for (int i = 0; i < populationSize; i++) {
			if (i == index)
				continue;

			double[] shiftNormalizedObjectives = (double[]) population.get(i).getAttribute(SHIFT_NORMALIZED_OBJECTIVES);
			distances.add(euclideanDistance(problem, shiftNormalizedObjectives, normalizedObjectivesRef));
		}

		return distances;
	}

	/**
	 * Compute shift based density estimation of the candidates to be removed from
	 * population
	 * 
	 * @param candidatesToBeRemoved the index of the candidates to be removed from
	 *                              population
	 * @return shift based density estimations
	 */
	protected double[] computeShiftBasedDensityEstimation(int[] candidatesToBeRemoved) {
		int populationSize = population.size();
		int k = (int) Math.pow(populationSize, 1.0 / 2.0);
		
		int numberOfCandidates = candidatesToBeRemoved.length;
		double[] density_estimations = new double[numberOfCandidates];		

		for (int i = 0; i < numberOfCandidates; i++) {
			int index = candidatesToBeRemoved[i];			
			shiftIndividuals(index);
			List<Double> distances = computeEuclideanDistance(index);
			Collections.sort(distances);			

			density_estimations[i] = 1.0 / (distances.get(k) + 2.0);
		}

		return density_estimations;
	}

	public Solution[] selectionOperator(int arity) {

		int populationSize = population.size();
		Solution[] result = new Solution[arity];

		for (int i = 0; i < arity; i++) {
			result[i] = this.population.get(PRNG.nextInt(populationSize));
		}

		return result;
	}

	@Override
	protected void iterate() {
		System.out.println(getNumberOfEvaluations());
		int populationSize = population.size();

		Population offspring = new Population();

		while (offspring.size() < populationSize) {
			Solution[] parents = selectionOperator(variation.getArity());
			Solution[] children = variation.evolve(parents);

			offspring.addAll(children);
		}

		evaluateAll(offspring);
		population.addAll(offspring);

		updateBounds();
		normalizedObjectivesPopulation();

		while (population.size() > populationSize) {
			int[] candidatesToBeRemoved = smallestAngleBetweenIndividuals();
			double[] density_estimations = computeShiftBasedDensityEstimation(candidatesToBeRemoved);

			if (density_estimations[0] < density_estimations[1]) {
				population.remove(candidatesToBeRemoved[1]);
			} else {
				population.remove(candidatesToBeRemoved[0]);
			}
		}

	}

}
