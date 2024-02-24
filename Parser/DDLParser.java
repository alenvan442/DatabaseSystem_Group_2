package Parser;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.MessagePrinter.MessageType;
import StorageManager.TableSchema;


import java.util.ArrayList;
import java.util.HashMap;

public class DDLParser extends ParserCommon {

	public static TableSchema parseCreateTable(ArrayList<String> tokens) throws Exception {
		ArrayList<AttributeSchema> attributes = new ArrayList<AttributeSchema>();
		String tableName = "";
		if (!tokens.get(0).toLowerCase().equals("create") || !tokens.get(1).toLowerCase().equals("table")) {
			MessagePrinter.printMessage(MessageType.ERROR, "this should be a create table statement???");
		}
		if (Character.isLetter(tokens.get(2).charAt(0)) && keywordCheck(tokens.get(2))) // verifying first is a letter ensures this is a legal label via the
											// tokenizer's constraints
		{
			tableName = tokens.get(2).toLowerCase();
		} else {
			MessagePrinter.printMessage(MessageType.ERROR, "Table names must be alphanumeric and begin with a letter!"); // can't use a double!
		}
		if (!tokens.get(3).equals("(")) {
			MessagePrinter.printMessage(MessageType.ERROR, "Open parenthesis expected in create table stmt!");
		}
		int i = 4;
		while (!tokens.get(i).equals(")") && i < tokens.size()) // I doubt anyone will write a statement 99999 tokens long, this is
														// just a loop failsafe for if there is no closing ")"
		{
			String attributeName = "";
			String dataType = "";
			boolean notNull = false;
			boolean primaryKey = false;
			boolean unique = false;
			if (Character.isLetter(tokens.get(i).charAt(0))) // verifying first is a letter ensures this is a legal label via the
												// tokenizer's constraints
			{
				attributeName = tokens.get(i).toLowerCase();
				i++;
			} else {
				MessagePrinter.printMessage(MessageType.ERROR, "Attribute names must be alphanumeric, begin with a letter, and cannot be a keyword!"); // can't use a
																										// double!
			}
			String type = tokens.get(i).toLowerCase();
			if (!(type.equals("integer") || type.equals("double") || type.equals("boolean") || type.equals("char")
					|| type.equals("varchar"))) {
				MessagePrinter.printMessage(MessageType.ERROR, String.format("Invalid data type \"%s\"", type));
			}
			if (type.equals("char") || type.equals("varchar")) {
				dataType += type;
				i++;
				if (!tokens.get(i).equals("(")) {
					MessagePrinter.printMessage(MessageType.ERROR, "Open parenthesis expected for char or varchar type");
				}
				dataType += "(";
				i++;
				String[] size = tokens.get(i).split("[.]"); // we need to ensure this is an integer not decimal
				if (size.length != 1) {
					MessagePrinter.printMessage(MessageType.ERROR, "char or varchar size must be integer");
				}
				dataType += tokens.get(i);
				i++;
				if (!tokens.get(i).equals(")")) {
					MessagePrinter.printMessage(MessageType.ERROR, "Close parenthesis expected for char or varchar type");
				}
				dataType += ")";
			} else {
				dataType = type;
			}
			i++;
			// from here we can have up to 3 constraints
			int constraints = 0;
			boolean comma = false;
			boolean end = false;
			while (!comma && constraints < 4) {
				String constraint = tokens.get(i).toLowerCase();
				if (constraint.equals(",")) {
					comma = true;
				} else if (constraint.equals(")")) {
					end = true;
					break;
				} else if (constraint.equals("notnull") && !notNull) {
					notNull = true;
				} else if (constraint.equals("primarykey") && !primaryKey) {
					primaryKey = true;
					unique = true;
					notNull = true;
				} else if (constraint.equals("unique") && !unique) {
					unique = true;
				} else if (constraint.equals(";")) {
					throw new Exception("Missing ending bracket.");
				} else {
					MessagePrinter.printMessage(MessageType.ERROR, "Unrecognized constraint, valid constraints are notnull, primarykey, and unique");
				}
				i++;
			}
			if (!comma && !end) {
				MessagePrinter.printMessage(MessageType.ERROR,"Attributes must be comma-seperated");
			}
			attributes.add(new AttributeSchema(attributeName, dataType, notNull, primaryKey, unique));
		}
		if (!tokens.get(i).equals(")")) {
			MessagePrinter.printMessage(MessageType.ERROR, "Attribute list must be closed with \")\"!"); // just a few escape characters
		}
		i++;
		if (!tokens.get(i).equals(";")) {
			MessagePrinter.printMessage(MessageType.ERROR, "Commands must end with a \";\"!");
		}
		TableSchema schema = new TableSchema(tableName);
		schema.setAttributes(attributes);
		return schema;
	}

	public static String parseDropTable(ArrayList<String> tokens) throws Exception {
		if (!tokens.get(0).toLowerCase().equals("drop") || !tokens.get(1).toLowerCase().equals("table")) {
			MessagePrinter.printMessage(MessageType.ERROR, "this should be a drop table statement???");
		}
		if (!Character.isLetter(tokens.get(2).charAt(0)) || !keywordCheck(tokens.get(2))) 	// verifying first is a letter ensures this is a legal label via the
			// tokenizer's constraints
		{
			MessagePrinter.printMessage(MessageType.ERROR, "Table names must be alphanumeric,begin with a letter, and cannot be a keyword!");
		}
		return tokens.get(2).toLowerCase();
	}

