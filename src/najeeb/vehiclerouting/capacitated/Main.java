package najeeb.vehiclerouting.capacitated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {

	static String arg0;

	public static void main(String[] args) {
		if (args.length == 0)
			return;
		arg0 = args[0];
		Scanner sc = null;
		try {
			sc = new Scanner(new FileInputStream(new File(args[0])));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String[] firstline = sc.nextLine().split(" ");
		customers = new Customer[Integer.parseInt(firstline[0]) - 1];
		num_vehicles = Integer.parseInt(firstline[1]);
		capacity = Integer.parseInt(firstline[2]);
		for (int i = 0; i < customers.length + 1; i++) {
			String[] nextline = sc.nextLine().split(" ");
			if (i == 0) {
				depot_x = Double.parseDouble(nextline[1]);
				depot_y = Double.parseDouble(nextline[2]);
			} else {
				customers[i - 1] = new Customer(i, nextline[0], nextline[1], nextline[2]);
			}
		}
		sc.close();

		AntcolonyVRP solver = new AntcolonyVRP(customers, num_vehicles, capacity, depot_x, depot_y, computeRadius());
		LinkedList<Customer>[] best_solution = null;
		double best_obj = Double.POSITIVE_INFINITY;

		// SOLVER LOOP
		while (solver.objectiveFunction() == Double.POSITIVE_INFINITY) {
			int repeat = (customers.length < 500) ? customers.length * 12000 : 1;
			for (int i = 0; i < repeat; i++) {
				if (i % (customers.length * 10) == 0) {
					solver.ph_table.normalize();
				}
				solver.sendAnts();
				if (solver.objectiveFunction() < best_obj) {
					best_solution = solver.paths.clone();
					best_obj = solver.objectiveFunction();
				}
				solver.updatePheromoneTable();
			}
		}
//		File file = new File("solutions/" + args[0].substring(args[0].indexOf("/") + 1) + ".data");
//		try {
//			file.createNewFile();
//		} catch (IOException e1) {
//		}
//		FileWriter fw = null;
//		try {
//			fw = new FileWriter(file);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		PrintWriter pw = new PrintWriter(fw);
//		pw.println(best_obj + " 0");
		System.out.println(best_obj + " 0");
		for (LinkedList<Customer> tour : best_solution) {
			System.out.print(0 + " ");
			//pw.print(0 + " ");
			for (Customer c : tour) {
				System.out.print(c.index + " ");
				//pw.print(c.index + " ");
			}
			System.out.println("0");
			//pw.print("0\n");
		}
//		pw.close();
		solver.ph_table.writeData();
	}

	private static double computeRadius() {
		double max_dist = 0;
		for (Customer c : customers) {
			if (distToDepot(c) > max_dist)
				max_dist = distToDepot(c);
		}
		for (int i = 0; i < customers.length - 1; i++) {
			for (int j = i + 1; j < customers.length; j++) {
				if (distBetweenCustomers(customers[i], customers[j]) > max_dist)
					max_dist = distBetweenCustomers(customers[i], customers[j]);
			}
		}
		return max_dist;
	}

	static double distToDepot(Customer c) {
		double dx = c.x - depot_x;
		double dy = c.y - depot_y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	static double distBetweenCustomers(Customer c1, Customer c2) {
		double dx = c1.x - c2.x;
		double dy = c1.y - c2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	static double depot_x;
	static double depot_y;
	static int capacity;
	static int num_vehicles;
	static Customer[] customers;

}
