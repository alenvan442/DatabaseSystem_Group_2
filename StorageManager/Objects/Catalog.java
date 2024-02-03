package StorageManager.Objects;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import StorageManager.TableSchema;

public class Catalog implements Serializable, CatalogInterface{
    private static Catalog catalog;
    private Dictionary<Integer, TableSchema> schemas;
    private String dbLocation;
    private String catalogLocation;
    private int pageSize;
    private int bufferSize;


    private Catalog(String catalogLocation, String dbLocation, int pageSize, int bufferSize) {
        this.catalogLocation = catalogLocation;
        this.dbLocation = dbLocation;
        this.pageSize = pageSize != -1 ? pageSize : loadCatalog();
    }

    public static void createCatalog(String dbLocation, String catalogLocation, int pageSize, int bufferSize) {
        catalog = new Catalog(catalogLocation, dbLocation, pageSize, bufferSize);
    }

    public static void alterCatalog(){
        //curently assumes the operation will only be done a





    }

    public static void dropCatalog(){}


    public static Catalog getCatalog() {
        return catalog;
    }

    public void saveCatalog() {

    }

    public void addTableSchema(TableSchema schema) {

    }

    public void removeTableSchema(int tableNumber){

        schemas.remove(tableNumber);
    }

    public void alterTableSchema(int tableNumber) throws Exception {
        TableSchema table = schemas.get(tableNumber);

        // TODO: Figure out how we will determine what is being altered.
        // may need more parameters to determine, what the AttrName and attrType
        String testValue= "";
        String testAName="";
        String testAType="";
        String testDefVal = null;
        boolean has = false;
        List<Attribute> attrList = table.getAttributes();
        if(testValue.equals("drop")){
            //List<Attribute> attrList = table.getAttributes();
            attrList.remove(attrList.indexOf(testAName));
            //table.setAttributes(attrList);

        } else if (testValue.equals("add")) {
            for(int i=0; i<attrList.size(); i++){
                Attribute currentAttr = attrList.get(i);
                if(currentAttr.getAttributeName().equals(testAName)){
                    has = true;
                }
            }
            if(!has) {
                attrList.add(new Attribute(testAName, testAType, false, false, false));
            }
            //table.setAttributes(attrList);
        }else{
            throw new Exception("Invalid Command");
        }
        table.setAttributes(attrList);


    }

    private int loadCatalog() {
        return 0;
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
