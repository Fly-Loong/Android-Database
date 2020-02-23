package cn.com.easyadr.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.com.easyadr.database.annotation.Column;
import cn.com.easyadr.database.annotation.Table;
import static cn.com.easyadr.database.ClassName.*;

public abstract class BaseTable<T extends BaseEntity>{

    protected Database database;
    protected String tableName;

    private FieldInfo[] fields;
    private Class<T> entityClass;

    public BaseTable(Database database){
        this.database = database;
        try {
            Type pt = getClass().getGenericSuperclass();
            if(!(pt instanceof ParameterizedType)){
                throw new RuntimeException(getClass().getName() + "must extends BaseTable<XXXXEntity>");
            }
            entityClass = (Class<T>)((ParameterizedType)pt).getActualTypeArguments()[0];
            Table table = entityClass.getAnnotation(Table.class);
            if(table == null) {
                throw new RuntimeException("not set table name");
            }
            tableName = table.value();
            String createTableSql = "create table if not exists " + tableName + " (id integer primary key autoincrement,";
            boolean hasField = false;
            List<FieldInfo> fieldList = new ArrayList<>();
            for (Field field : entityClass.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if(column != null){
                    String columnName = column.value();
                    String className = field.getType().getName();
                    checkFieldType(className, field.getName());
                    createTableSql += columnName;
                    if(className.equals(STRING)){
                        createTableSql += " TEXT,";
                    }else if(isInteger(className)){
                        createTableSql += " INTEGER,";
                    } else if(isReal(className)){
                        createTableSql += " REAL,";
                    } else if(className.equals(BYTE_ARRAY)){
                        createTableSql += " BLOB,";
                    } else {
                        throw new RuntimeException(className + "is not handled by BaseTable");
                    }
                    fieldList.add(new FieldInfo(columnName, field));
                    field.setAccessible(true);
                    hasField = true;
                }
            }
            if(!hasField){
                throw new RuntimeException("no field in table " + tableName);
            }
            fields = fieldList.toArray(new FieldInfo[0]);
            createTableSql = createTableSql.substring(0, createTableSql.length() - 1);
            createTableSql +=")";
            database.getWritableDatabase().execSQL(createTableSql);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /*
     *save item into db
     * if id is null, create, and set id value back into item
     * else update the no-null value in item
     */
    public void save(T item){
        ContentValues contentValues = toContentValue(item);
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        if(item.getId() == null) {
            Long ret = sqLiteDatabase.insert(tableName, null, contentValues);
            item.setId(ret);
        }else{
            sqLiteDatabase.update(tableName,contentValues, "id=?", new String[]{String.valueOf(item.getId())});
        }
    }

    /*
     * delete by id
     */
    public void delete(long id) {
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        sqLiteDatabase.delete(tableName, "id=?", new String[]{String.valueOf(id)});
    }

    /*
     * delete the item in db which has same value with filter item
     */
    public void delete(T filterItem) {
        WhereClause whereClause = new WhereClause(filterItem, fields);
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        sqLiteDatabase.delete(tableName, whereClause.whereClause, whereClause.whereArgs);
    }

    /*
     * find item by id
     */
    public T findById(long id) {
        List<T> ret = rawQuery("select * from {table} where id=? limit 1", new String[]{String.valueOf(id)});
        return ret.size() == 0 ? null: ret.get(0);
    }

    /*
     * simplest find all items
     */
    public List<T> findAll(){
        Cursor cursor = database.getReadableDatabase().rawQuery("select * from " + tableName, null);
        return curorToItems(cursor);
    }

    /*
     * find items using sqLiteDatabase.rawQuery params
     * columns: columns in rawQuery
     * params: 1. selection in rawQuery
     *         2. selectionArgs in rawQuery
     *         3. groupBy in rawQuery
     *         4. having in rawQuery
     *         5. orderBy in rawQuery
     *         6. limit in rawQuery
     */
    public List<T> findWithColumns(String[] columns_, Object...args){
        SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();

        String selection = args.length > 0 ? (String)args[0] : null;
        String[] selectionArgs = args.length > 1 ? (String[])args[1] : null;
        String groupBy = args.length > 2 ? (String)args[2] : null;
        String having = args.length > 3 ? (String)args[3] : null;
        String orderBy = args.length > 4 ? (String)args[4] : null;
        String limit = args.length > 5 ? (String)args[5] : null;

        Cursor cursor = sqLiteDatabase.query(tableName, null, selection, selectionArgs, groupBy, having, orderBy, limit);
        return curorToItems(cursor);
    }

    /*
     * find items has same no-null value with filter
     * filter: selectionArgs are filter's no-null fields
     * params: 1. groupBy in rawQuery
     *         2. having in rawQuery
     *         3. orderBy in rawQuery
     *         4. limit in rawQuery
     */
    public List<T> findSameWith(T filter, Object...args){
        SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();

        WhereClause whereClause = new WhereClause(filter, fields);

        String groupBy = args.length > 0 ? (String)args[0] : null;
        String having = args.length > 1 ? (String)args[1] : null;
        String orderBy = args.length > 2 ? (String)args[2] : null;
        String limit = args.length > 3 ? (String)args[3] : null;

        Cursor cursor = sqLiteDatabase.query(tableName, null, whereClause.whereClause, whereClause.whereArgs, groupBy, having, orderBy, limit);
        return curorToItems(cursor);
    }

    /*
     * same as SQLiteDatabase.rawQuery
     */
    public List<T> rawQuery(String sql, String[] selectionArgs){
        sql = sql.replace("{table}", tableName);
        return curorToItems(database.getReadableDatabase().rawQuery(sql, selectionArgs));
    }

    /*
     * find the first one
     */
    public T findFirstOneWithColumns(String[] columns, String selection, String[] selectionArgs){
        SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(tableName, columns, selection, selectionArgs, null, null, null, "1");
        List<T> ret = curorToItems(cursor);
        return ret.size() == 0 ? null: ret.get(0);
    }

    /*
     * find the first one
     */
    public T findFirstOneSameWith(T filter){
        SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();
        WhereClause whereClause = new WhereClause(filter, fields);

        Cursor cursor = sqLiteDatabase.query(tableName, null, whereClause.whereClause, whereClause.whereArgs, null, null, null, "1");
        List<T> ret = curorToItems(cursor);
        return ret.size() == 0 ? null: ret.get(0);
    }

    /*
     * find items has same no-null value with filter, but use like for string rather than equal
     * filter: selectionArgs are filter's no-null fields
     * params: 1. groupBy in rawQuery
     *         2. having in rawQuery
     *         3. orderBy in rawQuery
     *         4. limit in rawQuery
     */
    public List<T> findLikeWith(T filter, Object...args){
        SQLiteDatabase sqLiteDatabase = database.getReadableDatabase();

        WhereLikeClause whereClause = new WhereLikeClause(filter, fields);

        String groupBy = args.length > 0 ? (String)args[0] : null;
        String having = args.length > 1 ? (String)args[1] : null;
        String orderBy = args.length > 2 ? (String)args[2] : null;
        String limit = args.length > 3 ? (String)args[3] : null;

        Cursor cursor = sqLiteDatabase.query(tableName, null, whereClause.whereClause, whereClause.whereArgs, groupBy, having, orderBy, limit);
        return curorToItems(cursor);
    }

    protected ContentValues toContentValue(T item) {
        ContentValues contentValues = new ContentValues();
        try {
            if(item.getId() != null) {
                contentValues.put("id", item.getId());
            }
            for (FieldInfo field : fields) {
                Object value = field.classField.get(item);
                if(value != null) {
                    addValue(contentValues, field.tableFieldName, value);
                }
            }
        }catch(Exception e){}
        return contentValues;
    }

    protected List<T> curorToItems(Cursor cursor) {
        ArrayList<T> ret = new ArrayList<>();
        if(cursor != null){
            try {
                while (cursor.moveToNext()) {
                    if(cursor == null) {
                        break;
                    }
                    T item = cursorToItem(cursor);
                    ret.add(item);
                }
            }catch(Exception e){
                throw new RuntimeException(e);
            }finally {
                cursor.close();
            }
        }
        return ret;
    }

    protected T cursorToItem(Cursor cursor){
        T item = newEntity();
        int idIndex = cursor.getColumnIndex("id");
        if(idIndex >= 0){
            item.setId(cursor.getLong(cursor.getColumnIndex("id")));
        }
        for (FieldInfo field : fields) {
            try {
                int columnIndex = cursor.getColumnIndex(field.tableFieldName);
                if (columnIndex >= 0) {
                    String typeName = field.classField.getType().getName();
                    if (typeName.equals(INTEGER)) {
                        field.classField.set(item, cursor.getInt(columnIndex));
                    } else if (typeName.equals(LONG)) {
                        field.classField.set(item, cursor.getLong(columnIndex));
                    } else if (typeName.equals(BOOLEAN)) {
                        field.classField.set(item, cursor.getInt(columnIndex)==0?false:true);
                    } else if (typeName.equals(STRING)) {
                        field.classField.set(item, cursor.getString(columnIndex));
                    } else if (typeName.equals(BYTE_ARRAY)) {
                        field.classField.set(item, cursor.getBlob(columnIndex));
                    } else if (typeName.equals(FLOAT)) {
                        field.classField.set(item, cursor.getFloat(columnIndex));
                    } else if (typeName.equals(DOUBLE)) {
                        field.classField.set(item, cursor.getDouble(columnIndex));
                    }
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        return item;
    }
    private T newEntity() {
        try {
            return entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isInteger(String name) {
        return name.equals(INTEGER) || name.equals(LONG) || name.equals(BOOLEAN);
    }

    private boolean isReal(String name) {
        return name.equals(FLOAT) || name.equals(DOUBLE);
    }

    private void checkFieldType(String name, String valueName) {
        if(!name.equals(INTEGER)
                && !name.equals(LONG)
                && !name.equals(BOOLEAN)
                && !name.equals(FLOAT)
                && !name.equals(DOUBLE)
                && !name.equals(STRING)
                && !name.equals(BYTE_ARRAY)){
            String msgFmt = "[%s].[%s %s] not support. Only support Integer, Long, Boolean, Float, Double, String, byte[].";
            throw new RuntimeException(String.format(msgFmt, tableName, name, valueName));
        }
    }

    private void addValue(ContentValues values, String key, Object value) {
        if(value.getClass().getName().equals(INTEGER)){
            values.put(key, (Integer)value);
        } else if(value.getClass().getName().equals(LONG)){
            values.put(key, (Long) value);
        } else if(value.getClass().getName().equals(STRING)){
            values.put(key, (String) value);
        } else if(value.getClass().getName().equals(BYTE_ARRAY)){
            values.put(key, (byte[]) value);
        } else if(value.getClass().getName().equals(BOOLEAN)){
            values.put(key, (Boolean) value);
        } else if(value.getClass().getName().equals(FLOAT)){
            values.put(key, (Float) value);
        } else if(value.getClass().getName().equals(DOUBLE)){
            values.put(key, (Double) value);
        }
    }

    private static class FieldInfo {
        String tableFieldName;
        Field classField;

        FieldInfo(String name, Field field){
            tableFieldName = name;
            classField = field;
        }
    }

    private class WhereClause{
        String whereClause;
        String[] whereArgs;

        WhereClause(T filterItem, FieldInfo[] fieldInfos){
            StringBuilder stringBuilder = new StringBuilder();
            List<String> selectionArgList = new ArrayList<>();
            try {
                for (FieldInfo field : fields) {
                    Object value = field.classField.get(filterItem);
                    if (value != null) {
                        if(stringBuilder.length() > 0){
                            stringBuilder.append(" and ");
                        }
                        stringBuilder.append(field.tableFieldName);
                        stringBuilder.append("=?");
                        if(value instanceof Boolean) {
                            selectionArgList.add(((Boolean) value) ? "1" : "0");
                        }else {
                            selectionArgList.add(String.valueOf(value));
                        }
                    }
                }
                whereClause = stringBuilder.toString();
                whereArgs = selectionArgList.toArray(new String[0]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    private class WhereLikeClause{
        String whereClause;
        String[] whereArgs;

        WhereLikeClause(T filterItem, FieldInfo[] fieldInfos){
            StringBuilder stringBuilder = new StringBuilder();
            List<String> selectionArgList = new ArrayList<>();
            try {
                for (FieldInfo field : fields) {
                    Object value = field.classField.get(filterItem);
                    if (value != null) {
                        if(stringBuilder.length() > 0){
                            stringBuilder.append(" and ");
                        }
                        stringBuilder.append(field.tableFieldName);
                        if(value instanceof String) {
                            stringBuilder.append(" like ?");
                        }else {
                            stringBuilder.append("=?");
                        }
                        if(value instanceof Boolean) {
                            selectionArgList.add(((Boolean) value) ? "1" : "0");
                        }else {
                            selectionArgList.add(String.valueOf(value));
                        }
                    }
                }
                whereClause = stringBuilder.toString();
                whereArgs = selectionArgList.toArray(new String[0]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
