package najeeb.vehiclerouting.capacitated;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PheromoneTable {

	double[][] pheromones;

	public PheromoneTable(int num_customers) {
		pheromones = new double[num_customers + 1][num_customers + 1];
		initialize();
	}

	public void addPheromone(double add, Customer c1, Customer c2) {
		if (c2 == null) {
			pheromones[0][c1.index] += add;
			return;
		}
		if (c1.index < c2.index)
			pheromones[c1.index][c2.index] += add;
		else
			pheromones[c2.index][c1.index] += add;
	}

	public void normalize() {
		for (int i = 0; i < pheromones.length; i++) {
			double a = 0;
			for (int j = 0; j < i; j++) {
				a += pheromones[j][i];
			}
			double b = 0;
			for (int j = i; j < pheromones.length; j++) {
				b += pheromones[i][j];
			}
			double k = (1 - a) / b;
			for (int j = i; j < pheromones.length; j++) {
				pheromones[i][j] *= k;
			}
		}
	}

	public void initialize() {
		double initial_pheromone = 1d / (double) (pheromones.length - 1);
		for (int i = 0; i < pheromones.length - 1; i++) {
			for (int j = i + 1; j < pheromones.length; j++) {
				pheromones[i][j] = initial_pheromone;
			}
		}
		int[] columns = new int[pheromones.length];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = i;
		}
	}

	public double pheromoneBetweenCustomers(Customer c1, Customer c2) {
		if (c1.index < c2.index)
			return pheromones[c1.index][c2.index];
		else
			return pheromones[c2.index][c1.index];
	}

	public double pheromoneFromDepot(Customer c) {
		return pheromones[0][c.index];
	}

	public void writeData() {
		try {
			File file = new File("pher_" + Main.arg0 + "_" + (System.currentTimeMillis() % 86400000) + ".data");
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			PrintWriter writer = new PrintWriter(fw);
			for (int i = 0; i < pheromones.length; i++) {
				for (int j = 0; j < pheromones.length; j++) {
					if (i < j)
						writer.print(pheromones[i][j] + " ");
					else
						writer.print(pheromones[j][i] + " ");
				}
				writer.println();
			}
			writer.close();
		} catch (IOException e) {
		}
	}

}
