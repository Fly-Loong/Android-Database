package cn.com.easyadr.app.Database;

import cn.com.easyadr.database.BaseTable;
import cn.com.easyadr.database.Database;

public class TestTable extends BaseTable<TestEntity> {
    public TestTable(Database database) {
        super(database);
    }
}
