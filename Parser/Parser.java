//contains methods common to DDLParser and DMLParser
package Parser;

public static class Parser{ //extend me!
	
	//keywordCheck returns false if the passed label is a protected keyword, true otherwise
	private boolean keywordCheck(String label)
	{
		label = label.toLower()
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
				label.equals("select")
				label.equals("into") ||
				label.equals("values") ||
				label.equals("add") ||
				label.equals("default"));
	}
	
	//Tokenize Splits the ddlStatement into a list of strings, while checking some light semantics. There are a few types
	//"("
	//")"
	//","
	//";"
	//Labels, which are alphanumeric and start with a letter, ths includes keywords such as "Create"
	//Numbers, which can be integer or double with exactly 1 "." in any place, the char/varchar case must check for double seperately.
	//any tokens outside these options will throw an error.
	private Arraylist<String> Tokenize(String ddlStatement)
	{
        Scanner scanner = new scanner(ddlStatement);
        Arraylist<String> tokens;
        String currentToken = "";
        char nextByte (Char)scanner.nextByte();
        bool label = false;
		bool number = false;
		bool sentinal = (!label && ! number);
		bool hasdecimal = false;
        While(scanner.hasNext){
            if(nextByte == '(' && !sentinal)
			{
                tokens.add("(")
                nextByte = scanner.nextByte();
            }
            else if(nextByte == ')' && !sentinal)
			{
                tokens.add(")")
                nextByte = scanner.nextByte();
            }
            else if(nextByte == ';' && !sentinal)
			{
                tokens.add(";")
                nextByte = scanner.nextByte();
            }
            else if(nextByte == ',' && !sentinal)
			{
                tokens.add(",")
                nextByte = scanner.nextByte();
            }
			else if((Character.isDigit(nextByte) || (nextByte == "." && !hasdecimal)) && !label) //only ONE decimal point per double!
			{
				if(nextByte == ".")
				{
					hasdecimal = true;
				}
				currentToken += nextByte;
				number = true;
				nextByte = scanner.nextByte();
			}
            else if(Character.isLetterOrDigit(nextByte)) //covering both labels and values in the same block since values only come after "default" anyway
			{
				if(!label && !Character.isLetter(nextByte){
					throw new Exception("Labels must start with a letter!"); //realistically the digit branch should stop this from ever being accessible
				}
                currentToken += nextByte;
                label = true; //we need to block the other paths to know when to flush.
                nextByte = scanner.nextByte();
            } else
			{ //flush the label or number token string
                tokens.add(currentToken);
                currentToken = "";
                number = false;
				label = false;
				hasdecimal = false;
            } else 
			{
				throw new Exception("invalid token, please check your command");
			}
        }            
    }
}