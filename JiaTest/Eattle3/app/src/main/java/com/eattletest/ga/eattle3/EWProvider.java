package com.eattletest.ga.eattle3;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by GA on 2015. 1. 21..
 */
public class EWProvider extends ContentProvider{
    static final  Uri CONTENT_URI = Uri.parse("content://com.eattletest.ga.eattle3.MainActivity/word");
    static  final int ALLWORD = 1;
    static  final int ONEWORD = 2;

    static final UriMatcher Matcher;
    static {
        Matcher = new UriMatcher(UriMatcher.NO_MATCH);
        Matcher.addURI("com.eattletest.ga.eattle3.MainActivity", "word", ALLWORD);
        Matcher.addURI("com.eattletest.ga.eattle3.MainActivity", "word/*", ONEWORD);
    }

    SQLiteDatabase mDB;

    public boolean onCreate(){
        WordDBHelper helper = new WordDBHelper(getContext());
        mDB = helper.getWritableDatabase();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String sql;

        sql = "SELECT eng, han FROM dic";

        if(Matcher.match(uri) == ONEWORD)
            sql += " where eng = '" + uri.getPathSegments().get(1) + "'";
        Cursor cursor = mDB.rawQuery(sql, null);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        if(Matcher.match(uri) == ALLWORD)
            return "vnd.EnglishWord.ver4_1.andexam.cursor.item/word";
        if(Matcher.match(uri) == ONEWORD)
            return "vnd.EnglishWord.ver4_1.andexam.cursor.dir/word";
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = mDB.insert("dic", null, values);
        if(row > 0){
            Uri notiuri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(notiuri, null);
            return notiuri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (Matcher.match(uri)){
            case ALLWORD:
                count = mDB.delete("dic", selection, selectionArgs);
                break;
            case ONEWORD:
                String where;
                where = "eng = '" + uri.getPathSegments().get(1) + "'";
                if(TextUtils.isEmpty(selection) == false)
                    where += "AND" + selection;
                count = mDB.delete("dic", where, selectionArgs);
                break;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (Matcher.match(uri)){
            case ALLWORD:
                count = mDB.update("dic", values, selection, selectionArgs);
                break;
            case ONEWORD:
                String where;
                where = "eng = '" + uri.getPathSegments().get(1) + "'";
                if(TextUtils.isEmpty(selection) == false){
                    where += " AND " + selection;
                }
                count = mDB.update("dic", values, where, selectionArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
