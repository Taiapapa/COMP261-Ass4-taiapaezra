import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import javax.swing.JFileChooser;

/**
 * The parser and interpreter. The top level parse function, a main method for
 * testing, and several utility methods are provided. You need to implement
 * parseProgram and all the rest of the parser.
 */
public class Parser {

	/**
	 * Top level parse method, called by the World
	 */
	static RobotProgramNode parseFile(File code) {
		Scanner sc = null;
		try {
			sc = new Scanner(code);

			// the only time tokens can be next to each other is
			// when one of them is one of (){},;
			sc.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");

			RobotProgramNode n = parseProgram(sc); // You need to implement this!!!

			sc.close();
			return n;
		} catch (FileNotFoundException e) {
			System.out.println("Robot program source file not found");
		} catch (ParserFailureException e) {
			System.out.println("Parser error:");
			System.out.println(e.getMessage());
			sc.close();
		}
		return null;
	}

	/** For testing the parser without requiring the world */

	public static void main(String[] args) {
		if (args.length > 0) {
			for (String arg : args) {
				File f = new File(arg);
				if (f.exists()) {
					System.out.println("Parsing '" + f + "'");
					RobotProgramNode prog = parseFile(f);
					System.out.println("Parsing completed ");
					if (prog != null) {
						System.out.println("================\nProgram:");
						System.out.println(prog);
					}
					System.out.println("=================");
				} else {
					System.out.println("Can't find file '" + f + "'");
				}
			}
		} else {
			while (true) {
				JFileChooser chooser = new JFileChooser(".");// System.getProperty("user.dir"));
				int res = chooser.showOpenDialog(null);
				if (res != JFileChooser.APPROVE_OPTION) {
					break;
				}
				RobotProgramNode prog = parseFile(chooser.getSelectedFile());
				System.out.println("Parsing completed");
				if (prog != null) {
					System.out.println("Program: \n" + prog);
				}
				System.out.println("=================");
			}
		}
		System.out.println("Done");
	}

	// Useful Patterns

	static Pattern NUMPAT = Pattern.compile("-?\\d+"); // ("-?(0|[1-9][0-9]*)");
	static Pattern OPENPAREN = Pattern.compile("\\(");
	static Pattern CLOSEPAREN = Pattern.compile("\\)");
	static Pattern OPENBRACE = Pattern.compile("\\{");
	static Pattern CLOSEBRACE = Pattern.compile("\\}");

	/**
	 * See assignment handout for the grammar.
	 */
	static RobotProgramNode parseProgram(Scanner sc) {
		// THE PARSER GOES HERE

		// List of all program nodes
		List<RobotProgramNode> nodeList = new ArrayList<>();

		// Iterate through list
		while (sc.hasNext()) {
			nodeList.add(parseSTMT(sc));
		}

		sc.close();

		return new PROG(nodeList);
	}

	// utility methods for the parser

	/**
	 * Report a failure in the parser.
	 */
	static void fail(String message, Scanner sc) {
		String msg = message + "\n   @ ...";
		for (int i = 0; i < 5 && sc.hasNext(); i++) {
			msg += " " + sc.next();
		}
		throw new ParserFailureException(msg + "...");
	}

	/**
	 * Requires that the next token matches a pattern if it matches, it consumes
	 * and returns the token, if not, it throws an exception with an error
	 * message
	 */
	static String require(String p, String message, Scanner sc) {
		if (sc.hasNext(p)) {
			return sc.next();
		}
		fail(message, sc);
		return null;
	}

	static String require(Pattern p, String message, Scanner sc) {
		if (sc.hasNext(p)) {
			return sc.next();
		}
		fail(message, sc);
		return null;
	}

	/**
	 * Requires that the next token matches a pattern (which should only match a
	 * number) if it matches, it consumes and returns the token as an integer if
	 * not, it throws an exception with an error message
	 */
	static int requireInt(String p, String message, Scanner sc) {
		if (sc.hasNext(p) && sc.hasNextInt()) {
			return sc.nextInt();
		}
		fail(message, sc);
		return -1;
	}

	static int requireInt(Pattern p, String message, Scanner sc) {
		if (sc.hasNext(p) && sc.hasNextInt()) {
			return sc.nextInt();
		}
		fail(message, sc);
		return -1;
	}

	/**
	 * Checks whether the next token in the scanner matches the specified
	 * pattern, if so, consumes the token and return true. Otherwise returns
	 * false without consuming anything.
	 */
	static boolean checkFor(String p, Scanner sc) {
		if (sc.hasNext(p)) {
			sc.next();
			return true;
		} else {
			return false;
		}
	}

	static boolean checkFor(Pattern p, Scanner sc) {
		if (sc.hasNext(p)) {
			sc.next();
			return true;
		} else {
			return false;
		}
	}

	// Utility parsing methods
	// STMT parser
	static STMT parseSTMT(Scanner sc) {
		// Check if either loop or act and parse accordingly
		if (sc.hasNext("loop")) {
			return parseLOOP(sc);
		} else if (sc.hasNext("move|turnL|turnR|takeFuel|wait")) {
			return parseACT(sc);
		} else { // Undefined statement
			fail("Invalid STMT! Cannot continue parsing!", sc); // FIX: Null, so doesn't reach the brace check.
			return null;
		}
	}

	// LOOP parser
	static STMT parseLOOP(Scanner sc) {
		boolean isMatch = checkFor("loop", sc);

		// Not a loop
		if (!isMatch) {
			fail("Missing loop!", sc);
		}

		return new LOOP(parseBLOCK(sc));
	}

