package org.moeaframework.examples.ga.MaOKP.standard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.util.Vector;
import org.moeaframework.util.io.CommentedLineReader;

public class UnconstrainedMaOKP implements Problem {

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

	/**
	 * Constructs a multiobjective 0/1 knapsack problem instance loaded from the
	 * specified file.
	 * 
	 * @param file the file containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	public UnconstrainedMaOKP(File file) throws IOException {
		this(new FileReader(file));
	}

	/**
	 * Constructs a multiobjective 0/1 knapsack problem instance loaded from the
	 * specified input stream.
	 * 
	 * @param inputStream the input stream containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	public UnconstrainedMaOKP(InputStream inputStream) throws IOException {
		this(new InputStreamReader(inputStream));
	}

	/**
	 * Constructs a multiobjective 0/1 knapsack problem instance loaded from the
	 * specified reader.
	 * 
	 * @param reader the reader containing the knapsack problem instance
	 * @throws IOException if an I/O error occurred
	 */
	public UnconstrainedMaOKP(Reader reader) throws IOException {
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
			//System.out.println("\nItemns");
			//items.forEach(System.out::println);

		}

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

	private double[] calculateSacksProfit(boolean[] itemSelection) {
		double[] sackProfits = new double[this.nsacks];
		for (int j = 0; j < this.nitems; j++) {
			if (itemSelection[j]) {
				for (int i = 0; i < this.nsacks; i++) {
					sackProfits[i] += this.profit[i][j];
				}
			}
		}
		return sackProfits;
	}

	private double[] calculateSacksWeight(boolean[] itemSelection) {
		double[] sackWeights = new double[this.nsacks];
		for (int j = 0; j < this.nitems; j++) {
			if (itemSelection[j]) {
				for (int i = 0; i < this.nsacks; i++) {
					sackWeights[i] += this.weight[i][j];
				}
			}
		}
		return sackWeights;
	}

	@Override
	public void evaluate(Solution solution) {

		// Infeasibility status
		boolean infeasible = false;

		// Get item selection
		boolean[] itemSelection = EncodingUtils.getBinary(solution.getVariable(0));

		// Calculate the weights of all knapsacks
		double[] sacksWeight = calculateSacksWeight(itemSelection);

		// Check if any weights exceed the capacities
		for (int i = 0; i < this.nsacks; i++) {
			if (sacksWeight[i] <= this.capacityComputed[i]) {
				sacksWeight[i] = 0.0;
			} else {
				sacksWeight[i] = sacksWeight[i] - this.capacityComputed[i];
				infeasible = true;
			}
		}

		// If infeasible, it repairs the solution
		if (infeasible) {
			repairSolution(solution, itemSelection, sacksWeight);
			sacksWeight = calculateSacksWeight(itemSelection);
		}

		// Calculate the profits of all knapsacks
		double[] sacksProfit = calculateSacksProfit(itemSelection);

		// Double check if any weight exceeds capacities
		for (int i = 0; i < nsacks; i++) {
			if (sacksWeight[i] > this.capacityComputed[i]) {				
				System.out.println("ERROR :::: Infeasible Knapsack!!!!");
				System.exit(-1);
			}
		}

		// Negate the objectives since Knapsack is maximization
		solution.setObjectives(Vector.negate(sacksProfit));

	}

	public void repairSolution(Solution solution, boolean[] itemSelection, double[] sacksWeight) {

		int itemIterator = 0;

		for (int i = 0; i < this.nsacks; i++) {

			while (sacksWeight[i] > 0.0) {

				int itemIndex = items.get(itemIterator).getIndex();

				if (itemSelection[itemIndex]) {
					itemSelection[itemIndex] = false;
					removeItemInAllSacks(itemIndex, sacksWeight);
				}

				itemIterator++;
			}

			sacksWeight[i] = 0.0;
		}

		// Update solution encoding
		EncodingUtils.setBinary(solution.getVariable(0), itemSelection);

	}

	public void removeItemInAllSacks(int itemIndex, double[] sacksWeight) {
		for (int i = 0; i < this.nsacks; i++) {
			sacksWeight[i] -= this.weight[i][itemIndex];
		}
	}

	public void checkFeasibility(Solution solution) {

		// Get item selection
		boolean[] itemSelection = EncodingUtils.getBinary(solution.getVariable(0));

		// Calculate the weights of the knapsacks
		double[] sacksWeight = calculateSacksWeight(itemSelection);

		// Check if any weights exceed the capacities
		for (int i = 0; i < this.nsacks; i++) {
			if (sacksWeight[i] > this.capacityComputed[i]) {
				System.out.println("ERROR :::: Infeasible Knapsack!!!!");
				System.exit(-1);
			}
		}

	}

	public void changeToMaximizationProblem(Solution solution) {
		solution.setObjectives(Vector.negate(solution.getObjectives()));
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(1, this.nsacks, 0);
		solution.setVariable(0, EncodingUtils.newBinary(this.nitems));
		return solution;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

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
