package org.moeaframework.examples.ga.MaOKP.customizable;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CombinationGeneratorStrategy1 {

    private int numberOfObjectives;

    public CombinationGeneratorStrategy1(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }    

    public List<Combination> generateCombinations() {
        List<Combination> combinations = new ArrayList<Combination>();

        for (int i = 0; i < this.numberOfObjectives; i += 2) {
            for (int j = 1; j < this.numberOfObjectives; j += 2) {
                Map<Integer, Integer> objsAssignedToFixedObjs = new HashMap<Integer, Integer>();

                for (int k = 0; k < this.numberOfObjectives; k++) {
                    if (k == i || k == j)
                        continue;
                    if (k % 2 == 0)
                        objsAssignedToFixedObjs.put(k, i);
                    else
                        objsAssignedToFixedObjs.put(k, j);
                }

                combinations.add(new Combination(i, j, objsAssignedToFixedObjs));
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

        CombinationGeneratorStrategy1 combinationGenerator = new CombinationGeneratorStrategy1(numberOfObjectives);
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
