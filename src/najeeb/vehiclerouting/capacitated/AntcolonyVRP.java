package najeeb.vehiclerouting.capacitated;

import java.util.Iterator;

import java.util.LinkedList;
import najeeb.tsp.Vertex;
import najeeb.tsp.SolverTSP;

public class AntcolonyVRP {

	PheromoneTable ph_table;
	int vehicles;

	int capacity;
	int total_demand;

	double radius;

	double depot_x;
	double depot_y;

	// This will remain in order
	Customer[] all_customers;
	LinkedList<Customer>[] paths;

	public AntcolonyVRP(Customer[] customers, int vehicles, int capacity, double depot_x, double depot_y,
			double radius) {
		ph_table = new PheromoneTable(customers.length);
		all_customers = customers;
		this.vehicles = vehicles;
		this.capacity = capacity;
		this.depot_x = depot_x;
		this.depot_y = depot_y;
		this.radius = radius;
		total_demand = 0;
		for (Customer c : customers) {
			total_demand += c.demand;
		}
	}

	@SuppressWarnings("unchecked")
	void sendAnts() {
		paths = new LinkedList[vehicles];
		CustomerList unassigned = new CustomerList();
		int demand_to_satisfy = total_demand;
		for (int i = 0; i < all_customers.length; i++) {
			unassigned.insertBalanced(all_customers[i]);
		}
		for (int i = 0; i < vehicles; i++) {
			paths[i] = new LinkedList<Customer>();
			int x = capacity;
			int min_to_satisfy = (demand_to_satisfy - 1) / (vehicles - i) + 1;
			Customer prev = null;
			MakeTour: for (CustomerList options = (CustomerList) unassigned.clone(); !options.isEmpty(); options
					.dropExcess(x)) {
				double a = 0d;
				if (prev != null && capacity - x > min_to_satisfy) {
					a += ph_table.pheromoneFromDepot(prev);
				}
				Iterator<Customer> it = options.iterator();
				while (it.hasNext()) {
					if (prev == null)
						a += ph_table.pheromoneFromDepot(it.next());
					else
						a += ph_table.pheromoneBetweenCustomers(prev, it.next());
				}
				it = options.iterator();
				while (it != null && it.hasNext()) {
					Customer cand = it.next();
					if (prev == null) {
						if (ph_table.pheromoneFromDepot(cand) / a > Math.random()) {
							it = null;
							paths[i].add(cand);
							options.remove(cand);
							unassigned.remove(cand);
							prev = cand;
							demand_to_satisfy -= cand.demand;
							x -= cand.demand;
						} else {
							a -= ph_table.pheromoneFromDepot(cand);
						}
					} else {
						if (ph_table.pheromoneBetweenCustomers(prev, cand) / a > Math.random()) {
							it = null;
							paths[i].add(cand);
							options.remove(cand);
							unassigned.remove(cand);
							prev = cand;
							demand_to_satisfy -= cand.demand;
							x -= cand.demand;
						} else {
							a -= ph_table.pheromoneBetweenCustomers(prev, cand);
						}
					}
				}
				if (it != null)
					break MakeTour;
			}

		}
		if (unassigned.isEmpty()) {
			optimizeRoutes(0.5);
			all_assigned = true;
		} else {
			all_assigned = false;
		}

	}

	void optimizeRoutes(double e) {
		for (int j = 0; j < vehicles; j++) {
			if (paths[j].size() < 3)
				continue;
			Vertex[] verts = new Vertex[paths[j].size() + 1];
			verts[0] = new Vertex(depot_x, depot_y, 0);
			Iterator<Customer> it = paths[j].iterator();
			for (int i = 1; i < paths[j].size() + 1; i++) {
				Customer c = it.next();
				verts[i] = new Vertex(c.x, c.y, c.index);
			}
			SolverTSP tsp = new SolverTSP(verts, 0.4 * paths[j].size());
			tsp.solve();
			LinkedList<Customer> optimized_route = new LinkedList<Customer>();
			for (int i = 1; optimized_route.size() < paths[j].size();) {
				int ind = tsp.vertices[i].index;
				it = paths[j].iterator();
				while (it.hasNext()) {
					Customer c = it.next();
					if (c.index == ind) {
						optimized_route.add(c);
						i++;
						if (i >= tsp.vertices.length) break;
						ind = tsp.vertices[i].index;
					}
				}
				it = null;
			}
			paths[j] = optimized_route;
		}
	}

	void updatePheromoneTable() {
		double pher_add = objectiveToPheromoneAddAmount(objectiveFunction());
		for (LinkedList<Customer> path : paths) {
			Iterator<Customer> it = path.iterator();
			if (!it.hasNext())
				continue;
			Customer prev = it.next();
			ph_table.addPheromone(pher_add, prev, null);
			while (it.hasNext()) {
				Customer next = it.next();
				ph_table.addPheromone(pher_add, prev, next);
				prev = next;
			}
			ph_table.addPheromone(pher_add, prev, null);
		}
		//ph_table.normalize();
	}

	double objectiveFunction() {
		if (!legalityFunction()) {
			return Double.POSITIVE_INFINITY;
		}
		double sum = 0d;
		for (LinkedList<Customer> path : paths) {
			Iterator<Customer> it = path.iterator();
			if (!it.hasNext())
				continue;
			Customer prev = it.next();
			sum += distToDepot(prev);
			while (it.hasNext()) {
				Customer next = it.next();
				sum += distBetweenCustomers(prev, next);
				prev = next;
			}
			sum += distToDepot(prev);
		}
		return sum;
	}

	boolean all_assigned;

	boolean legalityFunction() {
		if (!all_assigned) {
			return false;
		}
		for (LinkedList<Customer> tour : paths) {
			int sum = 0;
			for (Customer c : tour) {
				sum += c.demand;
				if (sum > capacity)
					return false;
			}
		}
		return true;
	}

	double distToDepot(Customer c) {
		double dx = c.x - depot_x;
		double dy = c.y - depot_y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	double distBetweenCustomers(Customer c1, Customer c2) {
		double dx = c1.x - c2.x;
		double dy = c1.y - c2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	double objectiveToPheromoneAddAmount(double obj_val) {
		return radius / obj_val / all_customers.length;
	}

}
