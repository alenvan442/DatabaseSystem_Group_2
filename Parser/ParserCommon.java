//contains methods common to DDLParser and DMLParser
package Parser;

import java.util.ArrayList;
import java.util.Scanner;

public static class ParserCommon { // extend me!

	// keywordCheck returns false if the passed label is a protected keyword, true
	// otherwise
	protected boolean keywordCheck(String label) {
		label = label.toLower();
		return !(label.equals("integer") ||
				label.equals("double") ||
				label.equals("boolean") ||
				label.equals("char") ||
				label.equals("varchar") ||
				label.equals("notnull") ||
				label.equals("primarykey") ||
				label.equals("unique") ||
				label.equals("distinct") ||
				label.equals("table") ||
				label.equals("create") ||
				label.equals("drop") ||
				label.equals("alter") ||
				label.equals("select") ||
				label.equals("into") ||
				label.equals("values") ||
				label.equals("add") ||
				label.equals("default"));
	}

	// Tokenize Splits the ddlStatement into a list of strings, while checking some
	// light semantics. There are a few types
	// "("
	// ")"
	// ","
	// ";"
	// Labels, which are alphanumeric and start with a letter, ths includes keywords
	// such as "Create"
	// Numbers, which can be integer or double with exactly 1 "." in any place, the
	// char/varchar case must check for double seperately.
	// any tokens outside these options will throw an error.
	protected ArrayList<String> Tokenize(String ddlStatement) {
		Scanner scanner = new Scanner(ddlStatement);
		ArrayList<String> tokens;
		String currentToken = "";
		char nextByte = (char) scanner.nextByte();
		boolean label = false;
		boolean number = false;
		boolean hasdecimal = false;
		while (scanner.hasNext()) {
			if (nextByte == '(' && !label && !number) {
				tokens.add("(");
				nextByte = (char) scanner.nextByte();
			} else if (nextByte == ')' && !label && !number) {
				tokens.add(")");
				nextByte = (char) scanner.nextByte();
			} else if (nextByte == ';' && !label && !number) {
				tokens.add(";");
				nextByte = (char) scanner.nextByte();
			} else if (nextByte == ',' && !label && !number) {
				tokens.add(",");
				nextByte = (char) scanner.nextByte();
			} else if ((Character.isDigit(nextByte) || (nextByte == "." && !hasdecimal)) && !label) // only ONE decimal
																									// point per double!
			{
				if (nextByte == '.') {
					hasdecimal = true;
				}
				currentToken += nextByte;
				number = true;
				nextByte = (char) scanner.nextByte();
			} else if (Character.isLetterOrDigit(nextByte)) // covering both labels and values in the same block since
															// values only come after "default" anyway
			{
				if (!label && !Character.isLetter(nextByte)) {
					throw new Exception("Labels must start with a letter!"); // realistically the digit branch should
																				// stop this from ever being accessible
				}
				currentToken += nextByte;
				label = true; // we need to block the other paths to know when to flush.
				nextByte = (char) scanner.nextByte();
			} else { // flush the label or number token string
				if (currentToken.equals("")) {
					throw new Exception("Invalid Token!");
				}
				tokens.add(currentToken);
				currentToken = "";
				number = false;
				label = false;
				hasdecimal = false;
			}
		}
	}
}