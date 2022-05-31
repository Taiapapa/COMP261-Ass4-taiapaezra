import java.util.*;
import java.io.*;

public class KMP {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please call this program with " +
                               "two arguments which is the input file name " +
                               "and the string to search.");
        } else {
            try {
                Scanner s = new Scanner(new File(args[0]));

                // Read the entire file into one String.
                StringBuilder fileText = new StringBuilder();
                while (s.hasNextLine()) {
                    fileText.append(s.nextLine() + "\n");
                }

                System.out.println(search(fileText.toString(), args[1]));
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file called " + args[0]);
            }
        }
    }

    /**
     * Perform KMP substring search on the given text with the given pattern.
     * 
     * This should return the starting index of the first substring match if it
     * exists, or -1 if it doesn't.
     */
    public static int search(String text, String pattern) {
        // TODO

        // Initialise jump table
        int[] jumpTableM = buildJumpTable(pattern);

        int k = 0; // Start of the current match in text
        int i = 0; // Position of the current character in pattern

        while (k + i < text.length()) {
            if (pattern.charAt(i) == text.charAt(k + i)) { // Match at i
                i = i + 1;
                // Check if i reaches the end of the searching pattern
                if (i == pattern.length()) {
                    return k; // Found string
                }
            } else if (jumpTableM[i] == -1) { // Mismatch, no self overlap
                k = k + i + 1;
                i = 0;
            } else { // Mismatch, with self overlap
                k = k + i - jumpTableM[i]; // Match position jumps forward
                i = jumpTableM[i]; // Move jump table index as well
            }
        }

        return -1; // Failed to find string
    }

    public static int[] buildJumpTable(String pattern) {
        int m = pattern.length();
        int[] jumpTableM = new int[m]; // Jump table

        jumpTableM[0] = -1; // Position in prefix
        jumpTableM[1] = 0; // Length of match

        int j = 0; // Position in table
        int pos = 2;

        while (pos < m) {
            if (pattern.charAt(pos - 1) == pattern.charAt(j)) {
                j++;
                jumpTableM[pos] = j;
                pos++;
            } else if (j > 0) {
                j = jumpTableM[j];
            } else {
                jumpTableM[pos] = 0;
                pos++;
            }
        }
        return jumpTableM;
    }
}
