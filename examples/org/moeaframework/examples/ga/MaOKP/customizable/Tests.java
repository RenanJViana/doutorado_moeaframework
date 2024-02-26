package org.moeaframework.examples.ga.MaOKP.customizable;

import java.io.File;

public class Tests {

	public static void main(String[] args) {

		// Get the current working directory
        String currentDirectory = System.getProperty("user.dir");
        
        // Print the current working directory
        System.out.println("Current directory: " + currentDirectory);
        
        // Create a File object representing the current directory
        File directory = new File(currentDirectory);

        // List all files and directories in the current directory
		/*
		 * File[] files = directory.listFiles();
		 * 
		 * if (files != null) { // Iterate through the files/directories and print only
		 * directories for (File file : files) { if (file.isDirectory()) {
		 * System.out.println("Directory: " + file.getName()); } } }
		 */
        
        // Get the file separator for the current operating system
        String fileSeparator = System.getProperty("file.separator");
        
        // Append the subdirectory name to the current directory
        String subdirectoryPath = currentDirectory + fileSeparator + "input";
        System.out.println("Subdirectory path: " + subdirectoryPath);
        
        File inputDirectoryPath = new File(subdirectoryPath);
        File[] inputFiles = inputDirectoryPath.listFiles();

        if (inputFiles != null) {
            // Iterate through the files/directories and print only directories
            for (File file : inputFiles) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                }
            }
        }

	}

}
