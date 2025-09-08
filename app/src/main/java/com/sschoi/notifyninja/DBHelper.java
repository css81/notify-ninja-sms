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
    private static final int DB_VER = 2;

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
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +   // 고유 id
                COL_PKG + " TEXT NOT NULL," +
                COL_NAME + " TEXT," +
                COL_PHONE + " TEXT," +
                "UNIQUE(" + COL_PKG + ", " + COL_PHONE + ") ON CONFLICT IGNORE" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        //db.execSQL("DROP TABLE IF EXISTS " + TBL);
        //onCreate(db);
        if (oldV == 1 && newV == 2) { // 앱+다중연락처 개선 기존디비 유지
            // 1. 기존 테이블 백업
            db.execSQL("ALTER TABLE " + TBL + " RENAME TO " + TBL + "_backup");

            // 2. 새로운 구조로 테이블 생성
            onCreate(db);

            // 3. 데이터 복원
            db.execSQL("INSERT OR IGNORE INTO " + TBL + " (" +
                    COL_PKG + ", " + COL_NAME + ", " + COL_PHONE +
                    ") SELECT " + COL_PKG + ", " + COL_NAME + ", " + COL_PHONE +
                    " FROM " + TBL + "_backup");

            // 4. 임시 테이블 삭제
            db.execSQL("DROP TABLE " + TBL + "_backup");
        }
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

//    public AppModel getByPackage(String pkg) {
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor c = db.query(TBL, new String[]{COL_PKG, COL_NAME, COL_PHONE},
//                COL_PKG + "=?", new String[]{pkg}, null, null, null);
//        AppModel m = null;
//        if (c.moveToFirst()) {
//            m = new AppModel(c.getString(0), c.getString(1), c.getString(2));
//        }
//        c.close();
//        return m;
//    }

    public List<AppModel> getAllByPackage(String pkg) {
        List<AppModel> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TBL,
                new String[]{COL_PKG, COL_NAME, COL_PHONE},
                COL_PKG + "=?",
                new String[]{pkg},
                null, null, null);

        if (c.moveToFirst()) {
            do {
                list.add(new AppModel(
                        c.getString(0),  // package_name
                        c.getString(1),  // app_name
                        c.getString(2)   // phone
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

}
