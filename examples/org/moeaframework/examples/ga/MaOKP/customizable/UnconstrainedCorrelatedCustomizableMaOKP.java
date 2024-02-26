package org.moeaframework.examples.ga.MaOKP.customizable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.util.Vector;
import org.moeaframework.util.io.CommentedLineReader;

public class UnconstrainedCorrelatedCustomizableMaOKP implements Problem {

	/**
	 * The number of sacks.
	 */
	protected int nsacks;

	/**
	 * The number of items.
	 */
	protected int nitems;

	/**
	 * Entry {@code profit[i][j]} is the profit from including item {@code j} in
	 * sack {@code i}.
	 */
	protected int[][] profit;

	/**
	 * Entry {@code weight[i][j]} is the weight incurred from including item
	 * {@code j} in sack {@code i}.
	 */
	protected int[][] weight;

	/**
	 * Entry {@code capacity[i]} is the weight capacity of sack {@code i}.
	 */
	protected double[] capacityComputed;
	protected double[] capacityImputed;

	// List of items
	protected List<Item> items;

	// Array of maximum value of profit/weight ratio
	protected double[] itemsMaximumRatio;

	// Instance name
	protected String instanceName;

	// Formulation
	protected Formulation formulation;

	/**
	 * Constructs a multiobjective 0/1 knapsack problem instance loaded from the
	 * specified file.
	 * 
	 * @param file the file containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	public UnconstrainedCorrelatedCustomizableMaOKP(File file) throws IOException {
		this(new FileReader(file));
	}

	/**
	 * Constructs a multiobjective 0/1 knapsack problem instance loaded from the
	 * specified input stream.
	 * 
	 * @param inputStream the input stream containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	public UnconstrainedCorrelatedCustomizableMaOKP(InputStream inputStream) throws IOException {
		this(new InputStreamReader(inputStream));
	}

	/**
	 * Constructs a multiobjective 0/1 knapsack problem instance loaded from the
	 * specified reader.
	 * 
	 * @param reader the reader containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	public UnconstrainedCorrelatedCustomizableMaOKP(Reader reader) throws IOException {
		super();
		load(reader);
	}

	/**
	 * Loads the knapsack problem instance from the specified reader.
	 * 
	 * @param reader the file containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	private void load(Reader reader) throws IOException {

		String line = null;

		try (CommentedLineReader lineReader = new CommentedLineReader(reader)) {

			try {
				line = lineReader.readLine();
				nitems = Integer.parseInt(line);
			} catch (Exception e) {
				throw new IOException("knapsack data file not properly formatted: invalid number of items line");
			}

			try {
				line = lineReader.readLine();
				nsacks = Integer.parseInt(line);
			} catch (Exception e) {
				throw new IOException("knapsack data file not properly formatted: invalid number of sacks line");
			}

			capacityComputed = new double[nsacks];
			capacityImputed = new double[nsacks];
			profit = new int[nsacks][nitems];
			weight = new int[nsacks][nitems];
			itemsMaximumRatio = new double[nitems];

			// For each sack
			for (int i = 0; i < nsacks; i++) {

				double weightSum = 0.0;

				try {
					// capacity of the sack
					line = lineReader.readLine();
					capacityImputed[i] = Double.parseDouble(line);
				} catch (Exception e) {
					throw new IOException("knapsack data file not properly formatted: invalid capacity line");
				}

				// For each item
				for (int j = 0; j < nitems; j++) {

					try {

						// weight
						line = lineReader.readLine();
						weight[i][j] = Integer.parseInt(line);
						weightSum += weight[i][j];

					} catch (Exception e) {
						throw new IOException("knapsack data file not properly formatted: invalid weight line");
					}

					try {

						// profit
						line = lineReader.readLine();
						profit[i][j] = Integer.parseInt(line);

					} catch (Exception e) {
						throw new IOException("knapsack data file not properly formatted: invalid profit line");
					}

					// Update maximum ratio of the current item (based on all objectives)
					double ratio = ((double) profit[i][j]) / ((double) weight[i][j]);
					if (ratio > itemsMaximumRatio[j]) {
						itemsMaximumRatio[j] = ratio;
					}
				}

				capacityComputed[i] = weightSum * 0.5;
				System.out.println("Imputed capacity of the Sack " + i + ": " + capacityImputed[i]);
				System.out.println("Computed capacity of the Sack " + i + ": " + capacityComputed[i]);
			}

			// Add the item and its maximum value of profit/weight ratio
			items = new ArrayList<>();
			for (int j = 0; j < nitems; j++) {
				items.add(new Item(j, itemsMaximumRatio[j]));
			}

			// Sort the itens in an ascending order of maximum ratio
			Collections.sort(items);

			// displayProblemTest();
			// displayItems();
			System.out.println("\nItemns");
			items.forEach(System.out::println);

		}

	}

	public void createFormulation(Combination combination, double correlation) {
		this.formulation = new Formulation(combination.getFixedObjA(), combination.getFixedObjB(),
				this.getNumberOfObjectives(), correlation, combination.getObjsAssignedToFixedObjs());
	}

	public void displayFormulation() {
		this.formulation.display();
	}

	public void SetName(String name) {
		this.instanceName = name;
	}

	@Override
	public String getName() {
		return this.instanceName;
	}

	@Override
	public int getNumberOfVariables() {
		return 1;
	}

	@Override
	public int getNumberOfObjectives() {
		return this.nsacks;
	}

	@Override
	public int getNumberOfConstraints() {
		return 0;
	}

	@Override
	public void evaluate(Solution solution) {
		// TODO Auto-generated method stub

	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(1, nsacks, 0);
		solution.setVariable(0, EncodingUtils.newBinary(nitems));
		return solution;
	}

	@Override
	public void close() {
		// do nothing
	}

	private class Formulation {
		private int numberOfObjectives;
		private int fixedObjA;
		private int fixedObjB;
		private double correlation;
		private Map<Integer, Integer> objsAssignedToFixedObjs;

		public Formulation(int fixedObjA, int fixedObjB, int numberOfObjectives, double correlation,
				Map<Integer, Integer> objsAssignedToFixedObjs) {

			this.fixedObjA = fixedObjA;
			this.fixedObjB = fixedObjB;
			this.numberOfObjectives = numberOfObjectives;
			this.correlation = correlation;
			this.objsAssignedToFixedObjs = objsAssignedToFixedObjs;

		}

		public Map<Integer, Integer> getObjsAssignedToFixedObjs() {
			return objsAssignedToFixedObjs;
		}

		public void display() {

			System.out.println("\nFORMULATION");

			for (int i = 0; i < this.numberOfObjectives; i++) {
				if (i == fixedObjA)
					System.out.println("G" + (this.fixedObjA + 1) + "(x) = F" + (this.fixedObjA + 1) + "(x)");
				else if (i == fixedObjB)
					System.out.println("G" + (this.fixedObjB + 1) + "(x) = F" + (this.fixedObjB + 1) + "(x)");
				else {
					System.out.println(
							"G" + (i + 1) + "(x) = " + this.correlation + " * F" + (objsAssignedToFixedObjs.get(i) + 1)
									+ "(x) + (1.0 - " + this.correlation + ") * F" + (i + 1) + "(x)");
				}
			}

		}

	}

	private class Item implements Comparable<Item> {

		private Integer index;
		private Double maximumRatio;

		public Item(int index, double ratio) {
			this.index = index;
			this.maximumRatio = ratio;
		}

		public Integer getIndex() {
			return index;
		}

		public double getMaximumRatio() {
			return maximumRatio;
		}

		@Override
		public int compareTo(Item other) {
			return maximumRatio.compareTo(other.getMaximumRatio());
		}

		@Override
		public String toString() {
			return "Item [id=" + index + ", maximumRatio=" + maximumRatio + "]";
		}

	}

}
