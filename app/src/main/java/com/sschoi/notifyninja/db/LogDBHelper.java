package com.sschoi.notifyninja.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sschoi.notifyninja.model.ForwardLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "forward_log.db";
    private static final int DB_VERSION = 1;
    public static final String TBL_LOG = "forward_log";

    public static final String COL_ID = "id";
    public static final String COL_TIME = "time";
    public static final String COL_TARGET = "target";
    public static final String COL_TITLE = "title";
    public static final String COL_STATUS = "status"; // SUCCESS / FAIL

    public LogDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL_LOG + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_TIME + " TEXT," +
                COL_TARGET + " TEXT," +
                COL_TITLE + " TEXT," +
                COL_STATUS + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_LOG);
        onCreate(db);
    }
	
	 // ========================
    // 로그 추가
    // ========================
    public void insertLog(String target, String title, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TIME, System.currentTimeMillis());
        values.put(COL_TARGET, target);
        values.put(COL_TITLE, title);
        values.put(COL_STATUS, status);
        db.insert(TBL_LOG, null, values);
    }

    // ========================
    // 모든 로그 조회
    // ========================
    public List<ForwardLog> getAllLogs() {
        List<ForwardLog> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TBL_LOG,
                new String[]{COL_ID, COL_TIME, COL_TARGET, COL_TITLE, COL_STATUS},
                null, null, null, null, COL_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ForwardLog log = new ForwardLog();
                log.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                log.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)));
                log.setTarget(cursor.getString(cursor.getColumnIndexOrThrow(COL_TARGET)));
                log.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
                log.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
                list.add(log);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
	
	public void insertLog(String pkg, String target, String title, String status) {
		SQLiteDatabase dbWritable = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
		cv.put("target", target);
		cv.put("title", title);
		cv.put("status", status);
		dbWritable.insert(TBL_LOG, null, cv);
	}
}
