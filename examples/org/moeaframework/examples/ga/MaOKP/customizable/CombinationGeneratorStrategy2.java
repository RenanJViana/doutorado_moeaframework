package org.moeaframework.examples.ga.MaOKP.customizable;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CombinationGeneratorStrategy2 {

	private int numberOfObjectives;

	public CombinationGeneratorStrategy2(int numberOfObjectives) {
		this.numberOfObjectives = numberOfObjectives;
	}

	private List<List<Integer>> generateSubsets(List<Integer> options, int subsetSize) {
		List<List<Integer>> subsets = new ArrayList<>();
		List<Integer> currSubset = new ArrayList<>();

		generateSubsetsHelper(options, 0, currSubset, subsetSize, subsets);

		return subsets;
	}

	private void generateSubsetsHelper(List<Integer> options, int index, List<Integer> currSubset, int subsetSize,
			List<List<Integer>> subsets) {
		if (index >= options.size()) {
			if (currSubset.size() == subsetSize) {
				subsets.add(new ArrayList<>(currSubset));
			}
			return;
		}
		if (currSubset.size() > subsetSize)
			return;

		// Include current element
		currSubset.add(options.get(index));
		generateSubsetsHelper(options, index + 1, currSubset, subsetSize, subsets);
		currSubset.remove(currSubset.size() - 1);

		// Exclude current element
		generateSubsetsHelper(options, index + 1, currSubset, subsetSize, subsets);
	}

	private static Map<Integer, Integer> createMap(int key, int value) {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(key, value);
		return map;
	}

	public List<Combination> generateCombinations() {

		if (this.numberOfObjectives < 4) {
			List<Combination> combinations = new ArrayList<Combination>();
			combinations.add(new Combination(0, 1, createMap(2, 0)));
	        combinations.add(new Combination(0, 1, createMap(2, 1)));
	        combinations.add(new Combination(0, 2, createMap(1, 0)));
	        combinations.add(new Combination(0, 2, createMap(1, 2)));
	        combinations.add(new Combination(1, 2, createMap(0, 1)));
	        combinations.add(new Combination(1, 2, createMap(0, 2)));
			return combinations;
		}

		List<Combination> combinations = new ArrayList<>();

		int fixedObjA, fixedObjB;
		List<Integer> allObjs = new ArrayList<>();
		for (int i = 0; i < numberOfObjectives; ++i) {
			allObjs.add(i);
		}

		List<List<Integer>> allCombinations = generateSubsets(allObjs, 2);

		for (List<Integer> subset1 : allCombinations) {
			fixedObjA = subset1.get(0);
			fixedObjB = subset1.get(1);
			List<Integer> allObjsExceptFixedOnes = new ArrayList<>();
			for (int obj : allObjs) {
				if (obj == fixedObjA || obj == fixedObjB)
					continue;
				allObjsExceptFixedOnes.add(obj);
			}

			List<List<Integer>> subsets2 = generateSubsets(allObjsExceptFixedOnes, allObjsExceptFixedOnes.size() / 2);

			for (List<Integer> subset2 : subsets2) {
				Map<Integer, Integer> objsAssignedToFixedObjs = new HashMap<>();
				for (int obj : allObjsExceptFixedOnes) {
					if (subset2.contains(obj)) {
						objsAssignedToFixedObjs.put(obj, fixedObjA);
					} else {
						objsAssignedToFixedObjs.put(obj, fixedObjB);
					}
				}
				combinations.add(new Combination(fixedObjA, fixedObjB, objsAssignedToFixedObjs));
			}
		}
		return combinations;
	}

	public static void main(String[] args) {
		java.util.Scanner scanner = new java.util.Scanner(System.in);

		System.out.print("\n Digite o numero de objetivos: ");
		int numberOfObjectives = scanner.nextInt();
		System.out.println();
		scanner.close();

		CombinationGeneratorStrategy2 combinationGenerator = new CombinationGeneratorStrategy2(numberOfObjectives);
		List<Combination> combinations = combinationGenerator.generateCombinations();

		int contComb = 0;

		for (Combination combination : combinations) {
			int fixedObjA = combination.getFixedObjA();
			int fixedObjB = combination.getFixedObjB();
			Map<Integer, Integer> objsAssignedToFixedObjs = combination.getObjsAssignedToFixedObjs();

			System.err.println("-> fixedObjA: " + (fixedObjA + 1) + " fixedObjB: " + (fixedObjB + 1));
			for (Map.Entry<Integer, Integer> entry : objsAssignedToFixedObjs.entrySet()) {
				int correlatedObj = entry.getKey();
				int baseObj = entry.getValue();
				System.err.println("baseObj: " + (baseObj + 1) + " correlatedObj: " + (correlatedObj + 1));
			}

			contComb++;
			System.err.println();
		}

		System.err.println("Num. combinations: " + contComb);
	}
}