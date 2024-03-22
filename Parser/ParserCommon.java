//contains methods common to DDLParser and DMLParser
package Parser;

import java.util.ArrayList;
import java.util.Scanner;

import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;

public class ParserCommon { // extend me!

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
	public static ArrayList<Token> Tokenize(String ddlStatement) throws Exception {
		ArrayList<Token> tokens = new ArrayList<>();
		String currentToken = "";

		boolean label = false;
		boolean number = false;
		boolean hasdecimal = false;
		boolean string = false;
		for (int stmti = 0; stmti < ddlStatement.length(); stmti++) {
			char nextByte = ddlStatement.charAt(stmti);
			if (nextByte == '(' && !label && !number && !string) {
				tokens.add(new Token(Type.L_PAREN, "("));
			} else if (nextByte == ')' && !label && !number && !string) {
				tokens.add(new Token(Type.R_PAREN, ")"));
			} else if (nextByte == ';' && !label && !number && !string) {
				tokens.add(new Token(Type.SEMICOLON, ";"));
			} else if (nextByte == ',' && !label && !number && !string) {
				tokens.add(new Token(Type.COMMA, ","));
			} else if (nextByte == '*' && !label && !number && !string) {
				tokens.add(new Token(Type.ASTERISK, "*"));
			} else if (nextByte == '>' || nextByte == '<' || nextByte == '=' || nextByte == '!') {
				StringBuilder relationalOperator = new StringBuilder();
				relationalOperator.append(nextByte);
				if (nextByte == '!' && ddlStatement.charAt(stmti + 1) == '=') {
					relationalOperator.append('=');
					stmti++;
				} else if ((nextByte == '>' || nextByte == '<') && ddlStatement.charAt(stmti + 1) == '=') {
					relationalOperator.append('=');
					stmti++;
				}
				tokens.add(new Token(Type.REL_OP, relationalOperator.toString()));
			} else if (nextByte == ' ' && !label && !number && !string) { // if space then ignore
			} else if (nextByte == '\"') { // have to parse string vals as well
				if (string) {
					currentToken += nextByte;
					currentToken = currentToken.replace("\"", "");
					tokens.add(new Token(Type.STRING, currentToken));
					currentToken = "";
					string = false;
					label = false;
				} else {
					string = true;
					currentToken += nextByte;
				}
			} else if ((Character.isDigit(nextByte) || (nextByte == '.' && !hasdecimal)) && !label) // only ONE decimal
			// point per double!
			{

				if (nextByte == '.') {
					hasdecimal = true;
				}
				currentToken += nextByte;
				number = true;
			} else if (Character.isLetterOrDigit(nextByte) || (nextByte == '.' && !hasdecimal)) // covering both labels and
																																													// values in the same block
																																													// since
			// values only come after "default" anyway
			{
				if (nextByte == '.') {
					hasdecimal = true;
				}
				currentToken += nextByte;
				label = true; // we need to block the other paths to know when to flush.
			}
			else { // flush the label or number token string
				currentToken = currentToken.toLowerCase();
				if (string) {
					currentToken += nextByte;
					continue;
				}
				if (number) {
					if (hasdecimal) {
						tokens.add(new Token(Type.DOUBLE, currentToken));
					} else {
						tokens.add(new Token(Type.INTEGER, currentToken));
					}
				}
				else if (label && hasdecimal) {
					tokens.add(new Token(Type.QUALIFIER, currentToken));
				}
				else if (label) {
					switch (currentToken) {
						case "notnull":
						case "primarykey":
						case "unique":
							tokens.add(new Token(Type.CONSTRAINT, currentToken));
							break;
						case "integer":
						case "double":
						case "boolean":
						case "null":
						case "char":
						case "varchar":
							tokens.add(new Token(Type.DATATYPE, currentToken));
							break;
						default:
							tokens.add(new Token(Type.IDKEY, currentToken));
					}
				} else {
						MessagePrinter.printMessage(MessageType.ERROR, "Invalid token " + currentToken);
				}
				currentToken = "";
				number = false;
				label = false;
				hasdecimal = false;
				stmti--;
			}
		}

		if (!currentToken.isEmpty()) {
			tokens.add(new Token(Type.IDKEY, currentToken.toLowerCase()));
		}

		return tokens;
	}
}