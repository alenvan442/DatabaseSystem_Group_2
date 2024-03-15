package Parser;

import StorageManager.Objects.MessagePrinter;
import StorageManager.Objects.Record;
import StorageManager.Objects.MessagePrinter.MessageType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import Parser.WhereTreeNodes.WhereTree;

public class DMLParser extends ParserCommon {

    public static Map<String, List<Record>> parseInsert(ArrayList<Token> tokens) throws Exception {
        // Format: insert into <name> values <tuples>;
        // Catalog catalog = Catalog.getCatalog();
        String tableName;
        tokens.remove(0);
        tokens.remove(0);

        // check if first one is the keyword
        if (tokens.get(0).getType() != Type.IDKEY) {
            MessagePrinter.printMessage(MessageType.ERROR, "Invalid table name");
        }

        tableName = tokens.remove(0).getVal().toLowerCase();

        if (tokens.get(0).getType() != Type.IDKEY && !tokens.get(0).getVal().equalsIgnoreCase("values")) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected 'values' keyword");
        }

        tokens.remove(0);

        if (tokens.get(0).getType() != Type.L_PAREN) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected '('");
        }

        tokens.remove(0); // remove opening bracket

        List<Record> records = new ArrayList<>();
        while (true) {
            Record record = new Record(new ArrayList<>());
            while (!tokens.isEmpty() && tokens.get(0).getType() == Type.R_PAREN) {

                // is integer
                if (tokens.get(0).getType() == Type.INTEGER) {
                    record.getValues().add(tokens.get(0).getVal());
                    tokens.remove(0);
                    continue;
                }

                // is double
                if (tokens.get(0).getType() == Type.DOUBLE) {
                    record.getValues().add(tokens.get(0).getVal());
                    tokens.remove(0);
                    continue;
                }

                // is boolean
                if (tokens.get(0).getType() == Type.BOOLEAN) {
                    record.getValues().add(tokens.get(0).getVal());
                    tokens.remove(0);
                    continue;
                }

                // is string literal
                if (tokens.get(0).getType() == Type.STRING) {
                    record.getValues().add(tokens.get(0).getVal());
                    tokens.remove(0);
                    continue;
                }

                if (tokens.get(0).getType() == Type.NULL) {
                    record.getValues().add(null);
                    tokens.remove(0);
                    continue;
                }

                MessagePrinter.printMessage(MessageType.ERROR, tokens.get(0) + " is an invalid input");
            }

            if (tokens.isEmpty() || tokens.get(0).getType() != Type.R_PAREN) {
                // closing bracket was missing
                MessagePrinter.printMessage(MessageType.ERROR, "Expected ')'");
            }

            tokens.remove(0); // remove closing bracket
            records.add(record);

            if (tokens.get(0).getType() == Type.COMMA) {
                tokens.remove(0);
                if (tokens.get(0).getType() != Type.L_PAREN) {
                    MessagePrinter.printMessage(MessageType.ERROR, "Expected '(' after ','");
                }
                tokens.remove(0);
            } else if (tokens.get(0).getType() == Type.SEMICOLON) {
                break;
            } else {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected a ';'");
            }
        }
        Map<String, List<Record>> map = new HashMap<>();
        map.put(tableName, records);
        return map;
    }

    public static String parseSelect(ArrayList<String> tokens) throws Exception {
        // Format: select * from <name>;
        // only deal with all attributes
        String tableName = "";

        if (tokens.size() != 5 || !tokens.get(0).equalsIgnoreCase("select")
                || !tokens.get(1).equalsIgnoreCase("*")
                || !tokens.get(2).equals("from")
                || tokens.get(3).isEmpty()
                || !tokens.get(4).equals(";")) {
            MessagePrinter.printMessage(MessageType.ERROR, "incorrect select format");
        } else {
            // if the command is correct, return table name
            if (Character.isLetter(tokens.get(3).charAt(0))
                    && keywordCheck(tokens.get(3))) {
                tableName = tokens.get(3).toLowerCase();
            }
        }
        return tableName;
    }

    public static ArrayList<String> parseFrom(ArrayList<String> tokens) throws Exception {
        // from tableName, tableName, ......
        ArrayList<String> tableName = new ArrayList<>();
        String nameRegex = "^[a-zA-Z][a-zA-Z0-9]*$";

        if (tokens.size() <= 1) {
            MessagePrinter.printMessage(MessageType.ERROR, "incorrect format");
        }

        if (!tokens.get(0).equalsIgnoreCase("from")) {
            MessagePrinter.printMessage(MessageType.ERROR, "first should be from");
        } else {
            // table name(s) in a string
            for (int i = 1; i < tokens.size() - 1; i++) {
                if (tokens.get(i).contains(nameRegex)) {
                    tableName.add(tokens.get(i));
                }
            }
        }
        return tableName;
    }

    public static WhereTree parseWhere(ArrayList<Token> tokens) throws Exception {
        Queue<Token> outputPostfix = new LinkedList<>();
        Stack<Token> operatorStack = new Stack<>();
        tokens.remove(0); // Remove "where" token

        while (true) {
            // Expect attribute name
            if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.IDDOUBLE) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected an attribute name");
            }
            outputPostfix.add(tokens.remove(0));

            // Expect relational operation
            if (tokens.isEmpty() || tokens.get(0).getType() != Type.REL_OP) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected a relational operation");
            }
            Token currOperator = tokens.remove(0);
            while (!operatorStack.isEmpty() && getPrecedent(operatorStack.peek()) >= getPrecedent(currOperator)) {
                outputPostfix.add(operatorStack.pop());
            }
            operatorStack.push(currOperator);

            // Expect attribute name or constant value
            if (tokens.isEmpty() || !isAttributeOrConstant(tokens.get(0))) {
                MessagePrinter.printMessage(MessageType.ERROR, "Attribute name or a constant value expected");
            }
            outputPostfix.add(tokens.remove(0));

            // Expect 'and', 'or', 'orderby', or ';'
            if (tokens.isEmpty() || !isLogicalOperator(tokens.get(0)) || tokens.get(0).getType() != Type.SEMICOLON
                    || !tokens.get(0).getVal().equals("orderby")) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected either 'and', 'or', 'orderby', or ';'");
            }

            if (isLogicalOperator(tokens.get(0))) {
                currOperator = tokens.remove(0);
                while (!operatorStack.isEmpty() && getPrecedent(operatorStack.peek()) >= getPrecedent(currOperator)) {
                    outputPostfix.add(operatorStack.pop());
                }
                operatorStack.push(currOperator);
            } else {
                while (!operatorStack.isEmpty()) {
                    outputPostfix.add(operatorStack.pop());
                }
                break;
            }
        }

        WhereTreeBuilder whereTreeBuilder = new WhereTreeBuilder(outputPostfix);
        return whereTreeBuilder.buildWhereTree();
    }

    public static void parseDisplaySchema(ArrayList<String> tokens) throws Exception {
        // Command: display schema
        // It will show: database location, page size, buffer, size, and table schema
        if (!tokens.get(0).equalsIgnoreCase("display")
                && !tokens.get(1).equalsIgnoreCase("schema")
                && !tokens.get(2).equals(";")) {
            MessagePrinter.printMessage(MessageType.ERROR, "");
        }
    }

    public static String parseDisplayInfo(ArrayList<String> tokens) throws Exception {
        // Command: display info <name>
        // It will show: table name, table schema, # of pages, # of records
        String tableName;

        if (tokens.get(0).equalsIgnoreCase("display")
                && tokens.get(1).equalsIgnoreCase("info")
                && tokens.get(3).equals(";")
                && tokens.size() == 4) {
            tableName = tokens.get(2).toLowerCase();
            return tableName;
        } else {
            MessagePrinter.printMessage(MessageType.ERROR, "incorrect display info format");
            return null;
        }
    }

    public static void parseDelete(String dmlStatement) {
        // delete from foo;
        // delete from foo where bar = 10;
        // TODO
    }

    public static void parseUpdate(String dmlStatement) {
        // delete / insert record
        // TODO
    }

    public static int getPrecedent(Token operator) {
        if (operator.getType() == Type.REL_OP) {
            return 0;
        } else if (operator.getVal().equals("and")) {
            return 1;
        } else {
            return 2;
        }
    }

    private static boolean isAttributeOrConstant(Token token) {
        Type type = token.getType();
        return type == Type.IDKEY || type == Type.IDDOUBLE || type == Type.INTDEF || type == Type.DOUBLEDEF ||
                type == Type.BOOLDEF || type == Type.VARCHARDEF || type == Type.CHARDEF || type == Type.STRING ||
                type == Type.DOUBLE || type == Type.INTEGER || type == Type.BOOLEAN || type == Type.IDDOUBLE;
    }

    private static boolean isLogicalOperator(Token token) {
        String value = token.getVal();
        return value.equals("and") || value.equals("or");
    }
}
