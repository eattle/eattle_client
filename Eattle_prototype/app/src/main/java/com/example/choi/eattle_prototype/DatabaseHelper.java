package com.example.choi.eattle_prototype;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by choi on 2015-02-08.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        //디비 이름 : Eattle_Prototype
        super(context, CONSTANT.DATABASE_NAME , null, CONSTANT.DATABASE_VERSION);
    }

    //데이터베이스 파일이 처음 만들어질 때 호출되는 함수
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Database Helper onCreate 함수 호출");
    }

    //데이터베이스가 오픈될 때 호출되는 함수
    public void onOpen(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Database Helper onOpen 함수 호출");
    }

    //데이터베이스의 버전이 바뀌었을 때 호출되는 함수
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Database Helper onUpgrade 함수 호출");
    }
}


