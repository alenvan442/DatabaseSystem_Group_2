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
	public static ArrayList<Token> Tokenize(String ddlStatement) throws Exception {
		ArrayList<Token> tokens = new ArrayList<>();
		String currentToken = "";

		boolean label = false;
		boolean number = false;
		boolean hasdecimal = false;
		boolean string = false;
		boolean charvar = false;
		String prior = "";
		String latter = "";
		for (int stmti = 0; stmti < ddlStatement.length() ; stmti++) {
			char nextByte = ddlStatement.charAt(stmti);
			if (nextByte == '(' && !label && !number && !string) {
				tokens.add(new Token(Type.L_PAREN,"("));
			} else if (nextByte == ')' && !label && !number && !string) {
				tokens.add(new Token(Type.R_PAREN, ")"));
			} else if (nextByte == ';' && !label && !number && !string) {
				tokens.add(new Token(Type.SEMICOLON, ";"));
			} else if (nextByte == ',' && !label && !number && !string) {
				tokens.add(new Token(Type.COMMA, ","));
			} else if (nextByte == '*' && !label && !number && !string) {
				tokens.add(new Token(Type.ASTERISK, "*"));
			} else if (nextByte == '>'){
				if(ddlStatement.charAt(stmti+1) == '='){
					tokens.add(new Token(Type.REL_OP, ">="));
					stmti++;
				} else {
					tokens.add(new Token(Type.REL_OP, ">"));
				}
			} else if (nextByte == '<') {
				if (ddlStatement.charAt(stmti + 1) == '=') {
					tokens.add(new Token(Type.REL_OP, "<="));
					stmti++;
				} else {
					tokens.add(new Token(Type.REL_OP, "<"));
				}
			} else if (nextByte == '!') {
				if (ddlStatement.charAt(stmti + 1) == '=') {
					tokens.add(new Token(Type.REL_OP, "!="));
					stmti++;
				} else {
					throw new Exception("'!=' expected!");
				}
			} else if (nextByte == '='){
					tokens.add(new Token(Type.REL_OP, "="));
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
						tokens.add(new Token(Type.STRING, currentToken));
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
				} else {
					if(!hasdecimal){
						prior += nextByte;
					} else{
						latter += nextByte;
					}
				}
				currentToken += nextByte;
				number = true;
			} else if ((Character.isLetterOrDigit(nextByte) || (nextByte == '.' && !hasdecimal)) && !number) // covering both labels and values in the same block since
															// values only come after "default" anyway
			{
				if (!label && !Character.isLetter(nextByte)) {
					throw new Exception("Labels must start with a letter!"); // realistically the digit branch should
																			 // stop this from ever being accessible
				}
				if (nextByte == '.') {
					hasdecimal = true;
				} else {
					if(!hasdecimal){
						prior += nextByte;
					} else{
						latter += nextByte;
					}
				}
				currentToken += nextByte;
				label = true; // we need to block the other paths to know when to flush.
			} else { // flush the label or number token string
				currentToken = currentToken.toLowerCase();
				if (currentToken.equals("")) {
					throw new Exception("Invalid Token!");
				}
				if (number && !hasdecimal) {
					tokens.add(new Token(Type.INTEGER, currentToken));
				}
				if (number && hasdecimal) {
					tokens.add(new Token(Type.DOUBLE, currentToken, prior, latter));
				}
				if (label && hasdecimal){
					tokens.add(new Token(Type.IDDOUBLE, currentToken, prior, latter));
				}
				if (label && !hasdecimal){
					switch (currentToken.toLowerCase()){
						case "integer":
							tokens.add(new Token(Type.INTDEF, currentToken));
							break;
						case "double":
							tokens.add(new Token(Type.DOUBLEDEF, currentToken));
							break;
						case "boolean":
							tokens.add(new Token(Type.BOOLDEF, currentToken));
							break;
						case "null":
							tokens.add(new Token(Type.NULL, currentToken));
							break;
						case "char":
						case "varchar":
							stmti++;
							if(ddlStatement.charAt(stmti) != '('){
								throw new Exception("( expected after char or varchar");
							}
							String size = "";
							stmti++;
							while(ddlStatement.charAt(stmti) != ')' && stmti < ddlStatement.length()){
								char next = ddlStatement.charAt(stmti);
								if(!Character.isDigit(next)){
									throw new Exception("Size expected in (var)char definition!");
								}
								size += next;
								stmti++;
							}
							if(size.equals("")){
								throw new Exception("Size expected in (var)char definition!");
							}
							if(currentToken.toLowerCase().equals("char")){
								tokens.add(new Token(Type.CHARDEF, currentToken, Integer.parseInt(size)));
							}
							if (currentToken.toLowerCase().equals("varchar")){
								tokens.add(new Token(Type.VARCHARDEF, currentToken, Integer.parseInt(size)));
							}
						default:
							tokens.add(new Token(Type.IDKEY, currentToken));
					}
				}
				currentToken = "";
				prior = "";
				latter = "";
				number = false;
				label = false;
				hasdecimal = false;
				stmti--;
			}
		}
		return tokens;
	}
}