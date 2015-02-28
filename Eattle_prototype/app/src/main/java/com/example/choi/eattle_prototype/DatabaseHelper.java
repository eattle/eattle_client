package com.example.choi.eattle_prototype;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.choi.eattle_prototype.model1.Path;
import com.example.choi.eattle_prototype.model1.Spot;

import java.util.ArrayList;
import java.util.List;

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
    private static final String KEY_SPOTID = "spotid";

    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "name";
//    private static final String KEY_EXPLANATION = "explanation";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPOTINFOID = "spotInfoID";
    private static final String KEY_SPOTGROUPID = "spotGroupID";
//    private static final String KEY_PRODUCTID = "productID";
//    private static final String KEY_PICNAME = "picName";

    // SPOTINFO Table - column names
//    private static final String ID = "_id";
    private static final String KEY_INFOTITLE = "infoTitle";
    private static final String KEY_EXPLANATION = "explanation";
    private static final String KEY_PICNAME = "picName";


    // PRODUCT Table - column names
    private static final String KEY_PRODUCTID = "productID";
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
                    + KEY_SPOTID + " INTEGER NOT NULL"
                    + ");";

    // spot table create statement
    private static final String CREATE_TABLE_SPOT =
            "CREATE TABLE spot("
                    + " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + " name VARCHAR(30), "
                    + " explanation VARCHAR(200), "
                    + " latitude DOUBLE, "
                    + " longitude DOUBLE, "
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
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTINFO);
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
            db.execSQL("insert into spot (name, explanation, latitude, longitude, spotInfoID, spotGroupID, productID, picName) values ('Dormitory150','여기는 Dormitory150입니다', 40.418776, -86.925172,'1.2.3.4.5',0,0,'spot1');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, spotInfoID, spotGroupID, productID, picName) values ('Burton Morgan','여기는 Burton Morgan입니다',40.423646, -86.922908,'6.7.8.9.10',0,0,'spot2');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, spotInfoID, spotGroupID, productID, picName) values ('DLR','여기는 DLR입니다',40.421226,-86.922258,'11.12.13.14.15',0,0,'spot3');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, spotInfoID, spotGroupID, productID, picName) values ('PMU','여기는 PMU입니다',40.425588,-86.91081,'16.17.18.19.20',0,0,'spot4');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, spotInfoID, spotGroupID, productID, picName) values ('Knoy Hall','여기는 Knoy Hall입니다',40.427661,-86.9111284,'21.22.23.24.25',0,0,'spot5');");
            db.execSQL("insert into spot (name, explanation, latitude, longitude, spotInfoID, spotGroupID, productID, picName) values ('Research Park','여기는 Research Park입니다',40.4614305,-86.9308978,'26.27.28.29.30',0,0,'spot6');");

            db.execSQL("insert into path (time,spotid) values ( 1423319520, 1);");
            db.execSQL("insert into path (time,spotid) values ( 1423326060, 2);");
            db.execSQL("insert into path (time,spotid) values ( 1423342320, 3);");
            db.execSQL("insert into path (time,spotid) values ( 1423348260, 5);");
            db.execSQL("insert into path (time,spotid) values ( 1423351860, 4);");
            db.execSQL("insert into path (time,spotid) values ( 1423353150, -1);");


            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed1_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed1_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed1_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed1_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed1_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed2_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed2_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed2_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed2_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed2_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed3_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed3_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed3_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed3_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed3_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed4_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed4_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed4_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed4_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed4_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed5_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed5_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed5_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed5_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed5_5');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기1','상세보기 내용','detailed5_1');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기2','상세보기 내용','detailed5_2');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기3','상세보기 내용','detailed5_3');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기4','상세보기 내용','detailed5_4');");
            db.execSQL("insert into spotInfo (infoTitle,explanation,picName) values ('상세보기5','상세보기 내용','detailed5_5');");


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


    // ------------------------ "Path" table methods ----------------//

    /*
     * Creating path
     */
    public long createPath(Path path) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, path.getTime());
        values.put(KEY_SPOTID, path.getSpotId());

        // insert row
        long path_time = db.insert(TABLE_PATH, null, values);
        if(path_time == -1)   Log.e(LOG, "create Path Error");

        return path_time;
    }

    /**
     * getting all paths
     * */
    public List<Path> getAllPaths() {
        List<Path> paths = new ArrayList<Path>();
        String selectQuery = "SELECT  * FROM " + TABLE_PATH;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Path path = new Path();
                path.setTime(c.getLong(c.getColumnIndex(KEY_TIME)));
                path.setSpotId(c.getInt(c.getColumnIndex(KEY_SPOTID)));

                // adding to tags list
                paths.add(path);
            } while (c.moveToNext());
        }
        return paths;
    }



    /**
     * getting all spot from name
     * */
    public Spot getSpot(int id) {
        Spot spot = new Spot();
        String selectQuery = "SELECT  * FROM " + TABLE_SPOT + " WHERE "+ KEY_ID + " = " + id;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            spot.setLatitude(c.getFloat(c.getColumnIndex(KEY_LATITUDE)));
            spot.setLongitude(c.getFloat(c.getColumnIndex(KEY_LONGITUDE)));
            spot.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            spot.setPicName(c.getString(c.getColumnIndex(KEY_PICNAME)));
        }

        return spot;
    }

}
