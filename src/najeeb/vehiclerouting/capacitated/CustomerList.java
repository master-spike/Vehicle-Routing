package najeeb.vehiclerouting.capacitated;

import java.util.Iterator;
import java.util.LinkedList;

public class CustomerList extends LinkedList<Customer> {

	public void insertBalanced(Customer c) {
		if (isEmpty()) {
			add(c);
			return;
		}
		Iterator<Customer> it = listIterator();
		int i = 0;
		while (it.next().demand < c.demand) {
			i++;
			if (!it.hasNext()) {
				it = null;
				add(c);
				return;
			}
		}
		it = null;
		add(i, c);
	}

	public void dropExcess(int max_capacity) {
		if (isEmpty())
			return;
		Iterator<Customer> it = listIterator();
		while (it.hasNext()) {
			if (it.next().demand > max_capacity) {
				it.remove();
			}
		}
	}

	private static final long serialVersionUID = 8952334853727480345L;

}
