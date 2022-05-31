import java.util.*;
import java.io.*;

/**
 * A new instance of HuffmanCoding is created for every run. The constructor is
 * passed the full text to be encoded or decoded, so this is a good place to
 * construct the tree. You should store this tree in a field and then use it in
 * the encode and decode methods.
 */
public class HuffmanCoding {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please call this program with two arguments, which are " +
                               "the input file name and either 0 for constructing tree and printing it, or " +
                               "1 for constructing the tree and encoding the file and printing it, or " +
                               "2 for constructing the tree, encoding the file, and then decoding it and " +
                               "printing the result which should be the same as the input file.");
        } else {
            try {
                Scanner s = new Scanner(new File(args[0]));

                // Read the entire file into one String.
                StringBuilder fileText = new StringBuilder();
                while (s.hasNextLine()) {
                    fileText.append(s.nextLine() + "\n");
                }
                
                if (args[1].equals("0")) {
                    System.out.println(constructTree(fileText.toString()));
                } else if (args[1].equals("1")) {
                    constructTree(fileText.toString()); // initialises the tree field.
                    System.out.println(encode(fileText.toString()));
                } else if (args[1].equals("2")) {
                    constructTree(fileText.toString()); // initialises the tree field.
                    String codedText = encode(fileText.toString());
                     // DO NOT just change this code to simply print fileText.toString() back. ;-)
                    System.out.println(decode(codedText));
                } else {
                    System.out.println("Unknown second argument: should be 0 or 1 or 2");
                }
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file called " + args[0]);
            }
        }
    }

    // TODO add a field with your ACTUAL HuffmanTree implementation.
    private static Map<Character, String> tree; // Change type from Object to HuffmanTree or appropriate type you design.
    static HuffmanNode ROOT; // Needed for decoding

    /**
     * This would be a good place to compute and store the tree.
     */
    public static Map<Character, String> constructTree(String text) {
        // TODO Construct the ACTUAL HuffmanTree here to use with both encode and decode below.
        // TODO fill this in.

        Map<Character, Integer> frequencyTable = getFrequencyTable(text);
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        // Construct leaf node for each symbol
        // Put these nodes into a priority queue, with frequency as priority
        for (Map.Entry<Character, Integer> entry : frequencyTable.entrySet()) {
            Character charValue = entry.getKey();
            Integer frequency = entry.getValue();
            priorityQueue.offer(new HuffmanNode(charValue, frequency));
        }

        // Construct tree
        while (priorityQueue.size() > 1) {
            // Remove the top two nodes
            HuffmanNode leftNode = priorityQueue.remove();
            HuffmanNode rightNode = priorityQueue.remove();

            // Create a new tree with these two nodes as children
            HuffmanNode parentNode = new HuffmanNode(leftNode, rightNode);
            leftNode.setParent(parentNode);
            rightNode.setParent(parentNode);

            // Node frequency = sum of frequencies of the two nodes
            parentNode.setFrequency(leftNode.getFrequency() + rightNode.getFrequency());

            // Add new node to the queue
            priorityQueue.offer(parentNode);
        }

        // Final node is root of tree
        HuffmanNode root = priorityQueue.poll();
        ROOT = root;

        // Traverse this tree to assign codes:
        // If a node has code c, assign c0 to the left child and c1 to the right child
        Map<Character, String> charCodes = new HashMap<>();
        assignCodes(root, charCodes);
        tree = charCodes;
        
        return charCodes;
    }

    public static void assignCodes(HuffmanNode node, Map<Character, String> charCodes) {
        if (node.getLeftChild() == null && node.getRightChild() == null) {
            charCodes.put(node.getCharValue(), node.getCode());
            return;
        }

        HuffmanNode leftNode = node.getLeftChild();
        leftNode.setCode((node.getCode() + '0'));
        assignCodes(leftNode, charCodes);

        HuffmanNode rightNode = node.getRightChild();
        rightNode.setCode((node.getCode() + '1'));
        assignCodes(rightNode, charCodes);
    }

    public static Map<Character, Integer> getFrequencyTable(String text) {
        Map<Character, Integer> frequencyTable = new HashMap<>();

        for (Character c : text.toCharArray()) {
            if (!frequencyTable.containsKey(c)) {
                frequencyTable.put(c, 0);
            }

            int prev = frequencyTable.get(c);
            frequencyTable.put(c, prev + 1);
        }
        return frequencyTable;
    }
    
    /**
     * Take an input string, text, and encode it with the tree computed from the text. Should
     * return the encoded text as a binary string, that is, a string containing
     * only 1 and 0.
     */
    public static String encode(String text) {
        // TODO fill this in.

        StringBuilder encodeString = new StringBuilder();

        for (char c : text.toCharArray()) {
            encodeString.append(tree.get(c));
        }

        return encodeString.toString();
    }
    
    /**
     * Take encoded input as a binary string, decode it using the stored tree,
     * and return the decoded text as a text string.
     */
    public static String decode(String encoded) {
        // TODO fill this in.

        StringBuilder decodeString = new StringBuilder();
        HuffmanNode pointer = ROOT;

        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            
            // Check if left or right
            if (c == '0') {
                pointer = pointer.getLeftChild();
            } else {
                pointer = pointer.getRightChild();
            }

            // Found leaf node
            // Assign Pointer to the root
            if (pointer.getLeftChild() == null && pointer.getRightChild() == null) {
                decodeString.append(pointer.charValue);
                pointer = ROOT;
            }
        }

        return decodeString.toString();
    }
}

/**
 * Implementation of a HuffmanNode
 */
class HuffmanNode implements Comparable<HuffmanNode> {
    char charValue; 
    int frequency; // Number of times present in text
    String code = ""; // Unique code
    
    HuffmanNode leftChild;
    HuffmanNode rightChild;
    HuffmanNode parent;

    // Constructor
    public HuffmanNode(char charValue, int frequency) {
        this.charValue = charValue;
        this.frequency = frequency;
    }

    // Assigns two children nodes 
    // Frequency is the sum of the childrens frequencies
    public HuffmanNode(HuffmanNode leftChild, HuffmanNode rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.frequency = leftChild.getFrequency() + rightChild.getFrequency();

    }

    // Matching frequencies
    public char findLowestValueChar() {
        if (this.getLeftChild() == null && this.getRightChild() == null) {
            return this.getCharValue();
        }

        char left = this.getLeftChild().findLowestValueChar();
        char right = this.getRightChild().findLowestValueChar();

        char lowestChar;
        if (Character.compare(left, right) < 0) {
            lowestChar = left;
        } else {
            lowestChar = right;
        }
        return lowestChar;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        HuffmanNode curr = this;

        if (curr.getFrequency() < other.getFrequency()) {
            return -1;
        } else if (curr.getFrequency() > other.getFrequency()) {
            return 1;
        } else {
            char currLowest = curr.findLowestValueChar();
            char otherLowest = other.findLowestValueChar();
            int val = Character.compare(currLowest, otherLowest);

            if (val < 0) {
                return -1;
            } else if (val > 0) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return this.getCharValue() + this.getCode();
    }

    // Getters and setters
    public char getCharValue() {
        return charValue;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public HuffmanNode getLeftChild() {
        return leftChild;
    }

    public HuffmanNode getRightChild() {
        return rightChild;
    }

    public void setParent(HuffmanNode parent) {
        this.parent = parent;
    }
}
