/*  Student information for assignment:
 *
 *  On <OUR> honor, <Shreyansh Dixit> and <Arushi Sharma),
 *  this programming assignment is <MY|OUR> own work
 *  and <We> have not provided this code to any other student.
 *
 *  Number of slip days used:1
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
import java.io.OutputStream;
import java.util.*;

public class SimpleHuffProcessor implements IHuffProcessor {

	private IHuffViewer myViewer;
	private TreeNode root; // root of the Huffman tree
	private int[] frequency;
	private Map<Integer, String> encodingMap = new HashMap<Integer, String>(); // map of characters
																				// to their
																				// encodings
	private int compressedFormat; // to record which kind of format
	private int compressedBitsVal; // total compressed bits
	private int savedBitsVal; // to record how many bits saved
	private boolean preProcessRan; // to record whether preProcess has been ran

	private final static int LEFTVAL = 0;
	private final static int RIGHTVAL = 1;

	/**
	 * Preprocess data so that compression is possible --- count characters/create
	 * tree/store state so that a subsequent call to compress will work. The
	 * InputStream is <em>not</em> a BitInputStream, so wrap it int one as needed.
	 * 
	 * @param in           is the stream which could be subsequently compressed
	 * @param headerFormat a constant from IHuffProcessor that determines what kind
	 *                     of header to use, standard count format, standard tree
	 *                     format, or possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note, to
	 *         determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic number,
	 *         the header format number, the header to reproduce the tree, AND the
	 *         actual data.
	 * @throws IOException if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
		preProcessRan = true;
		compressedFormat = headerFormat; // record the format chosen by user
		frequency = HuffmanTree.createFrequencyArray(in, frequency); // return the frequency array
		root = HuffmanTree.createHuffmanTree(root, frequency); // grab the root from the frequency
																// array
		encodingMap = HuffmanTree.createMap(root, encodingMap); // grab the encoding map from the
																// root

		int countOrginalBits = ogTotalBits();
		compressedBitsVal = compressedTotalBits();
		// add the number of bits of magical number
		compressedBitsVal += IHuffConstants.BITS_PER_INT;
		// add the number of bits of STORAGE_COUNT or STORAGE_TREE constant
		compressedBitsVal += IHuffConstants.BITS_PER_INT;

		// add the number of bits of the header data
		if (headerFormat == STORE_COUNTS) {
			compressedBitsVal += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
		} else if (headerFormat == STORE_TREE) {
			compressedBitsVal += treeSize(root) + IHuffConstants.BITS_PER_INT;
		}
		// add number of bits for PSEUDO_EOF
		compressedBitsVal += encodingMap.get(IHuffConstants.PSEUDO_EOF).length();
		savedBitsVal = countOrginalBits - compressedBitsVal;

		return savedBitsVal;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously been
	 * pre-processed via <code>preprocessCompress</code> storing state used by this
	 * call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in    is the stream being compressed (NOT a BitInputStream)
	 * @param out   is bound to a file/stream to which bits are written for the
	 *              compressed file (not a BitOutputStream)
	 * @param force if this is true create the output file even if it is larger than
	 *              the input file. If this is false do not create the output file
	 *              if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {

		// check pre process
		if (!preProcessRan) {
			myViewer.showError("Pre-process needs to be called before this method.");
		}

		if (savedBitsVal > 0 || (savedBitsVal < 0 && force)) { // precondition
			BitOutputStream bos = new BitOutputStream(out);
			bos.writeBits(BITS_PER_INT, MAGIC_NUMBER); // write a sign for HuffCompressed file
			bos.writeBits(BITS_PER_INT, compressedFormat); // write the format being currently used

			// write out header
			if (compressedFormat == IHuffConstants.STORE_COUNTS) {
				for (int k = 0; k < IHuffConstants.ALPH_SIZE; k++) {
					bos.writeBits(BITS_PER_INT, frequency[k]);
				}
			} else if (compressedFormat == IHuffConstants.STORE_TREE) {
				bos.writeBits(BITS_PER_INT, treeSize(root));
				preOrderTraversal(root, bos);
			}
			BitInputStream bis = new BitInputStream(in);
			encodeData(bis, bos);

			// generate encoding result for PSEUDO_EOF constant
			String s = encodingMap.get(PSEUDO_EOF);
			stringToBits(s, bos);
			bos.close();
		} else {
			myViewer.showError(
					"Error: Force is not the correct value. The output file is larger than the input file.");
		}
		return compressedBitsVal;
	}

	// helper method to: convert the Strings to bits and write to the output file
	// for the encoding/decoding result
	// @param s is the string to be converted to bits
	// @param bos is the BitOutputStream to write bits to the output file
	private void stringToBits(String s, BitOutputStream bos) {
		for (int i = 0; i < s.length(); i++) {
			boolean codeNum = s.charAt(i) == RIGHTVAL;
			if (codeNum)
				bos.writeBits(1, 1);
			else
				bos.writeBits(1, 0);
		}
	}

	// helper method to: write the actually encoding data
	// @param bis is the BitInputStream to read bits from the input file
	// @param bos is the BitOutputStream to write bits to the output file
	// @throws IOException if an error occurs while reading from the input file.
	private void encodeData(BitInputStream bis, BitOutputStream bos) throws IOException {
		int previousBit = bis.readBits(BITS_PER_WORD);
		while (previousBit != -1) {
			// to get the encoding result from the map
			String s = encodingMap.get(previousBit);
			stringToBits(s, bos);
			// to read next character
			previousBit = bis.readBits(BITS_PER_WORD);
		}
	}

	// helper method to: preorder traversal of tree
	// @param root is the root of the tree
	// @param bos is the BitOutputStream to write bits to the output file
	private void preOrderTraversal(TreeNode rootNodeVal, BitOutputStream bos) {
		if (rootNodeVal.isLeaf()) {
			bos.writeBits(1, 1); // 1 indicates a leaf node
			bos.writeBits(BITS_PER_WORD + 1, rootNodeVal.getValue());
		} else {
			bos.writeBits(1, 0); // 0 indicates an internal node
			preOrderTraversal(rootNodeVal.getLeft(), bos);
			preOrderTraversal(rootNodeVal.getRight(), bos);
		}
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out. pre condition: check for the magic number value
	 * 
	 * @param in  is the previously compressed data (not a BitInputStream)
	 * @param out is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
		BitInputStream bis = new BitInputStream(in);
		BitOutputStream bos = new BitOutputStream(out);
		// to get magic number and check if this file can be uncompressed
		int magic = bis.readBits(BITS_PER_INT);
		if (magic != MAGIC_NUMBER) {
			myViewer.showError(
					"Error: Compressed file did not start with the huff magic number. Thus, the file can not be compressed");
			return -1;
		}

		// to read the constant
		compressedFormat = bis.readBits(BITS_PER_INT);

		// to read the header
		if (compressedFormat == STORE_COUNTS) {
			rebuildFrequencyArray(bis, bos);// get frequency array
			HuffmanTree.createHuffmanTree(root, frequency);// build a tree
		} else if (compressedFormat == STORE_TREE) {
			bis.readBits(BITS_PER_INT);// skip the size of tree
			root = rebuildTree(bis, root); // build a tree
		}

		// traverse through the tree
		int count = traverseTree(bis, bos);
		return count;
	}

	// helper method purpose: to get total number of bits of original file
	// @return the toal number of bits in the original file (needed for compression
	// later)
	private int ogTotalBits() {
		int total = 0;
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			// traverse through the frequency array and add the number of bits of each
			// character and add it to your int value
			total += frequency[i] * IHuffConstants.BITS_PER_WORD;
		}
		return total;
	}

	// helper method purpose: to get the total number of bits of the actual data
	// after compressed
	// @return the total number of bits of the actual data after compressed
	private int compressedTotalBits() {
		int count = 0;
		for (int key : encodingMap.keySet()) {
			// make sure that it didn't hit the end of the file yet
			if (key != IHuffConstants.PSEUDO_EOF) {
				String s = encodingMap.get(key);
				int freq = frequency[key];
				count += freq * s.length();
			}
		}
		return count;
	}

	// helper method: to count the size( number of bits ) of the tree
	// @param root the root of the tree
	// @return the number of bits of the tree
	private int treeSize(TreeNode currentRoot) {
		if (currentRoot.isLeaf()) {
			// if it is a leaf, add 1 to the count
			return 1 + (1 + IHuffConstants.BITS_PER_WORD);
			// 1 bits for a node itself and 9 bits for value
		} else {
			// 1 bits for a node itself and 0 bits for value
			return 1 + treeSize(currentRoot.getLeft()) + treeSize(currentRoot.getRight());
		}
	}

	// helper method: to walk the tree and find the leaf with its value
	// @param bis is the BitInputStream to read bits from the input file
	// @param bos is the BitOutputStream to write bits to the output file
	private int traverseTree(BitInputStream bis, BitOutputStream bos) throws IOException {
		// intialize your variables to traverse
		TreeNode temp = root;
		int counter = 0;
		int currentVal = 0;// make it enter the loop
		while (currentVal != -1) {
			currentVal = bis.readBits(1);
			if (currentVal == 1) {
				// 1 value indicates right tree
				temp = temp.getRight();
			} else {
				// 0 value indicates left tree
				temp = temp.getLeft();
			}
			// traverse through the tree and find the leaf with its value to add it to the
			// count
			if (temp.isLeaf()) {
				if (temp.getValue() != PSEUDO_EOF) {
					bos.writeBits(BITS_PER_WORD, temp.getValue());
					counter += BITS_PER_WORD;
				} else {
					// if current one is PSEUDO_EOF,it will end after this.
					currentVal = -1;
				}
				// reset the temp to the root
				temp = root;
			}
		}
		// return the bitCount
		return counter;
	}

	// helper method: read the header to rebuild a tree
	// @param bis is the BitInputStream to read bits from the input file
	// @param root is the root of the tree
	// @return the root of the tree
	private TreeNode rebuildTree(BitInputStream bis, TreeNode currentNodeVal) throws IOException {
		if (bis.readBits(1) == 1) {
			// find a leaf
			currentNodeVal = new TreeNode(bis.readBits(BITS_PER_WORD + 1), 0);
			return currentNodeVal;
		} else {
			currentNodeVal = new TreeNode(-1, 0);// -1 indicates that value is null
			currentNodeVal.setLeft(rebuildTree(bis, currentNodeVal.getLeft())); // grab the left
																				// value of the tree
			currentNodeVal.setRight(rebuildTree(bis, currentNodeVal.getRight())); // grab the right
																					// value of the
																					// tree
			return currentNodeVal;
		}

	}

	// helper method: read the header to rebuild a frequency array
	// @param bis is the BitInputStream to read bits from the input file
	// @param bos is the BitOutputStream to write bits to the output file
	// @throw IOException if an error occurs while reading from the input file or
	// writing to the output file.
	private void rebuildFrequencyArray(BitInputStream bis, BitOutputStream bos) throws IOException {
		frequency = new int[ALPH_SIZE + 1];
		for (int i = 0; i < ALPH_SIZE; i++) {
			int freq = bis.readBits(BITS_PER_INT);
			frequency[i] = freq;
		}
		frequency[ALPH_SIZE] = 1;
	}

	// given method
	private void showString(String s) {
		if (myViewer != null)
			myViewer.update(s);
	}

	// given method
	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}
}