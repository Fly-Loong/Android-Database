package cn.com.easyadr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Database extends SQLiteOpenHelper {
    public static final int SDCARD_FOLDER = 0;
    public static final int SDCARD_APP_FOLDER = 1;
    public static final int APP_FOLDER = 2;

    private onUpgradeListener listener;

    public static Database create(Context context, int type, String filename, int version) {
        File file = null;
        if(type == SDCARD_FOLDER) {
            String dbFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
            file = new File(dbFileName);
        } else if(type == SDCARD_APP_FOLDER) {
            String dbFileName = context.getExternalFilesDir(null).getAbsolutePath() + "/" + filename;
            file = new File(dbFileName);
        } else if(type == APP_FOLDER) {
            String dbFileName = context.getFilesDir().getAbsolutePath() + "/" + filename;
            file = new File(dbFileName);
        } else {
            throw new RuntimeException("Database init error, type should be SDCARD_FOLDER, SDCARD_APP_FOLDER or APP_FOLDER");
        }
        file.getParentFile().mkdirs();
        return new Database(context, file.getAbsolutePath(), null, version);
    }

    public static void delete(Context context, int type, String filename) {
        File file = null;
        if(type == SDCARD_FOLDER) {
            String dbFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
            file = new File(dbFileName);
        } else if(type == SDCARD_APP_FOLDER) {
            String dbFileName = context.getExternalFilesDir(null).getAbsolutePath() + "/" + filename;
            file = new File(dbFileName);
        } else if(type == APP_FOLDER) {
            String dbFileName = context.getFilesDir().getAbsolutePath() + "/" + filename;
            file = new File(dbFileName);
        } else {
            throw new RuntimeException("Database init error, type should be SDCARD_FOLDER, SDCARD_APP_FOLDER or APP_FOLDER");
        }
        file.delete();
    }

    @SuppressWarnings("unchecked")
    public <T> T getTable(final Class<T> table) {
        return (T) Proxy.newProxyInstance(table.getClassLoader(), new Class<?>[]{table}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return 5;
            }
        });
    }
    private Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void setListener(onUpgradeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(listener != null) {
            listener.onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        }
    }

    public interface onUpgradeListener {
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion);
    }
}
