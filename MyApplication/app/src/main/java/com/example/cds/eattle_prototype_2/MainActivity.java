package com.example.cds.eattle_prototype_2;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    //앨범의 Image Setting(미디어 DB 연결)
    static AlbumImageSetter ImageSetter;
    Cursor mCursor;
    ImageView mImage;
    int folderID=0;//시간에 따라 할당될 폴더 아이디 (0부터 시작)
    List<PictureInfo> pictureInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = (ListView)findViewById(R.id.list);
        mImage = (ImageView)findViewById(R.id.image);

        ImageSetter = new AlbumImageSetter(this,0,0);
        SimpleCursorAdapter Adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,ImageSetter.mCursor,
                new String[]{MediaStore.MediaColumns._ID},
                new int[]{android.R.id.text1});
        //list.setAdapter(Adapter);
        //list.setOnItemClickListener(mItemClickListener);
        //startManagingCursor(mCursor);

        /*안드로이드 프로그래밍 정복 예제
        ContentResolver cr = getContentResolver();//범용 콘텐츠 관리 클래스
        mCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null,null,null,null);
        SimpleCursorAdapter Adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,mCursor,
                new String[]{MediaStore.MediaColumns.DATE_ADDED},
                new int[]{android.R.id.text1});
        list.setAdapter(Adapter);
        list.setOnItemClickListener(mItemClickListener);
        startManagingCursor(mCursor);
        */

        ArrayAdapter<String> m_Adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);
        list.setAdapter(m_Adapter);
        //사진들을 하나하나 순회하면서, 날짜별로 묶는다.
        String folderID="";
        File picture=null;//사진 하나씩 가리킬 File 객체
        File dir=null;//디렉토리 하나씩 가리킬 File 객체
        String folderName="";
        while(ImageSetter.mCursor.moveToNext()){

            m_Adapter.add(ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
            picture = new File(ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
            //사진 ID
            long pictureID = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            //사진이 촬영된 날짜
            long pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            pictureTakenTime *= 1000; //second->millisecond
            //millisecond -> Date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(pictureTakenTime);
            String _folderID=""+cal.get(Calendar.YEAR)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.DATE);

            if(!folderID.equals(_folderID)){//새로운 폴더를 만들어야 하는 상황(다른 날짜의 사진을 만남)
                folderID = _folderID;
                //스마트폰 최상위 경로에 folderID로 폴더를 만든다.
                folderName = Environment.getExternalStorageDirectory()+"/"+folderID+"/";
                dir = FolderManage.makeDirectory(folderName);
            }

            //사진을 새로운 폴더로 복사한다.
            FolderManage.copyFile(picture , folderName+Long.toString(pictureID)+".jpg");

            //pictureInfos.add(new PictureInfo(pictureID,folderID));
            //Log.d("MainActivity",Long.toString(pictureID)+" "+folderID);
        }

    }

    AdapterView.OnItemClickListener mItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageSetter.mCursor.moveToPosition(position);
                    //사진들의 경로를 가져오는 부분
                    String path = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    try{
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize=4;
                        Bitmap bm = BitmapFactory.decodeFile(path,opt);
                        mImage.setImageBitmap(bm);
                    }
                    catch(OutOfMemoryError e){
                        Toast.makeText(getBaseContext(), "이미지가 너무 큽니다", Toast.LENGTH_LONG).show();
                    }
                }
            };


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
