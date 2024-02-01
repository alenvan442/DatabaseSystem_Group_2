package Parser;
import java.util.Scanner;


public class DDLParser {


    public static void parseCreateTable(String ddlStatement) {
		
    }

    public static void parseDropTable(String ddlStatement) {

    }

    public static void parseAlterTable(String ddlStatement) {

    }
	
	private Arraylist<String> Tokenize(String ddlStatement){
		Scanner scanner = new scanner(ddlStatement);
		Arraylist<String> tokens;
		String currentToken = ""
		char nextByte (Char)scanner.nextByte();
		bool sentinal = false
		While(scanner.hasNext){
			if(nextByte = '(' && sentinal){
				tokens.add("(")
				nextByte = scanner.nextByte();
			}
			else if(nextByte = ')' && sentinal){
				tokens.add(")")
				nextByte = scanner.nextByte();
			}
			else if(nextByte = ';' && sentinal){
				tokens.add(";")
				nextByte = scanner.nextByte();
			}
			else if(nextByte = ',' && sentinal){
				tokens.add(",")
				nextByte = scanner.nextByte();
			}
			else if(Character.isLetterOrDigit(nextByte)){
				CurrentToken += nextByte;
				sentinal = true;
			} else{
				tokens.add(currentToken);
				currentToken = "";
				sentinal = false;
				nextByte = scanner.nextByte();
			}
		}
			
		
	}
}
