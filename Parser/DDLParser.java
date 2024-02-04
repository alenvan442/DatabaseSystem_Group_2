package Parser;
import java.util.Scanner;


public class DDLParser {

	

    public static void parseCreateTable(String ddlStatement) 
	{	private String tableName;
		ArrayList<Attribute> attributes = new ArrayList<Attribute>;
		String tableName;
		Arraylist<String> tokens = Tokenize(ddlStatement);
		if(!tokens[0].toLower.equals("create") || !tokens[1].toLower.equals("table")
		{
			throw new Exception("this should be a create table statement???");
		}
		if(StringUtils.isAlphanumeric && tokens[2].charAt(0).isLetter(){
			tableName = tokens[2].toLower()
		} else 
		{
			throw new Exception("Table names must be alphanumeric!"); //can't use a double!
		}
    }

    public static void parseDropTable(String ddlStatement)
	{
		Arraylist<String> tokens = Tokenize(ddlStatement);

    }

    public static void parseAlterTable(String ddlStatement) 
	{
		Arraylist<String> tokens = Tokenize(ddlStatement);

    }
	
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
				if(Character.equals.(".")
				{
					hasdecimal = true;
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
				throw new Exception("invalid token, please check your command")
			}
        }            
    }
}
