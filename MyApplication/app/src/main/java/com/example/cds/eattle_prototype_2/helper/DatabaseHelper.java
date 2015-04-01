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
import com.example.cds.eattle_prototype_2.model.Media_Tag;
import com.example.cds.eattle_prototype_2.model.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GA on 2015. 3. 19..
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    private static DatabaseHelper Instance;

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "FileManager";

    private static final String TABLE_MEDIA = "media";
    private static final String TABLE_FOLDER = "folder";
    private static final String TABLE_MANAGER = "manager";
    private static final String TABLE_TAG = "tag";
    private static final String TABLE_MEDIA_TAG = "media_tag";

    //media(사진)
    private static final String KEY_ID = "id";             //전체에서의 사진 id **primary key**
    private static final String KEY_FOLDER_ID = "folder_id";   //폴더 id (속한 스토리의 id)
    private static final String KEY_NAME = "name";        //사진 경로
    private static final String KEY_YEAR = "year";           //년
    private static final String KEY_MONTH = "month";          //월
    private static final String KEY_DAY = "day";            //일
    private static final String KEY_LATITUDE = "latitude";       //위도도
    private static final String KEY_LONGITUDE = "longitude";      //경

    //tag
    //private static final String KEY_ID = "id";
    //private static final String KEY_NAME = "name";

    //media_tag
    //private static final String KEY_ID = "id";
    private static final String KEY_TAG_ID = "tag_id";
    private static final String KEY_MEDIA_ID = "media_id";

    //folder
    //private static final String KEY_ID = "id";
    //private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE = "image";

    //manager
    private static final String KEY_TOTALPICTURENUM = "totalPictureNum";
    private static final String KEY_AVERAGEINTERVAL = "averageInterval";
    private static final String KEY_STANDARDDERIVATION = "standardDerivation";

    private static final String CREATE_TABLE_MEDIA =
            "CREATE TABLE " + TABLE_MEDIA + " ("
            + KEY_ID + " LONG PRIMARY KEY NOT NULL, "
            + KEY_FOLDER_ID + " LONG NOT NULL, "
            + KEY_NAME + " VARCHAR(100) NOT NULL, "
            + KEY_YEAR + " INTEGER NOT NULL, "
            + KEY_MONTH + " INTEGER NOT NULL, "
            + KEY_DAY + " INTEGER NOT NULL, "
            + KEY_LATITUDE + " DOUBLE, "
            + KEY_LONGITUDE + " DOUBLE "
            + ")";

    private static final String CREATE_TABLE_TAG =
            "CREATE TABLE " + TABLE_TAG + " ("
                    + KEY_ID + " LONG PRIMARY KEY NOT NULL, "
                    + KEY_NAME + " VARCHAR(100) NOT NULL "
                    + ")";

    private static final String CREATE_TABLE_MEDIA_TAG =
            "CREATE TABLE " + TABLE_MEDIA_TAG + " ("
                    + KEY_ID + " LONG PRIMARY KEY NOT NULL, "
                    + KEY_TAG_ID + " LONG NOT NULL, "
                    + KEY_MEDIA_ID + " LONG NOT NULL "
                    + ")";

    private static final String CREATE_TABLE_FOLDER =
            "CREATE TABLE " + TABLE_FOLDER + " ("
            + KEY_ID + " LONG PRIMARY KEY NOT NULL, "
            + KEY_NAME + " VARCHAR(255) NOT NULL, "
            + KEY_IMAGE + " VARCHAR(255) NOT NULL "
            + ")";

    private static final String CREATE_TABLE_MANAGER =
            "CREATE TABLE " + TABLE_MANAGER + " ("
            + KEY_TOTALPICTURENUM + " INTEGER PRIMARY KEY NOT NULL, "
            + KEY_AVERAGEINTERVAL + " LONG, "
            + KEY_STANDARDDERIVATION + " LONG"
            + ")";

    public static DatabaseHelper getInstance(Context context){
        if(Instance == null){
            Instance = new DatabaseHelper(context.getApplicationContext());
        }
        return Instance;
    }

    public DatabaseHelper(Context context) {


        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Database Helper onCreate 함수 호출");


        db.execSQL(CREATE_TABLE_MEDIA);
        db.execSQL(CREATE_TABLE_TAG);
        db.execSQL(CREATE_TABLE_MEDIA_TAG);
        db.execSQL(CREATE_TABLE_FOLDER);
        db.execSQL(CREATE_TABLE_MANAGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //기존의 테이블들을 일단 삭제하고 새로 만든다
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA_TAG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANAGER);
        } catch (Exception ex) {
            Log.e("DatabaseHelper", "Exception in DROP_SQL", ex);
        }

        onCreate(db);
    }


    /******************* FOLDER *******************/
    /*
     * Creating folder
     */
    public long createFolder(Folder folder){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, folder.getId());
        values.put(KEY_NAME, folder.getName());
        values.put(KEY_IMAGE, folder.getImage());

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

        /*
        if(c.moveToFirst()){
            do{
                Folder f = new Folder();
                f.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                f.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                folders.add(f);
            }while(c.moveToNext());
        }*/
        while(c.moveToNext()){
            Folder f = new Folder();
            f.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            f.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            f.setImage(c.getString(c.getColumnIndex(KEY_IMAGE)));
            folders.add(f);
        }

        return folders;
    }

    /*
     * getting folder by id
     */
    public Folder getFolder(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_FOLDER + " WHERE " + KEY_ID + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);

        if(c != null)
            c.moveToFirst();

        Folder f = new Folder();

        f.setId(c.getLong(c.getColumnIndex(KEY_ID)));
        f.setName(c.getString(c.getColumnIndex(KEY_NAME)));
        f.setImage(c.getString(c.getColumnIndex(KEY_IMAGE)));

        return f;

    }


    /*
     * Updating a folder by id
     * return number of updated row
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

    public void deleteAllFolder(){
        Log.d("DatabaseHelper","deleteAllFolder() 호출");
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_FOLDER);
    }





    /******************* MEDIA *******************/

    /*
     * creating media
     */
    public long createMedia(Media media){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, media.getId());
        values.put(KEY_FOLDER_ID, media.getFolder_id());
        values.put(KEY_NAME, media.getName());
        values.put(KEY_YEAR, media.getYear());
        values.put(KEY_MONTH, media.getMonth());
        values.put(KEY_DAY, media.getDay());
        values.put(KEY_LATITUDE, media.getLatitude());
        values.put(KEY_LONGITUDE, media.getLongitude());

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
                m.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                m.setFolder_id(c.getInt(c.getColumnIndex(KEY_FOLDER_ID)));
                m.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                m.setYear(c.getInt(c.getColumnIndex(KEY_YEAR)));
                m.setMonth(c.getInt(c.getColumnIndex(KEY_MONTH)));
                m.setDay(c.getInt(c.getColumnIndex(KEY_DAY)));
                m.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                m.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));

                media.add(m);
            }while(c.moveToNext());
        }

        return media;
    }

    /*
     * getting all media by folder
     */
    public List<Media> getAllMediaByFolder(long folder_id){
        List<Media> media = new ArrayList<Media>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDIA + " WHERE " + KEY_FOLDER_ID + " = " + folder_id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Media m = new Media();
                m.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                m.setFolder_id(c.getInt(c.getColumnIndex(KEY_FOLDER_ID)));
                m.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                m.setYear(c.getInt(c.getColumnIndex(KEY_YEAR)));
                m.setMonth(c.getInt(c.getColumnIndex(KEY_MONTH)));
                m.setDay(c.getInt(c.getColumnIndex(KEY_DAY)));
                m.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                m.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));

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

        return  db.update(TABLE_MEDIA, values, KEY_ID + " = ? ", new String[] {String.valueOf(media.getId())});

    }

    /*
     * Deleting media by id
     */
    public void deleteMedia(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDIA, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        deleteMediaTagByMediaId(id);
    }

    public void deleteAllMedia(){
        Log.d("DatabaseHelper","deleteAllMedia() 호출");
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MEDIA);
        deleteAllMediaTag();
    }

    /******************* TAG *******************/

    /*
     * creating tag at media_id
     */
    public long createTag(String tag_name, long media_id){
        SQLiteDatabase db = this.getWritableDatabase();

        long tag_id = getTagIdByTagName(tag_name);
        if(tag_id == 0) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, tag_name);

            tag_id = db.insert(TABLE_TAG, null, values);
        }

        createMediaTag(tag_id, media_id);

        return tag_id;
    }

    /*
    * creating tag at folder_id
    */
    public long createTagByFolder(String tag_name, long folder_id){
        List<Media> media = getAllMediaByFolder(folder_id);

        SQLiteDatabase db = this.getWritableDatabase();

        //일단 태그 만들어줌
        long tag_id = getTagIdByTagName(tag_name);
        if(tag_id == 0) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, tag_name);

            tag_id = db.insert(TABLE_TAG, null, values);
        }

        for(int i = 0, n = media.size(); i < n; i++){
            createMediaTag(tag_id, media.get(i).getId());
        }

        return tag_id;
    }

    /*
     * getting all tags
     */
    public List<Tag> getAlltag(){
        List<Tag> tags = new ArrayList<Tag>();
        String selectQuery = "SELECT * FROM " + TABLE_TAG;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Tag t = new Tag();
                t.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                t.setName(c.getString(c.getColumnIndex(KEY_NAME)));

                tags.add(t);
            }while(c.moveToNext());
        }

        return tags;
    }

    /*
     * getting Tag by Tag id
     */
    public Tag getAllTagByTagId(long tag_id){
        Tag tag = new Tag();
//        List<Tag> tags = new ArrayList<Tag>();
        String selectQuery = "SELECT * FROM " + TABLE_TAG + " WHERE " + KEY_ID + " = " + tag_id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            tag.setId(c.getLong(c.getColumnIndex(KEY_ID)));
            tag.setName(c.getString(c.getColumnIndex(KEY_NAME)));
        }

        return tag;
    }

    /*
    * getting Tag by Tag name
    */
    public long getTagIdByTagName(String tag_name){
        String selectQuery = "SELECT * FROM " + TABLE_TAG + " WHERE " + KEY_NAME + " = " + tag_name;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            return c.getLong(c.getColumnIndex(KEY_ID));
        }

        return 0;
    }



    /*
     * updating tag
     * 태그 이름이 바뀔 경우 사용
     */
    public int updateTag(Tag tag){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, tag.getId());
        values.put(KEY_NAME, tag.getName());

        return  db.update(TABLE_TAG, values, KEY_ID + " = ? ", new String[] {String.valueOf(tag.getId())});
    }

    /*
     * Deleting tag by id
     */
    public void deleteTagById(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TAG, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        deleteMediaTagByTagId(id);
    }

    /*
     * Deleting tag by name
     */
    public void deleteTagByName(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        long tag_id = getTagIdByTagName(name);
        deleteTagById(tag_id);
        deleteMediaTagByTagId(tag_id);
//        db.delete(TABLE_TAG, KEY_NAME + " = ?", new String[]{String.valueOf(name)});
    }


    public void deleteAllTag(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_TAG);
        deleteAllMediaTag();
    }


    /******************* MEDIA_TAG *******************/

    /*
     * creating media to tag relation
     */
    public long createMediaTag(Media_Tag relation){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TAG_ID, relation.getTag_id());
        values.put(KEY_MEDIA_ID, relation.getMedia_id());
        return db.insert(TABLE_MEDIA_TAG, null, values);
    }

    /*
     * creating media to tag relation
     */
    public long createMediaTag(long tag_id, long media_id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TAG_ID, tag_id);
        values.put(KEY_MEDIA_ID, media_id);
        return db.insert(TABLE_MEDIA_TAG, null, values);
    }

    /*
     * deleting single media to tag relation
     */
    public void deleteMediaTag(long tag_id, long media_id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDIA_TAG, KEY_TAG_ID + " = ? AND " + KEY_MEDIA_ID + " = ?", new String[]{String.valueOf(tag_id), String.valueOf(media_id)});
    }

    /*
     * deleting media to tag relation by tag_id
     */
    public void deleteMediaTagByTagId(long tag_id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDIA_TAG, KEY_TAG_ID + " = ?", new String[]{String.valueOf(tag_id)});
    }

    /*
     * deleting media to tag relation by media_id
     */
    public void deleteMediaTagByMediaId(long media_Id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDIA_TAG, KEY_MEDIA_ID + " = ?", new String[]{String.valueOf(media_Id)});
    }

    /*
     * deleting media to tag relation by media_id
     */
    public void deleteAllMediaTag(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_MEDIA_TAG);
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

    public List<Manager> getManagers(){
        List<Manager> managers = new ArrayList<Manager>();
        String selectQuery = "SELECT * FROM " + TABLE_MANAGER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if(c.moveToFirst()){
            do{
                Manager m = new Manager();
                m.setTotalPictureNum(c.getInt(c.getColumnIndex(KEY_TOTALPICTURENUM)));
                m.setAverageInterval(c.getLong(c.getColumnIndex(KEY_AVERAGEINTERVAL)));
                m.setAverageInterval(c.getLong(c.getColumnIndex(KEY_STANDARDDERIVATION)));
                managers.add(m);
            }while(c.moveToNext());
        }

        return managers;

    }


}
