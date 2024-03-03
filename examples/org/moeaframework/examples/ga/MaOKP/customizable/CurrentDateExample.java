package org.moeaframework.examples.ga.MaOKP.customizable;

import java.time.LocalDate;

public class CurrentDateExample {

	public static void main(String[] args) {
		
		// Obtém a data do dia corrente
        LocalDate currentDate = LocalDate.now();

        // Imprime a data do dia corrente
        System.out.println("Data do dia corrente: " + currentDate);

	}

}
