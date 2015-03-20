package com.example.cds.eattle_prototype_2.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.cds.eattle_prototype_2.model.Folder;
import com.example.cds.eattle_prototype_2.model.Manager;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GA on 2015. 3. 19..
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "FileManager";

    private static final String TABLE_MEDIA = "media";
    private static final String TABLE_FOLDER = "folder";
    private static final String TABLE_MANAGER = "manager";

    //media(사진)
    private static final String KEY_ID = "id";             //전체에서의 사진 id **primary key**
    private static final String KEY_FOLDER_ID = "folder_id";   //폴더 id (속한 스토리의 id)
    private static final String KEY_NAME = "name";        //사진 경로
    private static final String KEY_YEAR = "year";           //년
    private static final String KEY_MONTH = "month";          //월
    private static final String KEY_DAY = "day";            //일
    private static final String KEY_LATITUDE = "latitude";       //위도도
    private static final String KEY_LONGITUDE = "longitude";      //경
    private static final String KEY_TAG = "tag";         //추가 태그

    //folder
    //private static final String KEY_ID = "id";
    //private static final String KEY_NAME = "name";

    //manager
    private static final String KEY_TOTALPICTURENUM = "totalPictureNum";
    private static final String KEY_AVERAGEINTERVAL = "averageInterval";
    private static final String KEY_STANDARDDERIVATION = "standardDerivation";

    private static final String CREATE_TABLE_MEDIA = "CREATE TABLE " + TABLE_MEDIA
            + " (" + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + KEY_FOLDER_ID + " INTEGER NOT NULL, "
            + KEY_NAME + " VARCHAR(100) NOT NULL, "
            + KEY_YEAR + " INTEGER NOT NULL, "
            + KEY_MONTH + " INTEGER, "
            + KEY_DAY + " INTEGER, "
            + KEY_LATITUDE + " DOUBLE, "
            + KEY_LONGITUDE + " DOUBLE, "
            + KEY_TAG + " VARCHAR(100) " + ")";

    private static final String CREATE_TABLE_FOLDER = "CREATE TABLE " + TABLE_FOLDER
            + " (" + KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "
            + KEY_NAME + " VARCHAR(100) NOT NULL " + ")";

    private static final String CREATE_TABLE_MANAGER = "CREATE TABLE " + TABLE_MANAGER
            + " (" + KEY_TOTALPICTURENUM + " INTEGER PRIMARY KEY NOT NULL, "
            + KEY_AVERAGEINTERVAL + " LONG, "
            + KEY_STANDARDDERIVATION + " LONG" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANAGER);

        db.execSQL(CREATE_TABLE_MEDIA);
        db.execSQL(CREATE_TABLE_FOLDER);
        db.execSQL(CREATE_TABLE_MANAGER);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANAGER);


        onCreate(db);
    }


    /******************* FOLDER *******************/
    /*
     * Creating folder
     */
    public long createFolder(Folder folder){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, folder.getName());

        return db.insert(TABLE_FOLDER, null, values);
    }

    /*
     * getting all folders
     */
    public List<Folder> getAllFolders(){
        List<Folder> folders = new ArrayList<Folder>();
        String selectQuery = "SELECT * FROM " + TABLE_FOLDER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Folder f = new Folder();
                f.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                f.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                folders.add(f);
            }while(c.moveToNext());
        }

        return folders;
    }

    /*
     * getting folder by id
     */
    public Folder getFolder(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_FOLDER + " WHERE " + KEY_ID + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);

        if(c != null)
            c.moveToFirst();

        Folder f = new Folder();

        f.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        f.setName(c.getString(c.getColumnIndex(KEY_NAME)));

        return f;

    }


    /*
     * Updating a folder by id
     */
    public int updateFolder(Folder folder){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, folder.getName());

        return db.update(TABLE_FOLDER, values, KEY_ID + " = ? ", new String[] {String.valueOf(folder.getId())});
    }

    /*
     * deleting all folder with media or not
     */

    public void deleteFolder(Folder folder, boolean should_delete_all_media_in_that_folder){
        SQLiteDatabase db = this.getWritableDatabase();

        //check if media in that folder should also be deleted
        if(should_delete_all_media_in_that_folder){
            List<Media> allMedia =  getAllMediaByFolder(folder.getId());

            for(Media m : allMedia){
                deleteMedia(m.getId());
            }
        }

        db.delete(TABLE_MEDIA, KEY_ID + " = ? ", new String[]{String.valueOf(folder.getId())});
    }

    /******************* MEDIA *******************/

    /*
     * creating media
     */
    public long createMedia(Media media){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FOLDER_ID, media.getFolder_id());
        values.put(KEY_NAME, media.getName());
        values.put(KEY_YEAR, media.getYear());
        values.put(KEY_MONTH, media.getMonth());
        values.put(KEY_DAY, media.getDay());
        values.put(KEY_LATITUDE, media.getLatitude());
        values.put(KEY_LONGITUDE, media.getLongitude());
        values.put(KEY_TAG, media.getTag());

        return db.insert(TABLE_MEDIA, null, values);
    }


    /*
     * getting all media
     */
    public List<Media> getAllMedia(){
        List<Media> media = new ArrayList<Media>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDIA;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Media m = new Media();
                m.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                m.setFolder_id(c.getInt(c.getColumnIndex(KEY_FOLDER_ID)));
                m.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                m.setYear(c.getInt(c.getColumnIndex(KEY_YEAR)));
                m.setMonth(c.getInt(c.getColumnIndex(KEY_MONTH)));
                m.setDay(c.getInt(c.getColumnIndex(KEY_DAY)));
                m.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                m.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                m.setTag(c.getString(c.getColumnIndex(KEY_TAG)));

                media.add(m);
            }while(c.moveToNext());
        }

        return media;
    }

    /*
     * getting all media by folder
     */
    public List<Media> getAllMediaByFolder(int folder_id){
        List<Media> media = new ArrayList<Media>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDIA + " WHERE " + KEY_FOLDER_ID + " = " + folder_id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Media m = new Media();
                m.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                m.setFolder_id(c.getInt(c.getColumnIndex(KEY_FOLDER_ID)));
                m.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                m.setYear(c.getInt(c.getColumnIndex(KEY_YEAR)));
                m.setMonth(c.getInt(c.getColumnIndex(KEY_MONTH)));
                m.setDay(c.getInt(c.getColumnIndex(KEY_DAY)));
                m.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                m.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                m.setTag(c.getString(c.getColumnIndex(KEY_TAG)));

                media.add(m);
            }while(c.moveToNext());
        }

        return media;
    }

    /*
     * updating media
     */
    public int updateMedia(Media media){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FOLDER_ID, media.getFolder_id());
        values.put(KEY_NAME, media.getName());
        values.put(KEY_YEAR, media.getYear());
        values.put(KEY_MONTH, media.getMonth());
        values.put(KEY_DAY, media.getDay());
        values.put(KEY_LATITUDE, media.getLatitude());
        values.put(KEY_LONGITUDE, media.getLongitude());
        values.put(KEY_TAG, media.getTag());

        return  db.update(TABLE_MEDIA, values, KEY_ID + " = ? ", new String[] {String.valueOf(media.getId())});

    }

    /*
     * Deleting media by id
     */
    public void deleteMedia(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDIA, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }


    /******************* MANAGER *******************/
    /*
     * creating Manager
     */
    public long createManager(Manager manager){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MANAGER,null,null);//기존의 데이터들을 모두 삭제한다.

        ContentValues values = new ContentValues();
        values.put(KEY_TOTALPICTURENUM, manager.getTotalPictureNum());
        values.put(KEY_AVERAGEINTERVAL, manager.getAverageInterval());
        values.put(KEY_STANDARDDERIVATION, manager.getStandardDerivation());

        return db.insert(TABLE_MANAGER, null, values);
    }

    public Manager getManager(){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MANAGER;

        Cursor c = db.rawQuery(selectQuery,null);
        if(c!=null)
            c.moveToFirst();

        Manager m = new Manager();
        m.setTotalPictureNum(c.getInt(c.getColumnIndex(KEY_TOTALPICTURENUM)));
        m.setAverageInterval(c.getLong(c.getColumnIndex(KEY_AVERAGEINTERVAL)));
        m.setStandardDerivation(c.getLong(c.getColumnIndex(KEY_STANDARDDERIVATION)));

        return m;
    }


}
