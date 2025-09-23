package com.sschoi.notifyninja.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sschoi.notifyninja.model.AppModel;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "notify_ninja.db";
    private static final int DB_VER = 3;

    private static final String TBL = "apps";
    private static final String COL_PKG = "package_name";
    private static final String COL_NAME = "app_name";
    private static final String COL_PHONE = "phone";
    private static final String COL_SENDER_NAME = "sender_name";
    private static final String COL_SENDER_NUMBER = "sender_number";

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 기존 앱 등록 테이블
		db.execSQL("CREATE TABLE " + TBL + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +   // 고유 id
                COL_PKG + " TEXT NOT NULL," +
                COL_NAME + " TEXT," +
                COL_PHONE + " TEXT," +
                COL_SENDER_NAME + " TEXT," +
                COL_SENDER_NUMBER + " TEXT," +
                "UNIQUE(" + COL_PKG + ", " + COL_PHONE + ") ON CONFLICT IGNORE" +
                ")");		
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // v1 -> v2 마이그레이션
        if (oldV == 1 && newV >= 2) {
            db.execSQL("ALTER TABLE " + TBL + " RENAME TO " + TBL + "_backup");
            onCreate(db);
            db.execSQL("INSERT OR IGNORE INTO " + TBL + " (" +
                    COL_PKG + ", " + COL_NAME + ", " + COL_PHONE + ", " +
                    COL_SENDER_NAME + ", " + COL_SENDER_NUMBER +
                    ") SELECT " + COL_PKG + ", " + COL_NAME + ", " + COL_PHONE + ", NULL, NULL FROM " + TBL + "_backup");
            db.execSQL("DROP TABLE " + TBL + "_backup");
            oldV = 2;
        }

        // v2 -> v3: 보낸사람 이름/번호 필터 컬럼 추가
        if (oldV == 2 && newV >= 3) {
            db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN " + COL_SENDER_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TBL + " ADD COLUMN " + COL_SENDER_NUMBER + " TEXT");
            oldV = 3;
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

    public boolean insertApp(String pkg, String name, String phone, String senderName, String senderNumber) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_PKG, pkg);
        v.put(COL_NAME, name);
        v.put(COL_PHONE, phone);
        v.put(COL_SENDER_NAME, senderName);
        v.put(COL_SENDER_NUMBER, senderNumber);
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
        Cursor c = db.query(TBL, new String[]{COL_PKG, COL_NAME, COL_PHONE, COL_SENDER_NAME, COL_SENDER_NUMBER},
                null, null, null, null, COL_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                list.add(new AppModel(
                        c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)
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
                new String[]{COL_PKG, COL_NAME, COL_PHONE, COL_SENDER_NAME, COL_SENDER_NUMBER},
                COL_PKG + "=?",
                new String[]{pkg},
                null, null, null);

        if (c.moveToFirst()) {
            do {
                list.add(new AppModel(
                        c.getString(0),  // package_name
                        c.getString(1),  // app_name
                        c.getString(2),  // phone
                        c.getString(3),  // sender_name
                        c.getString(4)   // sender_number
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }
}
