package StorageManager.Objects;

import java.util.List;

public class Record implements java.io.Serializable {
    private List<Object> values;

    public Record(List<Object> values) {
        this.values = values;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

     /*
     * Compare a value of an attribute with another
     * Given: the dataType of other as well as the value at this.values.get(valueIndex)
     *        are the same
     *
     * ex)  The primaryKey of this record is at valueIndex = 2,
     *      then we call comparison(2, other, dataType) where:
     *          2:          the index where this record's primaryKey is
     *          other:      is what to compare this record's primaryKey with
     *          dataType:   the dataType of the primaryKey
     *
     * @param valueIndex    the index of the value in this record to compare
     * @param other         what to compare to
     * @param dataType      the dataType of the value to compare
     *
     * @return              1: this record is greater than other
     *                      0: this record is equal to other
     *                     -1: this record is less than other
     */
    public int comparison(int valueIndex, Object other, String dataType) {
        // TODO
        // cast value at valueIndex to the correct dataType, then comapre it to other
        // switch dataType to determine what to cast to
        return 0;
    }

    /*
     * Returns the size of this record in number of bytes
     *
     * @return  the number of bytes this record is
     */
    public int computeSize() {
        int size = 0;
        for (Object value: this.values) {
            if (value instanceof Integer) {
                size += Integer.BYTES;
            } else if (value instanceof String) {
                size += ((String) value).length();
            } else if (value instanceof Boolean) {
                size += 1;
            } else if (value instanceof Double) {
                size += Double.BYTES;
            }
        }
        return size;
    }
}
