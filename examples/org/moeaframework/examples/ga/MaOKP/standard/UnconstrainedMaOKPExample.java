package org.moeaframework.examples.ga.MaOKP.standard;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

public class UnconstrainedMaOKPExample {

	public static void main(String[] args) throws IOException {

		// Get the current working directory
		String currentDirectory = System.getProperty("user.dir");
		System.out.println("Current directory: " + currentDirectory);

		// Get the file separator for the current operating system
		String fileSeparator = System.getProperty("file.separator");

		// Define the name of the directory where the test problems are located
		String inputDirectoryPathName = currentDirectory + fileSeparator + "input" + fileSeparator;
		System.out.println("Input path: " + inputDirectoryPathName);

		String[] instances = new String[] { 
				// "knapsack_1_500_2",
				"knapsack_1_500_3", 
				"knapsack_1_500_4",
				// "knapsack_1_500_6",
				// "knapsack_1_500_8", 
				// "knapsack_1_500_10"				
		};		 

		String[] algorithms = new String[] { 
				// "NSGAII", 
				// "NSGAIII", 
				// "IBEA", 
				// "MOEAD",
				// "DBEA",
				// "RVEA"
				"AnD"
				};	
		
		for (String algorithm : algorithms) {
			
			System.out.println("\nAlgorithm: " + algorithm);

			for (String instanceName : instances) {

				String intancePath = inputDirectoryPathName + instanceName + ".txt";
				System.out.println("\nInstance path: " + intancePath);

				// Open the file containing the knapsack problem instance
				File inputFile = new File(intancePath);

				if (!inputFile.exists() || !inputFile.isFile()) {
					System.out.println("File does not exist or is not valid.");
					System.exit(-1);
				}

				// Create a MaOKP instance
				UnconstrainedMaOKP kpProblem = new UnconstrainedMaOKP(inputFile);
				kpProblem.SetName(instanceName);

				System.out.println("\nInstance name: " + kpProblem.getName());
				int numberOfObjectives = kpProblem.getNumberOfObjectives();
				System.out.println("Number of objectives: " + numberOfObjectives);

				run(kpProblem, algorithm, instanceName);

			}
		}

	}

	private static String getDirName(String instanceName) {

		if (instanceName.startsWith("knapsack"))
			return "Ishibuchi";

		String[] parts = instanceName.split("_");
		return parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1) + "s";
	}

	private static void run(UnconstrainedMaOKP kpProblem, String alg, String instanceName) throws IOException {

		Map<Integer, Integer> popSize = new HashMap<Integer, Integer>();
		popSize.put(2, 100);
		popSize.put(4, 120);
		popSize.put(6, 126);
		popSize.put(8, 120);
		popSize.put(10, 220);
		popSize.put(12, 220);
		popSize.put(14, 220);
		popSize.put(16, 220);
		popSize.put(18, 220);
		popSize.put(20, 220);

		TypedProperties typedProperties = new TypedProperties();

		int pSize = popSize.getOrDefault(kpProblem.getNumberOfObjectives(), 100);
		System.out.println("Population Size = " + pSize);

		typedProperties.setDouble("populationSize", pSize);
		typedProperties.setDouble("hux.rate", 0.8);
		typedProperties.setDouble("bf.rate", 2.0 / 500.0);
		typedProperties.setString("indicator", "epsilon");
		typedProperties.setDouble("neighborhoodSize", 0.1);

		// Obtém a data do dia corrente
		LocalDate currentDate = LocalDate.now();

		String dirName = "results/" + currentDate + "/executor/" + "/" 
					+ getDirName(instanceName) + "/"
				+ kpProblem.getNumberOfObjectives() + "/" 
				+ kpProblem.getNumberOfObjectives() + "_" 
				+ alg + "-MoeaFramework/";

		File directory = new File(dirName);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		int[] seeds = new int[] { 76, 6238, 8862, 4363, 6206, 3819, 2232, 252, 3098, 5295, 2748, 2903, 6180, 118, 476,
				7341, 5876, 665, 3977, 7079, 7632, 351, 3648, 948, 5604, 35, 40, 7058, 297, 1021, };

		for (int exec = 0; exec < 30; exec++) {

			Executor executor = new Executor()
					.withProblem(kpProblem)
					.withAlgorithm(alg)
					.withProperties(typedProperties.getProperties())					
					.withMaxEvaluations(100000)
					.distributeOnAllCores();

			System.out.println("Exec= " + exec + "   Seed= " + seeds[exec]);
			PRNG.setSeed(seeds[exec]);

			NondominatedPopulation approximationSet = executor.run();

			for (Solution sol : approximationSet) {
				kpProblem.changeToMaximizationProblem(sol);
			}

			PopulationIO.writeObjectives(new File(dirName + instanceName + "_approximationset_exec=" + exec + ".txt"),
					approximationSet);

		}

	}

}
