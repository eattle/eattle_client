package com.example.choi.eattle_prototype;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by choi on 2015-02-08.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Eattle_Prototype";

    // Table Names
    private static final String TABLE_LOCATION = "location";
    private static final String TABLE_PATH = "path";
    private static final String TABLE_SPOT = "spot";
    private static final String TABLE_SPOTINFO = "spotinfo";
    private static final String TABLE_PRODUCT = "product";
    private static final String TABLE_PICTURE = "picture";


    // Common column names
//    private static final String KEY_ID = "id";
//    private static final String KEY_CREATED_AT = "created_at";

    // LOCATION Table - column names
//    private static final String KEY_TIME = "time";
//    private static final String KEY_LATITUDE = "latitude";
//    private static final String KEY_LONGITUDE = "longitude";

    // PATH Table - column names
//    private static final String KEY_TIME = "time";
//    private static final String KEY_SPOTID = "spotID";

    // SPOT Table - column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPOTINFOID = "spotinfoid";
    private static final String KEY_SPOTGROUPID = "spotgroupid";
//    private static final String KEY_PRODUCTID = "productid";
//    private static final String KEY_PICNAME = "picname";

    // SPOTINFO Table - column names
    private static final String KEY_SPOTID = "id";
    private static final String KEY_SPOTINFO = "spotinfo";
    private static final String KEY_PICNAME = "picname";

    // PRODUCT Table - column names
    private static final String KEY_PRODUCTID = "productid";
    private static final String KEY_VERSION = "version";
    private static final String KEY_COMPLETE = "complete";

    // PICTURE Table - column names
    private static final String KEY_PICTURENAME = "picturename";
    private static final String KEY_PICPATH = "picpath";
    private static final String KEY_MEMO = "memo";
    private static final String KEY_TIME = "time";

    // Table Create Statements
    // location table create statement
    private static final String CREATE_TABLE_LOCATION =
            "CREATE TABLE " + TABLE_LOCATION
                    + "("
                    + KEY_TIME + " LONG PRIMARY KEY NOT NULL, "
                    + KEY_LATITUDE + " LONG NOT NULL, "
                    + KEY_LONGITUDE + " LONG NOT NULL"
                    + ");";

    // path table create statement
    private static final String CREATE_TABLE_PATH =
            "CREATE TABLE " + TABLE_PATH
                    + "("
                    + KEY_TIME + " LONG PRIMARY KEY NOT NULL, "
                    + "spotID" + " INTEGER NOT NULL"
                    + ");";

    // spot table create statement
    private static final String CREATE_TABLE_SPOT =
            "CREATE TABLE spot("
                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + " name VARCHAR(30), "
                    + " explanation VARCHAR(200), "
                    + " latitude DOUBLE, "
                    + " longitude DOUBLE, "
                    + " radius DOUBLE, "
                    + " spotInfoID VARCHAR(255), "
                    + " spotGroupID INTEGER, "
                    + " productID INTEGER, "
                    + " picName VARCHAR(20)); ";

    // spotinfo table create statement
    private static final String CREATE_TABLE_SPOTINFO =
            "CREATE TABLE spotInfo("
                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + " infoTitle VARCHAR(40), "
                    + " explanation VARCHAR(200), "
                    + " picName VARCHAR(20)); ";

    // product table create statement
    private static final String CREATE_TABLE_PRODUCT =
            "CREATE TABLE " + TABLE_PRODUCT
                    + "("
                    + KEY_PRODUCTID + " INTEGER PRIMARY KEY NOT NULL, "
                    + KEY_VERSION + " STRING, "
                    + KEY_COMPLETE + " INTEGER"
                    + ");";

    // picture table create statement
    private static final String CREATE_TABLE_PICTURE =
            "CREATE TABLE " + TABLE_PICTURE
                    + "("
                    + KEY_PICTURENAME + " INTEGER PRIMARY KEY NOT NULL, "
                    + KEY_PICPATH + " STRING, "
                    + KEY_MEMO + " STRING, "
                    + KEY_TIME + " LONG"
                    + ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG, "Database Helper onCreate 함수 호출");

        //기존의 테이블들을 일단 삭제하고 새로 만든다
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATH);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICTURE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTINFO);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        } catch (Exception ex) {
            Log.e(LOG, "Exception in DROP_SQL", ex);
        }

        // creating required tables
        try {
            db.execSQL(CREATE_TABLE_LOCATION);
            db.execSQL(CREATE_TABLE_PATH);
            db.execSQL(CREATE_TABLE_SPOT);
            db.execSQL(CREATE_TABLE_SPOTINFO);
            db.execSQL(CREATE_TABLE_PRODUCT);
            db.execSQL(CREATE_TABLE_PICTURE);
        } catch (Exception ex) {
            Log.e(LOG, "Exception in CREATE_SQL", ex);
        }

        try {
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Cary Franklin Levering Quadrangle','여기는 Cary Franklin Levering Quadrangle입니다', 37.5037165, 127.044845,1000,'1.2.3.4',0,0,'spot1');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Cordova recreational sports center','여기는 Cordova recreational sports center입니다',40.423646, -86.922908,1000,'5.6.7.8.9.10.11.12.13.14',0,0,'spot2');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Discovery Park','여기는 Discovery Park입니다',40.421226,-86.922258,1000,'15.16.17.18.19.20.21.22.23.24.25.26',0,0,'spot3');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Earhart','여기는 Earhart입니다',40.425588,-86.91081,1000,'27.28.29.30.31',0,0,'spot4');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Neil armstrong','여기는 Neil armstrong입니다',40.427661,-86.9111284,1000,'32.33.34.35.36',0,0,'spot5');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('PMU','여기는 PMU입니다',40.4614305,-86.9308978,1000,'37.38.39',0,0,'spot6');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Purdue Bell Tower','여기는 Purdue Bell Tower입니다',40.4614305,-86.9308978,1000,'40.41.42.43.44',0,0,'spot7');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Purdue Mall','여기는 Purdue Mall입니다',40.4614305,-86.9308978,1000,'45.46.47.48',0,0,'spot8');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Ross-Ade stadium','여기는 Ross-Ade stadium입니다',40.4614305,-86.9308978,1000,'49.50.51',0,0,'spot9');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Wiley Hall','여기는 Wiley Hall입니다',40.4614305,-86.9308978,1000,'52.53.54.55.56',0,0,'spot10');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, radius, spotInfoID, spotGroupID, productID, picName) values ('Winsor Hall','여기는 Winsor Hall입니다',40.4614305,-86.9308978,1000,'57.58',0,0,'spot11');");

            //Cary Franklin Levering Quadrangle
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed1_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed1_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed1_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed1_4');");

            //Cordova recreational sports center
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed2_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed2_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed2_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed2_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_6');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_7');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_8');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_9');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_10');");

            //Discovery Park
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed3_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed3_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed3_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed3_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_6');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_7');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_8');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_9');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_10');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_11');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_12');");

            //Earhart
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed4_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed4_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed4_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed4_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed4_5');");

            //Neil armstrong
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed5_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed5_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed5_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed5_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed5_5');");


            //PMU
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed6_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed6_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed6_3');");


            //Purdue Bell Tower
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed7_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed7_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed7_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed7_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed7_5');");

            //Purdue Mall
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed8_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed8_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed8_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed8_4');");

            //Ross-Ade stadium
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed9_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed9_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed9_3');");

            //Wiley Hall
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed10_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed10_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed10_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed10_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed10_5');");

            //Winsor Hall
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed11_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed11_2');");

        } catch (Exception ex) {
            Log.e(LOG, "Exception in insert SQL", ex);
        }


    }


    @Override
    //데이터베이스가 오픈될 때 호출되는 함수
    public void onOpen(SQLiteDatabase db) {
        Log.d(LOG, "Database Helper onOpen 함수 호출");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG, "Database Helper onUpgrade 함수 호출");
        /*
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATH);

        // create new tables
        onCreate(db);
        */
    }


}