	/**
	 *
	 * @param tokens the alter statement passed in
	 * @returns altervals, a hashmap of strings with the following values:
	 * tableName: Name of the table to be altered
	 * adddrop: "add" | "drop" defining the function to complete
	 * attriname: Name of the attribute to be altered
	 * type: type of the new attribute, defaults will be checked parser-side
	 * deflt: default value to change all existing rows in the table to for the new attri.
	 * will be passed as a string regardless of type.
	 * @throws Exception
	 */
	public static HashMap<String, String> parseAlterTable(ArrayList<String> tokens) throws Exception {
		HashMap<String, String> altervals = new HashMap<>();
		String tableName = "";
		String adddrop = "";
		String attriname = "";
		String type = "null";
		String deflt = "";
		String isDeflt = "false";
		String charType = "";
		if (!tokens.get(0).toLowerCase().equals("alter") || !tokens.get(1).toLowerCase().equals("table")) {
			MessagePrinter.printMessage(MessageType.ERROR, "this should be an alter table statement???");
		}
		tableName = tokens.get(2).toLowerCase();
		if(!(Character.isLetter(tableName.charAt(0)) && keywordCheck(tableName))){// verifying first is a letter ensures this is a legal label via the
			MessagePrinter.printMessage(MessageType.ERROR,"Table names must be alphanumeric, begin with a letter, and not a keyword!"); // can't use a double!
		}
		adddrop = tokens.get(3).toLowerCase();
		if(!(adddrop.equals("add") || adddrop.equals("drop"))) {
			MessagePrinter.printMessage(MessageType.ERROR, "Field 4 must be \"add\" or \"drop\"");
		}
		attriname = tokens.get(4).toLowerCase();
		if(!(Character.isLetter(attriname.charAt(0)) && keywordCheck(attriname))){// verifying first is a letter ensures this is a legal label via the
			MessagePrinter.printMessage(MessageType.ERROR, "Attribute names must be alphanumeric, begin with a letter, and not a keyword!"); // can't use a double!
		}
		if(adddrop.equals("add")) {
			type = tokens.get(5).toLowerCase();
			charType = type;
			if (!(type.equals("integer") || type.equals("double") || type.equals("boolean") || type.equals("char")
					|| type.equals("varchar"))) {
				MessagePrinter.printMessage(MessageType.ERROR, "Attribute types must be one of the following: integer, double, boolean, char, varchar");
			}
			if (type.equals("char") || type.equals("varchar")) {
				if (!tokens.get(6).equals("(")) {
					MessagePrinter.printMessage(MessageType.ERROR, "Open parenthesis expected for char or varchar type");
				}
				charType += "(";
				String[] Size = tokens.get(7).split("[.]"); // we need to ensure this is an integer not decimal
				if (!(Size.length == 1)) {
					MessagePrinter.printMessage(MessageType.ERROR, "char or varchar size must be integer");
				}
				charType += tokens.get(7);

				if (!tokens.get(8).equals(")")) {
					MessagePrinter.printMessage(MessageType.ERROR, "Close parenthesis expected for char or varchar type");
				}
				charType += ")";
			}
			if ((tokens.size() >=9 && !type.equals("char") && !type.equals("varchar")) || (type.equals("char") || type.equals("varchar")) && tokens.size() >= 12) { // checking for a default value, hellish boolean
				deflt = tokens.get(tokens.size() - 2).toLowerCase();
				isDeflt = "true";
				String[] Size = tokens.get(7).split("[.]");
				int charsize;
				if (!deflt.equals("null")) {
					switch (type) {
						case "integer": // we need to ensure this is an integer not decimal
							if (!(Size.length == 1) || !Character.isDigit(deflt.charAt(0))) {
								MessagePrinter.printMessage(MessageType.ERROR, "default is not an integer!");
							}
							break;
						case "double": // we need to ensure this is a decimal not an integer
							if (!(Size.length == 2) || !(Character.isDigit(deflt.charAt(0))) || deflt.charAt(0) == '.') {
								MessagePrinter.printMessage(MessageType.ERROR, "default is not an integer!");
							}
							break;
						case "boolean":
							if (!(deflt.equals("true") || deflt.equals("false"))) {
								MessagePrinter.printMessage(MessageType.ERROR, "default is not a boolean!");
							}
							break;
						case "char":
							deflt = deflt.substring(1, deflt.length() - 1);//removing quotes, if they aren't quotes then size constraint will (probably) trip
							charsize = Integer.parseInt(tokens.get(7)); //what token "8" is depends on the case but I'm just setting up vars for both here
							if (charsize != deflt.length()) {
								MessagePrinter.printMessage(MessageType.ERROR, "Char default must match specified size!");
							}
							type = charType;
							break;
						case "varchar":
							deflt = deflt.substring(1, deflt.length() - 1);//removing quotes, if they aren't quotes then size constraint will (probably) trip
							charsize = Integer.parseInt(tokens.get(7)); //what token "8" is depends on the case but I'm just setting up vars for both here
							if (deflt.length() > charsize) {
								MessagePrinter.printMessage(MessageType.ERROR, "Varchar default must be less than or equal to the specified size!");
							}
							type = charType;
							break;
						default:
							MessagePrinter.printMessage(MessageType.ERROR, "Type not recognized!"); //unreachable, hopefully
					}
				}
			}
		}
		altervals.put("tableName", tableName);
		altervals.put("adddrop", adddrop);
		altervals.put("attriname", attriname);
		altervals.put("type", type);
		altervals.put("deflt", deflt);
		altervals.put("isDeflt", isDeflt);
		return altervals;
	}
}
