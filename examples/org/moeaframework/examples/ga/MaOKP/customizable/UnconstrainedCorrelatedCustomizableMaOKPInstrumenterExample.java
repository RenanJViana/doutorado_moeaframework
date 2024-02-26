package org.moeaframework.examples.ga.MaOKP.customizable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UnconstrainedCorrelatedCustomizableMaOKPInstrumenterExample {

	public static void main(String[] args) throws IOException {

		// Get the current working directory
		String currentDirectory = System.getProperty("user.dir");
		System.out.println("Current directory: " + currentDirectory);

		// Get the file separator for the current operating system
		String fileSeparator = System.getProperty("file.separator");

		// Define the name of the directory where the test problems are located
		String inputDirectoryPathName = currentDirectory + fileSeparator + "input";
		System.out.println("Input path: " + inputDirectoryPathName);

		int instanceIndex = 0;
		String[] instances = new String[] { "knapsack_1_500_4", "knapsack_1_500_6", "knapsack_1_500_8",
				"knapsack_1_500_10" };		
		
		String intancePath = inputDirectoryPathName + fileSeparator + instances[instanceIndex] + ".txt";
		System.out.println(intancePath + "\n");
		
		// Open the file containing the knapsack problem instance		
		File inputFile = new File(intancePath);
		
		if (!inputFile.exists() || !inputFile.isFile()) {
			System.out.println("File does not exist or is not valid.");
			System.exit(-1);
		}		
		
		// Create a MaOKP instance
		UnconstrainedCorrelatedCustomizableMaOKP kpProblem = new UnconstrainedCorrelatedCustomizableMaOKP(inputFile);
		kpProblem.SetName(instances[instanceIndex]);	
		System.out.println("Instance name: " + kpProblem.getName());		
		
		CombinationGeneratorStrategy1 combinationGenerator = new CombinationGeneratorStrategy1(kpProblem.getNumberOfObjectives());
        List<Combination> combinations = combinationGenerator.generateCombinations();
        
        for (Combination combination : combinations) {            
            kpProblem.createFormulation(combination, 0.2);
            kpProblem.displayFormulation();            
        }

	}
}
