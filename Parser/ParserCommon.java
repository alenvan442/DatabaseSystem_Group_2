//contains methods common to DDLParser and DMLParser
package Parser;

import java.util.ArrayList;
import java.util.Scanner;

public class ParserCommon { // extend me!

	// keywordCheck returns false if the passed label is a protected keyword, true
	// otherwise
	protected static boolean keywordCheck(String label){
		label = label.toLowerCase();
		return (!(label.equals("integer") ||
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
				label.equals("default")));
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
	public static ArrayList<String> Tokenize(String ddlStatement) throws Exception {
		ArrayList<String> tokens = new ArrayList<>();
		String currentToken = "";

		boolean label = false;
		boolean number = false;
		boolean hasdecimal = false;
		boolean string = false;
		for (int stmti = 0; stmti < ddlStatement.length() ; stmti++) {
			char nextByte = ddlStatement.charAt(stmti);
			if (nextByte == '(' && !label && !number && !string) {
				tokens.add("(");
			} else if (nextByte == ')' && !label && !number && !string) {
				tokens.add(")");
			} else if (nextByte == ';' && !label && !number && !string) {
				tokens.add(";");
			} else if (nextByte == ',' && !label && !number && !string) {
				tokens.add(",");
			} else if (nextByte == '*' && !label && !number && !string) {
				tokens.add("*");
			} else if (nextByte == ' ' && !label && !number && !string) { //if space then ignore
			} else if (nextByte == '_' && label) {
				currentToken += nextByte;
			} else if (nextByte == '\"' && !label && !number || string){ //have to parse string vals as well
				if(string)
				{
					if(nextByte != '\"')
					{
						currentToken += nextByte;
					} else
					{
						currentToken += nextByte;
						tokens.add(currentToken);
						currentToken = "";
						string = false;
					}
				} else
				{
					currentToken += nextByte;
					string = true;
				}
			} else if ((Character.isDigit(nextByte) || (nextByte == '.' && !hasdecimal)) && !label) // only ONE decimal
																									// point per double!
			{
				if (nextByte == '.') {
					hasdecimal = true;
				}
				currentToken += nextByte;
				number = true;
			} else if (Character.isLetterOrDigit(nextByte)) // covering both labels and values in the same block since
															// values only come after "default" anyway
			{
				if (!label && !Character.isLetter(nextByte)) {
					throw new Exception("Labels must start with a letter!"); // realistically the digit branch should
																				// stop this from ever being accessible
				}
				currentToken += nextByte;
				label = true; // we need to block the other paths to know when to flush.
			} else { // flush the label or number token string
				if (currentToken.equals("")) {
					throw new Exception("Invalid Token!");
				}
				tokens.add(currentToken);
				currentToken = "";
				number = false;
				label = false;
				hasdecimal = false;
				stmti--;
			}
		}
		return tokens;
	}
}