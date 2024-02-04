package Parser;
import java.util.Scanner;


public static class DDLParser extends Parser{

    public static void parseCreateTable(String ddlStatement) 
	{	
		private String tableName;
		ArrayList<Attribute> attributes = new ArrayList<Attribute>;
		String tableName;
		Arraylist<String> tokens = Tokenize(ddlStatement);
		if(!tokens[0].toLower.equals("create") || !tokens[1].toLower.equals("table")
		{
			throw new Exception("this should be a create table statement???");
		}
		if(tokens[2].charAt(0).isLetter()) //verifying first is a letter ensures this is a legal label via the tokenizer's constraints
		{
			tableName = tokens[2].toLower();
		} else 
		{
			throw new Exception("Table names must be alphanumeric and begin with a letter!"); //can't use a double!
		}
		if(!tokens[3].equals("(")){
			throw new Exception("Open parenthesis expected in create table stmt!");
		}
		int i = 4;
		while(!tokens[i].equals(")") && i < 99999) //I doubt anyone will write a statement 99999 tokens long, this is just a loop failsafe for if there is no closing ")"
		{ 
			String attributeName = "";
			String dataType = "";
			boolean notNull = false;
			boolean primaryKey = false;
			boolean unique = false;
			if(tokens[i].charAt(0).isLetter()) //verifying first is a letter ensures this is a legal label via the tokenizer's constraints
			{
				attributeName = tokens[2].toLower();
				i++;
			} else 
			{
				throw new Exception("Attribute names must be alphanumeric and begin with a letter!"); //can't use a double!
			}
			String type = tokens[i].toLower();
			if(!(type.equals("integer") || type.equals("double") || type.equals("boolean") || type.equals("char") || type.equals("varchar"))
			{
				throw new Exception("Attribute types must be one of the following: integer, double, boolean, char, varchar");
			}
			if(type.equals("char") || type.equals("varchar")
			{
				dataType += type;
				i++;
				if(!tokens[i].equals("(")
				{
					throw new Exception("Open parenthesis expected for char or varchar type");
				}
				dataType += "(";
				i++
				String[] Size = tokens[i].split(.); //we need to ensure this is an integer not decimal
				if(!Size.length == 1)
				{
					throw new Exception("char or varchar size must be integer");
				}
				dataType += tokens[i];
				i++;
				if(!tokens[i].equals(")")
				{
					throw new Exception("Close parenthesis expected for char or varchar type");
				}
				dataType += ")";
			} else {
				dataType = type;
			}
			i++;
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
	
}
