/*  Student information for assignment:
 *
 *  On <OUR> honor, <Shreyansh Dixit> and <Arushi Sharma),
 *  this programming assignment is <MY|OUR> own work
 *  and <We> have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: as224936
 *  email address: arushisharma@utexas.edu
 *  Grader name: Pranav Chandupatla
 *  Section number: 52570 
 *
 *  Student 2
 *  UTEID: sd42627
 *  email address: shreyansh.dixit@utexas.edu
 */

import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue<E extends Comparable<E>> implements Iterable<E> {
	LinkedList<E> con;

	public PriorityQueue() {
		con = new LinkedList<E>();
	}

	public void enqueue(E e) {
		// separate LinkedList to avoid concurrent modification error with iterator
		LinkedList<E> temp = new LinkedList<E>();
		boolean added = false;
		Iterator<E> it = con.iterator();
		for (int i = 0; i < con.size(); i++) {
			E current = it.next();
			// if e is less than current, add e to temp
			if (e.compareTo(current) < 0 && !added) {
				temp.add(e);
				added = true;
			}
			// add current to temp
			temp.add(current);
		}
		// if e is greater than all elements in con, add e to temp
		if (!added) {
			temp.add(e);
		}
		// set con to temp
		con = temp;
	}

	// remove and return the first element in the queue
	public E dequeue() {
		return con.removeFirst();
	}

	public int size() {
		return con.size();
	}

	public Iterator<E> iterator() {
		return con.iterator();
	}
}
