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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HuffmanTree {

	private final static int LEFTVAL = 0; // to check for integer 0
	private final static int RIGHTVAL = 1; // to check for integer 1

	/**
	 * Create an array to record the frequency of each character in the file, using
	 * the InputStream
	 * 
	 * @param in     is the stream being compressed (NOT a BitInputStream)
	 * @param fArray is the frequency array created in the SimpleHuffProcesser
	 * @return the frequency array
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public static int[] createFrequencyArray(InputStream in, int[] fArray) throws IOException {
		// create a new inputStream to store the bits from the file
		BitInputStream bitsVal = new BitInputStream(in);
		// create the array with the size of the alphabet + 1 (for pseof)
		fArray = new int[IHuffConstants.ALPH_SIZE + 1];
		int inbits = 0;
		// traverse through the BitStream and continue incremeting until you reach the
		// end of the file (inbits = -1)
		while ((inbits = bitsVal.readBits(IHuffConstants.BITS_PER_WORD)) != -1) {
			fArray[inbits]++;
		}
		// set the last val = 1 for pseof
		fArray[IHuffConstants.ALPH_SIZE] = 1;

		// return the array -> needs to be accessed in the SimpleHuff -
		return fArray;
	}

	/**
	 * Create a Tree based on the frequency array that is created using the priority
	 * queue
	 * 
	 * @param the root TreeNode
	 * @param the frequency array
	 * @return return the PriorityQueue that is created
	 */
	public static TreeNode createHuffmanTree(TreeNode r, int[] f) {
		r = null;// initialization of the Tree
		// create the queue
		PriorityQueue<TreeNode> pQueue = new PriorityQueue<TreeNode>();
		for (int i = 0; i < IHuffConstants.ALPH_SIZE + 1; i++) {
			// if the current character exists in the file once or more it is added to the
			// pQueue
			if (f[i] > 0) {
				// add in the int value (ASCII character value) as the index and then the
				// frquency at that specific index
				pQueue.enqueue(new TreeNode(i, f[i]));
			}
		}
		// traverse through the priorityQueue to
		while (pQueue.size() > 1) {
			// left & right are storing the characters
			TreeNode leftNode = pQueue.dequeue();
			TreeNode rightNode = pQueue.dequeue();
			// grab the frquency by adding in the left and the right's frequency
			TreeNode parentNode = new TreeNode(leftNode,
					leftNode.getFrequency() + rightNode.getFrequency(), rightNode);
			pQueue.enqueue(parentNode);
		}
		return pQueue.dequeue(); // return the root value only -> store that in the
									// SimpleHuffProcesser
	}

	/**
	 * Create a Map for the HuffProcessor using the HuffmanTree
	 * 
	 * @param the root TreeNode
	 * @param the HashMap
	 * @return return the map so it can be accessed in the HuffProcessor
	 */
	public static Map<Integer, String> createMap(TreeNode r, Map<Integer, String> map) {
		recurseHashMap(r, "", map);
		return map;
	}

	// a recursive method to walk the tree in order to get the encoding result
	// for each character
	public static void recurseHashMap(TreeNode n, String s, Map<Integer, String> m) {
		if (!n.isLeaf()) {
			recurseHashMap(n.getLeft(), s + "" + LEFTVAL, m);
			recurseHashMap(n.getRight(), s + "" + RIGHTVAL, m);
		} else {
			m.put(n.getValue(), s);
		}
	}
}