	// ACT parser
	static ACT parseACT(Scanner sc) {
		// List of acts
		boolean validAction = sc.hasNext("move|turnL|turnR|takeFuel|wait");

		// Check if valid act
		if (!validAction) {
			fail("Invalid action!", sc);
		}

		ACT node = null;

		// Confirm next token is act and return new act node
		if (sc.hasNext("move")) { // Move
			boolean isValid = checkFor("move", sc);

			if (!isValid) {
				fail("move action not found!", sc);
			}

			node = new MOVE();
		} else if (sc.hasNext("turnL")) { // TurnL
			boolean isValid = checkFor("turnL", sc);

			if (!isValid) {
				fail("turnL action not found!", sc);
			}

			node = new TURNL();
		} else if (sc.hasNext("turnR")) { // TurnR
			boolean isValid = checkFor("turnR", sc);

			if (!isValid) {
				fail("turnR action not found!", sc);
			}

			node = new TURNR();
		} else if (sc.hasNext("takeFuel")) { // TakeFuel
			boolean isValid = checkFor("takeFuel", sc);

			if (!isValid) {
				fail("take fuel action not found!", sc);
			}

			node = new TAKEFUEL();
		} else if (sc.hasNext("wait")) { // Wait
			boolean isValid = checkFor("wait", sc);

			if (!isValid) {
				fail("wait action not found!", sc);
			}

			node = new WAIT();
		} else { // Undefined action
			fail("No valid actions found!", sc);
			return null;
		}

		// Endline check
		if (!checkFor(";", sc)) {
			fail("Invalid: ';' endline is missing", sc);
		}

		return node;
	}

	// BLOCK parser
	// Needs fixing, checks in wrong place
	static BLOCK parseBLOCK(Scanner sc) {
		// No opening brace
		if (!checkFor(OPENBRACE, sc)) {
			fail("Invalid Block: '{' is missing!", sc);
		}

		List<STMT> stmtNodes = new ArrayList<>();

		while (!sc.hasNext(CLOSEBRACE)) {
			stmtNodes.add(parseSTMT(sc));
		}

		// Null
		if (stmtNodes.isEmpty()) {
			fail("Invalid Block: null statement!", sc);
		}

		// No closing brace
		if (!checkFor(CLOSEBRACE, sc)) {
			fail("Invalid Block: '}' is missing!", sc);
		}

		return new BLOCK(stmtNodes);
	}
}

// You could add the node classes here, as long as they are not declared public
// (or private)

/**
 * Node class for PROG
 */
class PROG implements RobotProgramNode {
	private List<RobotProgramNode> stmtNodeList;

	// Constructor
	PROG(List<RobotProgramNode> stmt) {
		this.stmtNodeList = stmt;
	}

	@Override
	public void execute(Robot robot) {
		for (RobotProgramNode node : stmtNodeList) {
			node.execute(robot);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (RobotProgramNode node : stmtNodeList) {
			sb.append("\n");
			sb.append(node.toString());
		}

		return sb.toString();
	}
}

/**
 * Node class for STMT
 */
class STMT implements RobotProgramNode {
	private RobotProgramNode children;

	// Constructor for act
	public STMT() {
	}

	// Constructor
	public STMT(RobotProgramNode node) {
		this.children = node;
	}

	@Override
	public void execute(Robot robot) {
		if (children == null) {
			return;
		}
		children.execute(robot);
	}

	public String toString() {
		return children.toString();
	}
}

/**
 * Node class for ACT
 */
abstract class ACT extends STMT {
	// Implement acts as methods?

	public abstract void execute(Robot robot);
	
	public abstract String toString();
}

/**
 * Node class for LOOP
 */
class LOOP extends STMT {
	private RobotProgramNode block;

	// Constructor
	public LOOP(RobotProgramNode block) {
		this.block = block;
	}

	public void execute(Robot robot) {
		while (true) {
			block.execute(robot);
		}
	}

	public String toString() {
		return "loop \n" + this.block + "\n";
	}
}

/**
 * Node class for BLOCK
 */
class BLOCK implements RobotProgramNode {
	private List<STMT> statements;

	// Constructor
	public BLOCK(List<STMT> statementList) {
		this.statements = statementList;
	}

	public void addStatement(STMT PROGNode) {
		this.statements.add(PROGNode);
	}

	public List<STMT> getStatement() {
		return statements;
	}

	public void execute(Robot robot) {
		for (STMT statement : statements) {
			statement.execute(robot);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("{\n");

		for (STMT statement : statements) {
			sb.append(statement.toString());
			sb.append("\n");
		}

		sb.append("\n}");

		return sb.toString();
	}
}

class MOVE extends ACT {
	public void execute(Robot robot) {
		robot.move();
	}

	public String toString() {
		return "move;";
	}
}

class TURNL extends ACT {
	public void execute(Robot robot) {
		robot.turnLeft();
	}

	public String toString() {
		return "turnL;";
	}
}

class TURNR extends ACT {
	public void execute(Robot robot) {
		robot.turnRight();
	}

	public String toString() {
		return "turnR;";
	}
}

class TAKEFUEL extends ACT {
	public void execute(Robot robot) {
		robot.takeFuel();
	}

	public String toString() {
		return "takeFuel;";
	}
}

class WAIT extends ACT {
	public void execute(Robot robot) {
		robot.idleWait();
	}

	public String toString() {
		return "wait;";
	}
}

// STAGE 0