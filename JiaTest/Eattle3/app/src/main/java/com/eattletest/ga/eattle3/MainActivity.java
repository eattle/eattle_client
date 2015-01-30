package com.eattletest.ga.eattle3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    WordDBHelper mHelper;
    //
    EditText mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new WordDBHelper(this);
        mText = (EditText)findViewById(R.id.edittext);
    }

    public void mOnClick(View v){
        SQLiteDatabase db;
        ContentValues row;
        switch (v.getId()){
            case R.id.insert:
                db = mHelper.getWritableDatabase();

                //row = new ContentValues();
                //row.put("eng", "boy");
                //row.put("han", "머스마");
                //db.insert("dic", null, row);
                db.execSQL("INSERT INTO dic VALUES (null, 'boy', '머스마 ');");
                mHelper.close();
                mText.setText("Insert Success");
                break;
            case R.id.delete:
                db = mHelper.getWritableDatabase();
                //db.delete("dic", "eng =?", new String[]{"boy"});
                db.execSQL("DELETE FROM dic WHERE eng = 'boy';");
                mHelper.close();
                mText.setText("Delete Success");
                break;
            case R.id.update:
                db = mHelper.getWritableDatabase();
                row = new ContentValues();
                row.put("han", "소년");
//                db.update("dic", row, "eng = 'boy'", null);
                db.execSQL("UPDATE dic SET han = '소년' WHERE eng = 'boy';");
                mHelper.close();
                mText.setText("Update Success");
                break;
            case R.id.select:
                db = mHelper.getReadableDatabase();
                Cursor cursor;
                cursor = db.query("dic", new String[]{"eng", "han"}, null, null, null, null, null);
// 결과값 리턴
// cursor = db.rawQuery("SELECT eng, han FROM dic", null);

                String Result = "";

                while(cursor.moveToNext()){
                    String eng = cursor.getString(0);
                    String han = cursor.getString(1);
                    Result += (eng + " = " + han + "\n");
                }

                if(Result.length() == 0){
                    mText.setText("Empty Set");
                } else {
                    mText.setText(Result);
                }

                cursor.close();
                mHelper.close();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

class WordDBHelper extends SQLiteOpenHelper{

    public WordDBHelper(Context context){
        super(context, "EngWord.db", null, 1);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE dic ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " + "eng TEXT, han TEXT);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS dic");
        onCreate(db);
    }
}