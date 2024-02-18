package Parser;

import StorageManager.Objects.AttributeSchema;
import StorageManager.Objects.Utility.Pair;
import StorageManager.TableSchema;

import java.util.ArrayList;
import java.util.HashMap;

public class DDLParser extends ParserCommon {

	public static TableSchema parseCreateTable(String ddlStatement) throws Exception {
		ArrayList<AttributeSchema> attributes = new ArrayList<AttributeSchema>();
		String tableName;
		ArrayList<String> tokens = Tokenize(ddlStatement);
		if (!tokens.get(0).toLowerCase().equals("create") || !tokens.get(1).toLowerCase().equals("table")) {
			throw new Exception("this should be a create table statement???");
		}
		if (Character.isLetter(tokens.get(2).charAt(0))) // verifying first is a letter ensures this is a legal label via the
											// tokenizer's constraints
		{
			tableName = tokens.get(2).toLowerCase();
		} else {
			throw new Exception("Table names must be alphanumeric and begin with a letter!"); // can't use a double!
		}
		if (!tokens.get(3).equals("(")) {
			throw new Exception("Open parenthesis expected in create table stmt!");
		}
		int i = 4;
		while (!tokens.get(i).equals(")") && i < 99999) // I doubt anyone will write a statement 99999 tokens long, this is
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
				throw new Exception("Attribute names must be alphanumeric and begin with a letter!"); // can't use a
																										// double!
			}
			String type = tokens.get(i).toLowerCase();
			if (!(type.equals("integer") || type.equals("double") || type.equals("boolean") || type.equals("char")
					|| type.equals("varchar"))) {
				throw new Exception(
						"Attribute types must be one of the following: integer, double, boolean, char, varchar");
			}
			if (type.equals("char") || type.equals("varchar")) {
				dataType += type;
				i++;
				if (!tokens.get(i).equals("(")) {
					throw new Exception("Open parenthesis expected for char or varchar type");
				}
				dataType += "(";
				i++;
				String[] Size = tokens.get(i).split("."); // we need to ensure this is an integer not decimal
				if (!(Size.length == 1)) {
					throw new Exception("char or varchar size must be integer");
				}
				dataType += tokens.get(i);
				i++;
				if (!tokens.get(i).equals(")")) {
					throw new Exception("Close parenthesis expected for char or varchar type");
				}
				dataType += ")";
			} else {
				dataType = type;
			}
			i++;
			// from here we can have up to 3 constraints
			int constraints = 0;
			boolean comma = false;
			while (!comma && constraints <= 4) {
				String constraint = tokens.get(i).toLowerCase();
				if (constraint.equals(",")) {
					comma = true;
				} else if (constraint.equals("notnull") && !notNull) {
					notNull = true;
				} else if (constraint.equals("primarykey") && !primaryKey) {
					primaryKey = true;
					unique = true;
					notNull = true;
				} else if (constraint.equals("unique") && !unique) {
					unique = true;
				} else {
					throw new Exception(
							"Unrecognized constraint, valid constraints are notnull, primarykey, and unique");
				}
			}
			if (!comma) {
				throw new Exception("Attributes must be comma-seperated");
			}
			attributes.add(new AttributeSchema(attributeName, dataType, notNull, primaryKey, unique));
		}
		if (!tokens.get(i).equals(")")) {
			throw new Exception("Attribute list must be closed with \")\"!"); // just a few escape characters
		}
		i++;
		if (!tokens.get(i).equals(";")) {
			throw new Exception("Commands must end with a \";\"!");
		}
		TableSchema schema = new TableSchema(tableName);
		schema.setAttributes(attributes);
		return schema;
	}

	public static String parseDropTable(String ddlStatement) throws Exception {
		ArrayList<String> tokens = Tokenize(ddlStatement);
		if (!tokens.get(0).toLowerCase().equals("drop") || !tokens.get(1).toLowerCase().equals("table")) {
			throw new Exception("this should be a drop table statement???");
		}
		if (Character.isLetter(tokens.get(2).charAt(0))) 	// verifying first is a letter ensures this is a legal label via the
															// tokenizer's constraints
		{
			return tokens.get(2).toLowerCase();
		} else {
			throw new Exception("Table names must be alphanumeric and begin with a letter!"); // can't use a double!
		}

	}

	/**
	 *
	 * @param ddlStatement the alter statement passed in
	 * @returns altervals, a hashmap of strings with the following values:
	 * tableName: Name of the table to be altered
	 * adddrop: "add" | "drop" defining the function to complete
	 * attriname: Name of the attribute to be altered
	 * type: type of the new attribute, defaults will be checked parser-side
	 * deflt: default value to change all existing rows in the table to for the new attri.
	 * will be passed as a string regardless of type.
	 * @throws Exception
	 */
	public static HashMap<String, String> parseAlterTable(String ddlStatement) throws Exception {
		ArrayList<String> tokens = Tokenize(ddlStatement);
		HashMap<String, String> altervals = new HashMap<>();
		String tableName = "";
		String adddrop = "";
		String attriname = "";
		String type = "";
		String deflt = "";
	}

}
