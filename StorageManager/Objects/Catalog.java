package StorageManager.Objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import StorageManager.TableSchema;
import StorageManager.Objects.MessagePrinter.MessageType;

public class Catalog implements java.io.Serializable, CatalogInterface{
    private static Catalog catalog;
    private Dictionary<Integer, TableSchema> schemas;
    private String dbLocation;
    private String catalogLocation;
    private int pageSize;
    private int bufferSize;


    private Catalog(String catalogLocation, String dbLocation, int pageSize, int bufferSize) {
        this.catalogLocation = catalogLocation;
        this.dbLocation = dbLocation;
        this.pageSize = pageSize != -1 ? pageSize : 0;
        loadCatalog();
    }

    public static void createCatalog(String dbLocation, String catalogLocation, int pageSize, int bufferSize) {
        catalog = new Catalog(catalogLocation, dbLocation, pageSize, bufferSize);
    }


    public static Catalog getCatalog() {
        return catalog;
    }

    public void saveCatalog() throws IOException {
        ByteArrayOutputStream catalogByteArray = new ByteArrayOutputStream();
        ByteBuffer integerBuffer = ByteBuffer.allocate(Integer.BYTES * 3);
        integerBuffer.putInt(pageSize);
        integerBuffer.putInt(bufferSize);
        integerBuffer.putInt(schemas.size());
        catalogByteArray.write(integerBuffer.array());
        while (schemas.keys().hasMoreElements()) {
            Integer tableNum = schemas.keys().nextElement();
            catalogByteArray.write(schemas.get(tableNum).convertToBytes());
        }
        File schema = new File(this.catalogLocation + "/schema");
        FileOutputStream fileOutputStream = new FileOutputStream(schema.getAbsolutePath(), false);
        catalogByteArray.writeTo(fileOutputStream);
    }

    public void addTableSchema(TableSchema schema) {
        schemas.put(schema.getTableNumber(), schema);
    }

    /**
     * Method that deletes the schema for a table that is being dropped.
     * @param tableNumber - The old table # for the table we are updating due to being dropped.
     */
    public int removeTableSchema(int tableNumber){
        schemas.remove(tableNumber);
        //call StorageManager
    }

    /**
     * Method that updates the Schema for a particular Table based on the result of an Alter command.
     * @param tableNumber - The # for the Table we are updating the schemea of.
     * @param op - the alter operation we performed on this table, ei: drop attr or add attr
     * @param attrName - Name of the Attr that is being altered in the table.
     * @param attrType - Type of the Attr that is being altered in the table.
     * @param notNull - The attribute cannot be null.
     * @param pKey - Whether or not the attribute is a primary key.
     * @param unique - Whether or not the attribute has to be unique.
     * @throws Exception - Should only be thrown if the method is called in an incorrect fashion.
     * @return - If drop - The previous index of the dropped item. If add - the index of the new attr.
     */
    public int alterTableSchema(int tableNumber,String op, String attrName, String attrType, boolean notNull,
                                boolean pKey, boolean unique) throws Exception {
        TableSchema table = schemas.get(tableNumber);
        int returnIndex = -1;

        boolean has = false;
        List<AttributeSchema> attrList = table.getAttributes();
        if(op.equals("drop")){
            for(int i=0; i<attrList.size(); i++) {
                if(attrList.get(i).getAttributeName().equals(attrName)){
                    attrList.remove(i);
                    returnIndex = i;
                }
            }
        } else if (op.equals("add")) {
            for(int i=0; i<attrList.size(); i++){
                AttributeSchema currentAttr = attrList.get(i);
                if(currentAttr.getAttributeName().equals(attrName)){
                    has = true;
                }
            }
            if(!has) {
                attrList.add(new AttributeSchema(attrName, attrType, notNull, pKey, unique));
                returnIndex = attrList.size()-1;
            }
        }else{
            throw new Exception("Invalid Command");
        }
        table.setAttributes(attrList);

        if(returnIndex== -1){
            throw new Exception("The schema alter add or drop was not accounted for correctly.");
        }
        //call new storage manager method.
        return returnIndex;
    }

    private MessageType loadCatalog() {
        return null;
    }

    public Dictionary<Integer, TableSchema> getSchemas() {
        return schemas;
    }

    public TableSchema getSchema(int tableNumber) {
        return schemas.get(tableNumber);
    }

    public void setSchemas(List<TableSchema> schemas) {
        Dictionary<Integer, TableSchema> _new = new Hashtable<Integer,TableSchema>();
        for (TableSchema tableSchema : schemas) {
            _new.put(tableSchema.getTableNumber(), tableSchema);
        }
        this.schemas = _new;
    }

    public void setSchemas(Dictionary<Integer, TableSchema> schemas) {
        this.schemas = schemas;
    }

    public String getDbLocation() {
        return dbLocation;
    }

    public String getCatalogLocation() {
        return catalogLocation;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

}
