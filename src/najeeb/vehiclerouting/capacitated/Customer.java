package najeeb.vehiclerouting.capacitated;

public class Customer {

	int demand;
	int index;
	double x;
	double y;

	public Customer(int index, int demand, double x, double y) {
		this.demand = demand;
		this.x = x;
		this.y = y;
		this.index = index;
	}

	public Customer(int index, String demand, String x, String y) {
		this.demand = Integer.parseInt(demand);
		this.x = Double.parseDouble(x);
		this.y = Double.parseDouble(y);
		this.index = index;
	}

}
