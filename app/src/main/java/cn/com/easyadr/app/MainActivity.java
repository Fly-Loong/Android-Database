package cn.com.easyadr.app;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.com.easyadr.database.Database;
import cn.com.easyadr.app.Database.TestEntity;
import cn.com.easyadr.app.Database.TestTable;
import cn.com.easyadr.database.Cursor;

public class MainActivity extends AppCompatActivity {
    private Database database1;
    private Database database2;
    private TestTable table1;
    private TestTable table2;
    private ListView listView;
    private ListAdapter adapter;

    private Cursor<TestEntity> cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        adapter = new ListAdapter();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    public void handleDatabaseTest(View view) {
        recreateDB();
        testOnTable(table1);
        testOnTable(table2);
        if(cursor != null){
            cursor.close();
            cursor = null;
        }
        cursor = table1.findAllCursor();
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Test success", Toast.LENGTH_SHORT).show();
    }

    private void recreateDB() {
        Database.delete(getApplicationContext(), Database.SDCARD_FOLDER,"easyadr/db/testdb1.db");
        database1 = Database.create(getApplicationContext(), Database.SDCARD_FOLDER,"easyadr/db/testdb1.db", 1);
        table1 = new TestTable(database1);
        database1.setListener(new Database.onUpgradeListener() {
            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

            }
        });
        Database.delete(getApplicationContext(), Database.APP_FOLDER,"easyadr/db/testdb2.db");
        database2 = Database.create(getApplicationContext(), Database.APP_FOLDER,"easyadr/db/testdb2.db", 1);
        table2 = new TestTable(database2);
    }

    private void testOnTable(TestTable table){
        createTestEntities(table);
        findAll(table);
        editItems(table);
        deleteItem(table);
        findWithColumns(table);
        findSameWith(table);
        findLikeWith(table);
        rawQuery(table);
    }

    private void createTestEntities(TestTable table){
        for(int i=0;i<50;i++) {
            TestEntity entity = new TestEntity();
            entity.setIntValue(i + 2);
            entity.setLongValue(i + 2l);
            entity.setBoolValue(true);
            entity.setFloatValue(i + 18.678f);
            entity.setDoubleValue(i + 21.776d);
            entity.setStringValue("this is a test " + i);
            entity.setBlobValue("this is array".getBytes());
            table.save(entity);
        }
        List<TestEntity> entityList = table.findAll();
        if(entityList.size() != 50) {
            throw new RuntimeException("save 50 record into table error");
        }
    }

    private void findAll(TestTable table){
        List<TestEntity> entityList = table.findAll();
        for(int i=0;i<50;i++) {
            TestEntity entity = entityList.get(i);
            int testIndex = (int)entity.getId().longValue() - 1;
            if(entity.getIntValue() != testIndex+2
                    || entity.getLongValue() != testIndex+2
                    || !entity.getBoolValue()
                    || entity.getFloatValue() != testIndex+18.678f
                    || entity.getDoubleValue() != testIndex+21.776d
                    || !entity.getStringValue().equals("this is a test " + i)
                    || !new String(entity.getBlobValue()).equals("this is array")
            ){
                throw new RuntimeException("check table field error");
            }
        }
    }

    private void editItems(TestTable table) {
        for(int i=0;i<50;i++) {
            TestEntity editEntiry = new TestEntity();
            editEntiry.setId(i + 1l);
            editEntiry.setIntValue(i + 3);
            editEntiry.setStringValue("this is a test " + (i+2));
            table.save(editEntiry);
        }

        List<TestEntity> entityList = table.findAll();
        for(int i=0;i<50;i++) {
            TestEntity entity = entityList.get(i);
            int testIndex = (int)entity.getId().longValue() - 1;
            if(entity.getIntValue() != testIndex+3
                    || entity.getLongValue() != testIndex+2
                    || !entity.getBoolValue()
                    || entity.getFloatValue() != testIndex+18.678f
                    || entity.getDoubleValue() != testIndex+21.776d
                    || !entity.getStringValue().equals("this is a test " + (i+2))
                    || !new String(entity.getBlobValue()).equals("this is array")
            ){
                throw new RuntimeException("edit table field error");
            }
        }
    }

    private void deleteItem(TestTable table) {
        TestEntity findEntity = table.findById(8);
        if(findEntity == null) {
            throw new RuntimeException("delete error");
        }
        table.delete(8);
        TestEntity deletedEntity = table.findById(8);
        if(deletedEntity != null) {
            throw new RuntimeException("delete error");
        }

        TestEntity filter = new TestEntity();
        filter.setStringValue("this is a test 2");
        findEntity = table.findFirstOneSameWith(filter);
        if(findEntity == null) {
            throw new RuntimeException("delete error");
        }
        table.delete(filter);
        deletedEntity = table.findFirstOneSameWith(filter);
        if(deletedEntity != null) {
            throw new RuntimeException("delete error");
        }
    }

    private void findWithColumns(TestTable table) {
        for(int i=0;i<50;i++) {
            TestEntity entity = new TestEntity();
            entity.setIntValue(i + 2);
            entity.setLongValue(i + 2l);
            entity.setBoolValue(true);
            entity.setFloatValue(i + 18.678f);
            entity.setDoubleValue(i + 21.776d);
            entity.setStringValue("this is a test " + i);
            entity.setBlobValue("this is array".getBytes());
            table.save(entity);
        }
        List<TestEntity> entityList = table.findWithColumns(new String[]{"id", "f_long"}, "f_long=?1",new String[]{String.valueOf(20l)});
        if(entityList == null || entityList.size() != 2){
            throw new RuntimeException("find with columns error");
        }

        TestEntity findEntity = table.findFirstOneWithColumns(new String[]{"id", "f_long"}, "f_long=?1",new String[]{String.valueOf(20l)});
        if(findEntity == null){
            throw new RuntimeException("find first one with columns error");
        }
    }

    private void findSameWith(TestTable table) {
        TestEntity filterEntity = new TestEntity();
        filterEntity.setStringValue("this is a test 21");
        filterEntity.setBoolValue(true);
        List<TestEntity> entityList = table.findSameWith(filterEntity);
        if(entityList == null || entityList.size() != 2){
            throw new RuntimeException("find same with error");
        }

        TestEntity findEntity = table.findFirstOneSameWith(filterEntity);
        if(findEntity == null){
            throw new RuntimeException("find first one same with error");
        }
    }

    private void findLikeWith(TestTable table) {
        TestEntity filterEntity = new TestEntity();
        filterEntity.setStringValue("this is a test%");
        List<TestEntity> entityList = table.findLikeWith(filterEntity);
        if(entityList == null || entityList.size() != 98){
            throw new RuntimeException("find same with error");
        }
    }

    private void rawQuery(TestTable table) {
        List<TestEntity> entityList = table.rawQuery(
                "select id, f_int, f_long, f_string, f_blob from {table} where id>? and id<? and f_string like ?",
                new String[]{"0", "200", "this is a test%"});
        if(entityList == null || entityList.size() != 98){
            throw new RuntimeException("find same with error");
        }
        entityList = table.rawQuery(
                "select id, f_int, f_long, f_string, f_blob from {table} where id>? and id<? and f_string like ?",
                new String[]{"0", "200", "this is a testXX"});
        if(entityList.size() != 0){
            throw new RuntimeException("find same with error");
        }
;    }

    private class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return cursor == null ? 0: cursor.getCount();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TestEntity testEntity = cursor.moveToPosition(i);
            if(testEntity != null){
                View retView = LayoutInflater.from(MainActivity.this).inflate(R.layout.test_list_item, viewGroup, false);
                TextView str1TextView = retView.findViewById(R.id.str1);
                str1TextView.setText(testEntity.getId() + ":" + testEntity.getIntValue() + ":" + testEntity.getLongValue() + ":" + testEntity.getBoolValue());
                TextView str2TextView = retView.findViewById(R.id.str2);
                str2TextView.setText(testEntity.getDoubleValue() + ":" + testEntity.getFloatValue() + ":" + testEntity.getStringValue());
                return retView;
            }
            return null;
        }
    }
}
