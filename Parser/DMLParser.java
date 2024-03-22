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

    public static Insert parseInsert(ArrayList<Token> tokens) throws Exception {
        String tableName;
        tokens.remove(0);
        tokens.remove(0);

        if (tokens.get(0).getType() != Type.IDKEY) {
            MessagePrinter.printMessage(MessageType.ERROR, "Invalid table name");
        }
        tableName = tokens.remove(0).getVal();

        if (!tokens.get(0).getVal().equalsIgnoreCase("values")) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected 'values' keyword");
        }
        tokens.remove(0);

        List<Record> records = new ArrayList<>();
        records.add(parseRecord(tokens));

        while (tokens.get(0).getType() != Type.SEMICOLON) {
            if (tokens.get(0).getType() != Type.COMMA) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected a ','");
            }

            tokens.remove(0);
            records.add(parseRecord(tokens));
        }

        tokens.remove(0); // remove semicolon
        Map<String, List<Record>> map = new HashMap<>();
        map.put(tableName, records);

        return new Insert(tableName, records);
    }

    public static Record parseRecord(ArrayList<Token> tokens) throws Exception {
        if (tokens.get(0).getType() != Type.L_PAREN) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected '('");
        }
        tokens.remove(0); // remove opening bracket

        Record record = new Record(new ArrayList<>());
        while (tokens.get(0).getType() != Type.R_PAREN) {
            switch (tokens.get(0).getType()) {
                case INTEGER:
                    record.getValues().add(Integer.parseInt(tokens.remove(0).getVal()));
                    break;
                case DOUBLE:
                    record.getValues().add(Double.parseDouble(tokens.remove(0).getVal()));
                    break;
                case BOOLEAN:
                    record.getValues().add(Boolean.parseBoolean(tokens.remove(0).getVal()));
                    break;
                case STRING:
                    record.getValues().add(tokens.remove(0).getVal());
                    break;
                case NULL:
                    record.getValues().add(null);
                    break;
                default:
                    MessagePrinter.printMessage(MessageType.ERROR, tokens.get(0) + " is an invalid input");
            }
        }
        tokens.remove(0); // remove closing bracket
        return record;
    }

    public static Select parseSelect(ArrayList<Token> tokens) throws Exception {
        // Format: select * from <name>;
        List<String> attributeNames = new ArrayList<>();
        tokens.remove(0); // remove select token

        if (tokens.get(0).getType() != Type.ASTERISK) {

            if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.QUALIFIER
                    && tokens.get(0).getType() != Type.CONSTRAINT && tokens.get(0).getType() != Type.DATATYPE) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected attribute name");
            }

            attributeNames.add(tokens.remove(0).getVal());

            while (!tokens.get(0).getVal().equalsIgnoreCase("from")) {

                if (tokens.get(0).getType() != Type.COMMA) {
                    MessagePrinter.printMessage(MessageType.ERROR, "Expected ',' after attribute name");
                }

                tokens.remove(0);

                if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.QUALIFIER
                        && tokens.get(0).getType() != Type.CONSTRAINT && tokens.get(0).getType() != Type.DATATYPE) {
                    MessagePrinter.printMessage(MessageType.ERROR, "Expected attribute name");
                }

                attributeNames.add(tokens.remove(0).getVal());

            }
        } else {
            tokens.remove(0); // remove asterisk
        }

        List<String> tableNames = parseFrom(tokens);
        WhereTree whereTree = null;
        String ordeByAttribute = null;
        if (tokens.get(0).getVal().equalsIgnoreCase("where")) {
            whereTree = parseWhere(tokens);
        }

        if (tokens.get(0).getVal().equalsIgnoreCase("orderby")) {
            ordeByAttribute = parseOrderBy(tokens);
        }

        if (tokens.get(0).getType() != Type.SEMICOLON) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected ';'");
        }
        tokens.remove(0); // remove semicolon

        return new Select(attributeNames, tableNames, whereTree, ordeByAttribute);
    }

    public static ArrayList<String> parseFrom(ArrayList<Token> tokens) throws Exception {
        // from tableName, tableName, ......
        tokens.remove(0); // remove from keyword
        ArrayList<String> tableNames = new ArrayList<>();

        if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.DATATYPE
                && tokens.get(0).getType() != Type.CONSTRAINT) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected table name");
        }

        tableNames.add(tokens.remove(0).getVal());

        while (tokens.get(0).getType() != Type.SEMICOLON && !tokens.get(0).getVal().equalsIgnoreCase("where")) {
            if (tokens.get(0).getType() != Type.COMMA) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected a ','");
            }

            tokens.remove(0);

            if (tokens.get(0).getType() != Type.IDKEY) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected table name");
            }

            tableNames.add(tokens.remove(0).getVal());

        }

        return tableNames;
    }

    public static WhereTree parseWhere(ArrayList<Token> tokens) throws Exception {
        Queue<Token> outputPostfix = new LinkedList<>();
        Stack<Token> operatorStack = new Stack<>();
        tokens.remove(0); // Remove "where" token

        // Expect attribute name
        if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.QUALIFIER
                && tokens.get(0).getType() != Type.CONSTRAINT && tokens.get(0).getType() != Type.DATATYPE) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected attribute name");
        }

        outputPostfix.add(tokens.remove(0));

        // Expect relational operation
        if (tokens.get(0).getType() != Type.REL_OP) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected a relational operation");
        }
        Token currOperator = tokens.remove(0);
        while (!operatorStack.isEmpty() && getPrecedent(operatorStack.peek()) >= getPrecedent(currOperator)) {
            outputPostfix.add(operatorStack.pop());
        }
        operatorStack.push(currOperator);

        // Expect attribute name or constant value
        if (!isAttributeOrConstant(tokens.get(0))) {
            MessagePrinter.printMessage(MessageType.ERROR, "Attribute name or a constant value expected");
        }
        outputPostfix.add(tokens.remove(0));

        while (tokens.get(0).getType() != Type.SEMICOLON && !tokens.get(0).getVal().equalsIgnoreCase("orderby")) {

            if (!isLogicalOperator(tokens.get(0))) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected either 'and' or 'or'");
            }

            // Expect attribute name
            if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.QUALIFIER
                    && tokens.get(0).getType() != Type.CONSTRAINT && tokens.get(0).getType() != Type.DATATYPE) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected attribute name");
            }

            outputPostfix.add(tokens.remove(0));

            // Expect relational operation
            if (tokens.get(0).getType() != Type.REL_OP) {
                MessagePrinter.printMessage(MessageType.ERROR, "Expected a relational operation");
            }
            currOperator = tokens.remove(0);
            while (!operatorStack.isEmpty() && getPrecedent(operatorStack.peek()) >= getPrecedent(currOperator)) {
                outputPostfix.add(operatorStack.pop());
            }
            operatorStack.push(currOperator);

            // Expect attribute name or constant value
            if (!isAttributeOrConstant(tokens.get(0))) {
                MessagePrinter.printMessage(MessageType.ERROR, "Attribute name or a constant value expected");
            }
            outputPostfix.add(tokens.remove(0));
        }

        while (!operatorStack.isEmpty()) {
            outputPostfix.add(operatorStack.pop());
        }

        WhereTreeBuilder whereTreeBuilder = new WhereTreeBuilder(outputPostfix);
        return whereTreeBuilder.buildWhereTree();
    }

    public static String parseOrderBy(ArrayList<Token> tokens) throws Exception {
        String orderByAttribute = "";
        tokens.remove(0); // remove orderby token

        if (tokens.get(0).getType() != Type.IDKEY && tokens.get(0).getType() != Type.QUALIFIER
                && tokens.get(0).getType() != Type.CONSTRAINT && tokens.get(0).getType() != Type.DATATYPE) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected attribute name");
        }

        orderByAttribute = tokens.remove(0).getVal();

        return orderByAttribute;
    }

    public static void parseDisplaySchema(ArrayList<Token> tokens) throws Exception {
        // Command: display schema
        tokens.remove(0); // remove display token
        tokens.remove(0); // remove schema token

        if (tokens.get(0).getType() != Type.SEMICOLON) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected ';'");
        }
        tokens.remove(0); // remove semicolon
    }

    public static String parseDisplayInfo(ArrayList<Token> tokens) throws Exception {
        // Command: display info <name>
        // It will show: table name, table schema, # of pages, # of records
        String tableName;

        tokens.remove(0); // remove display token
        tokens.remove(0); // remove info token

        if (tokens.get(0).getType() != Type.IDKEY) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected table name");
        }

        tableName = tokens.remove(0).getVal();

        if (tokens.get(0).getType() != Type.SEMICOLON) {
            MessagePrinter.printMessage(MessageType.ERROR, "Expected ';'");
        }
        tokens.remove(0); // remove semicolon

        return tableName;
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
        } else if (operator.getVal().equalsIgnoreCase("and")) {
            return 1;
        } else {
            return 2;
        }
    }

    private static boolean isAttributeOrConstant(Token token) {
        Type type = token.getType();
        return type == Type.IDKEY || type == Type.QUALIFIER || type == Type.DATATYPE || type == Type.CONSTRAINT ||
                type == Type.STRING || type == Type.DOUBLE || type == Type.INTEGER || type == Type.BOOLEAN;
    }

    private static boolean isLogicalOperator(Token token) {
        String value = token.getVal();
        return value.equalsIgnoreCase("and") || value.equalsIgnoreCase("or");
    }
}
