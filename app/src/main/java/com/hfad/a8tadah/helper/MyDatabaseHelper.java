package com.hfad.a8tadah.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    public static final String DB_NAME = "chat";

    public MyDatabaseHelper(Context context){
        super(context, DB_NAME,null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        onUpgrade(db, VERSION, VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion == 1){
            db.execSQL("CREATE TABLE CHATS (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " USER_ID TEXT, CHAT_ID TEXT)");

            db.execSQL("CREATE TABLE CHAT_MESSAGES(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "SENDER_ID TEXT, MESSAGE_TEXT TEXT, MESSAGE_ID TEXT NOT NULL, " +
                    "MESSAGE_TIME NUMERIC, MESSAGE_SENT NUMERIC, MESSAGE_OPENED NUMERIC, CHAT_ID TEXT)");

            //

            db.execSQL("CREATE TABLE USER(_id INTEGER PRIMARY KEY AUTOINCREMENT,CONTACT TEXT NOT NULL UNIQUE, USER_ID TEXT NOT NULL, " +
                    "USERNAME TEXT, STATUS TEXT, IMG TEXT)");
        } else {
            db.execSQL("ALTER TABLE CHAT_MESSAGES ADD COLUMN  MESSAGE_OPENED NUMERIC;");
        }
    }

}
