package cn.com.easyadr.database;

public class Cursor <T extends BaseEntity> {
    private android.database.Cursor cursor;
    private BaseTable<T> table;

    Cursor(BaseTable<T> table, android.database.Cursor cursor) {
        this.table = table;
        this.cursor = cursor;
    }

    public void close(){
        cursor.close();
    }

    public T moveToNext(){
        if(!cursor.moveToNext()){
            return null;
        }
        return table.cursorToItem(cursor);
    }

    public T moveToPosition(int index){
        if(!cursor.moveToPosition(index)){
            return null;
        }
        return table.cursorToItem(cursor);
    }

    public int getCount(){
        return cursor.getCount();
    }

    public int getPosition(){
        return cursor.getPosition();
    }

    public T move(int index){
        if(!cursor.move(index)){
            return null;
        }
        return table.cursorToItem(cursor);
    }

    public T moveToFirst(){
        if(!cursor.moveToFirst()){
            return null;
        }
        return table.cursorToItem(cursor);
    }

    public T moveToLast(){
        if(!cursor.moveToLast()){
            return null;
        }
        return table.cursorToItem(cursor);
    }

    public T moveToPrevious(){
        if(!cursor.moveToPrevious()){
            return null;
        }
        return table.cursorToItem(cursor);
    }

    public boolean isFirst(){
        return cursor.isFirst();
    }

    public boolean isLast(){
        return cursor.isLast();
    }

    public boolean isBeforeFirst(){
        return cursor.isBeforeFirst();
    }

    public boolean isAfterLast(){
        return cursor.isAfterLast();
    }
}
