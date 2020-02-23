# Android-Database


Introduction
====
This is an Android Database library for easy using database.

How to use it
====
You can check the detailed sample code in test app. 
Next is some quick guide.

1. Create you own table and table item class.(Ref TestTable.java and TestEntity.java file)
-------
```
@Table("test_t")
public class TestEntity extends BaseEntity {

    @Column("f_int")
    private Integer intValue;

    @Column("f_long")
    private Long longValue;

    @Column("f_bool")
    private Boolean boolValue;

    @Column("f_float")
    private Float floatValue;

    @Column("f_double")
    private Double doubleValue;

    @Column("f_string")
    private String stringValue;

    @Column("f_blob")
    private byte[] blobValue;
}
```
```
public class TestTable extends BaseTable<TestEntity> {
    public TestTable(Database database) {
        super(database);
    }
}
```
2. Create database and table
-------
```
  Database database = Database.create(getApplicationContext(), Database.SDCARD_FOLDER,"easyadr/db/testdb.db", 1);
  //if need handle update, add next code
  database.setListener(new Database.onUpgradeListener() {
            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

            }
        });
  //handle update end
  TestTable table = new TestTable(database);
```  
3. Operate table using simple API
-------
a. Add records
```
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
```

b. Modify records -- set id to the record's id to be modified
```
for(int i=0;i<50;i++) {
	TestEntity editEntiry = new TestEntity();
	editEntiry.setId(i + 1l);
	editEntiry.setIntValue(i + 3);
	editEntiry.setStringValue("this is a test " + (i+2));
	table.save(editEntiry);
}
```

c. Delete record by id
```
table.delete(8);
```

d. Delete record by field, such as stringValue
```
TestEntity filter = new TestEntity();
filter.setStringValue("this is a test 2");
table.delete(filter);
```

e. Simple find all
```
List<TestEntity> entityList = table.findAll();
```

f. find one by id
```
TestEntity findEntity = table.findById(8);
```

g. find records with columns
```
List<TestEntity> entityList = table.findWithColumns(new String[]{"id", "f_long"}, "f_long=?1",new String[]{String.valueOf(20l)});
```

h. find records have same field values with item
```
TestEntity filterEntity = new TestEntity();
filterEntity.setStringValue("this is a test 21");
filterEntity.setBoolValue(true);
List<TestEntity> entityList = table.findSameWith(filterEntity);
```

i. find first record
```
TestEntity findEntity = table.findFirstOneWithColumns(new String[]{"id", "f_long"}, "f_long=?1",new String[]{String.valueOf(20l)});

TestEntity filterEntity = new TestEntity();
filterEntity.setStringValue("this is a test 21");
filterEntity.setBoolValue(true);
TestEntity findEntity = table.findFirstOneSameWith(filterEntity);
```

j. use "like" rather than "equal" for string field
```
TestEntity filterEntity = new TestEntity();
filterEntity.setStringValue("this is a test%");
List<TestEntity> entityList = table.findLikeWith(filterEntity);
if(entityList == null || entityList.size() != 98){
	throw new RuntimeException("find same with error");
}
```

k. rawQuery API as rawQuery of SQLiteDatabase, for complex situation
```
List<TestEntity> entityList = table.rawQuery(
	"select id, f_int, f_long, f_string, f_blob from {table} where id>? and id<? and f_string like ?",
	new String[]{"0", "200", "this is a test%"});
```

More detailed info can be get from the code and test code.

4. Attention
-------
Using {table} in sql string which is presenting table name.