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

        //기존의 테이블들을 일단 삭제하고 새로 만든다
        try {
            String DROP_SQL = "drop table if exists spot";
            db.execSQL(DROP_SQL);
            DROP_SQL = "drop table if exists spotInfo";
            db.execSQL(DROP_SQL);
        } catch(Exception ex) {
            Log.e("DatabaseHelper", "Exception in DROP_SQL", ex);
        }

        //"spot" 테이블 생성-----------------------------------------
        String CREATE_SQL = "CREATE TABLE spot("
                + " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + " name VARCHAR(30), "
                + " explanation VARCHAR(200), "
                + " latitude DOUBLE, "
                + " longitutde DOUBLE, "
                + " spotInfoID VARCHAR(255), "
                + " spotGroupID INTEGER, "
                + " productID INTEGER, "
                + " picName VARCHAR(20)); ";

        try {
            db.execSQL(CREATE_SQL);
        } catch(Exception ex) {
            Log.e("DatabaseHelper", "Exception in CREATE_SQL", ex);
        }

        try {
            db.execSQL( "insert into spot (name, explanation, latitude, longitutde, spotInfoID, spotGroupID, productID, picName) values ('Dormitory150', '여기는 Dormitory150입니다',40.418776, -86.925172,'1.2.3.4.5',0,0,'spot1');" );
            db.execSQL( "insert into spot (name, explanation, latitude, longitutde, spotInfoID, spotGroupID, productID, picName) values ('Burton Morgan','여기는 Burton Morgan입니다',40.423646, -86.922908,'6.7.8.9.10',0,0,'spot2');" );
            db.execSQL( "insert into spot (name, explanation, latitude, longitutde, spotInfoID, spotGroupID, productID, picName) values ('DLR', '여기는 DLR입니다',40.421226,-86.922258,'11.12.13.14.15',0,0,'spot3');" );
            db.execSQL( "insert into spot (name, explanation, latitude, longitutde, spotInfoID, spotGroupID, productID, picName) values ('PMU', '여기는 PMU입니다',40.425588,-86.91081,'16.17.18.19.20',0,0,'spot4');" );
            db.execSQL( "insert into spot (name, explanation, latitude, longitutde, spotInfoID, spotGroupID, productID, picName) values ('Knoy Hall', '여기는 Knoy Hall입니다',40.427661,-86.9111284,'21.22.23.24.25',0,0,'spot5');" );
            db.execSQL( "insert into spot (name, explanation, latitude, longitutde, spotInfoID, spotGroupID, productID, picName) values ('Research Park', '여기는 Research Park입니다',40.4614305,-86.9308978,'26.27.28.29.30',0,0,'spot6');" );

        } catch(Exception ex) {
            Log.e("DatabaseHelper", "Exception in insert SQL", ex);
        }

        //"spotInfo" 테이블 생성-----------------------------------------
        CREATE_SQL = "CREATE TABLE spotInfo("
                + " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + " infoTitle VARCHAR(40), "
                + " explanation VARCHAR(200), "
                + " picName VARCHAR(20)); ";

        try {
            db.execSQL(CREATE_SQL);
        } catch(Exception ex) {
            Log.e("DatabaseHelper", "Exception in CREATE_SQL", ex);
        }

        try {
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed1_1');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed1_2');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed1_3');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed1_4');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed1_5');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed2_1');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed2_2');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed2_3');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed2_4');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_5');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed3_1');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed3_2');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed3_3');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed3_4');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_5');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed4_1');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed4_2');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed4_3');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed4_4');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed4_5');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed5_1');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed5_2');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed5_3');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed5_4');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed5_5');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed5_1');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed5_2');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed5_3');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed5_4');" );
            db.execSQL( "insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed5_5');" );
        } catch(Exception ex) {
            Log.e("DatabaseHelper", "Exception in insert SQL", ex);
        }


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


