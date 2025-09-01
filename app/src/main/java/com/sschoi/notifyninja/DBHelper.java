package com.sschoi.notifyninja;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "notify_ninja.db";
    private static final int DB_VER = 1;

    private static final String TBL = "apps";
    private static final String COL_PKG = "package_name";
    private static final String COL_NAME = "app_name";
    private static final String COL_PHONE = "phone";

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL + " (" +
                COL_PKG + " TEXT PRIMARY KEY," +
                COL_NAME + " TEXT," +
                COL_PHONE + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL);
        onCreate(db);
    }

    public boolean insertApp(String pkg, String name, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_PKG, pkg);
        v.put(COL_NAME, name);
        v.put(COL_PHONE, phone);
        long r = db.insertWithOnConflict(TBL, null, v, SQLiteDatabase.CONFLICT_IGNORE);
        return r != -1;
    }

    public void deleteApp(String pkg) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TBL, COL_PKG + "=?", new String[]{pkg});
    }

    public List<AppModel> getAllApps() {
        List<AppModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TBL, new String[]{COL_PKG, COL_NAME, COL_PHONE},
                null, null, null, null, COL_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                list.add(new AppModel(
                        c.getString(0), c.getString(1), c.getString(2)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public AppModel getByPackage(String pkg) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TBL, new String[]{COL_PKG, COL_NAME, COL_PHONE},
                COL_PKG + "=?", new String[]{pkg}, null, null, null);
        AppModel m = null;
        if (c.moveToFirst()) {
            m = new AppModel(c.getString(0), c.getString(1), c.getString(2));
        }
        c.close();
        return m;
    }
}
