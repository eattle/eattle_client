package com.example.cds.eattle_prototype_2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Manager;


public class MainActivity extends ActionBarActivity {

    //데이터베이스 관련 변수들
    DatabaseHelper db;

    //앨범의 Image Setting(미디어 DB 연결)
    static AlbumImageSetter ImageSetter;
    ImageView mImage;

    int totalPictureNum=0;//사진들의 총 개수
    long totalInterval;//사진 간격의 총합
    long standardDerivation=0;//사진 간격의 표준편차

    int folderID=0;//시간에 따라 할당될 폴더 아이디 (0부터 시작)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(getApplicationContext());

        /*Button classification = (Button)findViewById(R.id.classification);
        classification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePictureInterval();//사진의 시간간격의 총합을 구한다.
                long averageInterval = totalInterval;
                if(totalPictureNum != 0)
                    averageInterval /= totalPictureNum;

                //DB를 참조한다.
                Manager m = new Manager(totalPictureNum,averageInterval,standardDerivation);
                db.createManager(m);
            }
        });*/

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
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.classification:
                calculatePictureInterval();//사진의 시간간격의 총합을 구한다.
                long averageInterval = totalInterval;
                if (totalPictureNum != 0)
                    averageInterval /= totalPictureNum;

                //DB를 참조한다.
                Manager m = new Manager(totalPictureNum, averageInterval, standardDerivation);
                db.createManager(m);
                break;
            case R.id.manager:
                Intent toDBView = new Intent(this,ManagerDBView.class);
                startActivity(toDBView);
               break;

        }
    }

    private void calculatePictureInterval() {
        totalInterval=0;
        totalPictureNum=0;
        ImageSetter.setCursor(0,0);//커서의 위치를 처음으로 이동시킨다.
        long pictureTakenTime=0;
        while (ImageSetter.mCursor.moveToNext()) {
            //사진이 촬영된 날짜
            long _pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            _pictureTakenTime *= 1000; //second->millisecond

            totalInterval += _pictureTakenTime-pictureTakenTime;
            pictureTakenTime = _pictureTakenTime;
            totalPictureNum++;
        }
        Toast.makeText(getBaseContext(),Long.toString(totalPictureNum)+" "+Long.toString(totalInterval),Toast.LENGTH_LONG).show();
    }
    private void pictureClassification() {

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

    /*
    private void pictureClassification(){
        //사진들을 하나하나 순회하면서, 날짜별로 묶는다.
        String folderID="";
        File picture=null;//사진 하나씩 가리킬 File 객체
        File dir=null;//디렉토리 하나씩 가리킬 File 객체
        String folderName="";
        while(ImageSetter.mCursor.moveToNext()){
            picture = new File(ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
            //사진 ID
            long pictureID = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            //사진이 촬영된 날짜
            long pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            pictureTakenTime *= 1000; //second->millisecond
            //millisecond -> Calendar
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

        Toast.makeText(getBaseContext(),"사진 정리가 완료되었습니다",Toast.LENGTH_LONG).show();
    }*/

    /*
    private void pictureClassification(){
        //사진들을 하나하나 순회하면서, 날짜별로 묶는다.
        String folderID="";
        String endFolderID="";//folderID 이전의 날짜를 가리킴(연속적인 스토리의 마지막 날짜)
        String startFolderID="";//연속적인 스토리의 시작 날짜
        File picture=null;//사진 하나씩 가리킬 File 객체
        File dir=null;//디렉토리 하나씩 가리킬 File 객체
        String folderName = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM)+"/tempEattle/";

        int count=0;//특정 날짜에 몇개의 사진이 있는지
        int continuity=0;//1이면 날짜의 연속,0이면 단일 날짜
        ArrayList<PictureInfo> pictureInfos = new ArrayList<PictureInfo>();//하루 단위로 사진을 가지는 List


        while(ImageSetter.mCursor.moveToNext()){
            String picturePath = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            picture = new File(picturePath);
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
                endFolderID = folderID;
                folderID = _folderID;

                if(continuity==0){//temp 디렉토리를 새로 만든다.
                    Log.d("MainActivitiy","continuity 0 상황!!!!!!!!");
                    //이전까지 사진을 넣었던 temp 디렉토리의 이름을 바꾼다(ex) *년*월*일~*년*월~*일의 일상)
                    if(!startFolderID.equals("")) {
                        File new_name = null;

                        if (!startFolderID.equals(endFolderID)) {
                            new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 일상");
                        } else
                            new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 특별한 날");
                        FolderManage.reNameFile(dir, new_name);
                    }

                    //folderName = Environment.getExternalStorageState()+"/tempEattle/";
                    dir = FolderManage.makeDirectory(folderName);
                    startFolderID = _folderID;//startFolderID는 연속적인 스토리의 시작 날짜가 된다.

                    continuity=1; //continuity가 0이 되지 않는 이상 일단 하나의 폴더에 게속 넣는다.
                }

                //날짜 단위의 사진 이동
                //이전 날짜의 사진들을 tempEattle 폴더에 넣는다.
                for (int i = 0; i < pictureInfos.size(); i++) {
                    //사진을 임시 폴더(tempEattle)로 복사한다.
                    Log.d("MainActivitiy", pictureInfos.get(i).getPicture() + "!!!" + folderName + Long.toString(pictureInfos.get(i).get_ID()) + ".jpg");
                    FolderManage.copyFile(pictureInfos.get(i).getPicture(), folderName + Long.toString(pictureInfos.get(i).get_ID()) + ".jpg");
                }
                pictureInfos.clear();
                count = 0;
            }

            pictureInfos.add(new PictureInfo(pictureID, folderID, picturePath));
            count++;
            if(count == 5) {//한 날짜에 사진이 5개가 넘으면 독립적인 스토리로 본다.
                //현재 날짜의 사진들을 위한 폴더를 만든다.
                continuity = 0;
            }


            //사진을 새로운 폴더로 복사한다.
            //FolderManage.copyFile(picture , folderName+Long.toString(pictureID)+".jpg");
            //Log.d("MainActivity",Long.toString(pictureID)+" "+folderID);
        }

        //마지막으로 남은 tempEattle 폴더를 정리해준다.
        //FolderManage.deleteFile(new File(folderName));
        endFolderID = folderID;

        if(continuity==0){//temp 디렉토리를 새로 만든다.
            Log.d("MainActivitiy","continuity 0 상황!!!!!!!!");
            //이전까지 사진을 넣었던 temp 디렉토리의 이름을 바꾼다(ex) *년*월*일~*년*월~*일의 일상)
            if(!startFolderID.equals("")) {
                File new_name = null;

                if (!startFolderID.equals(endFolderID)) {
                    new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 일상");
                } else
                    new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 특별한 날");
                FolderManage.reNameFile(dir, new_name);
            }

            //folderName = Environment.getExternalStorageState()+"/tempEattle/";
            dir = FolderManage.makeDirectory(folderName);
            //            startFolderID = _folderID;//startFolderID는 연속적인 스토리의 시작 날짜가 된다.

            continuity=1; //continuity가 0이 되지 않는 이상 일단 하나의 폴더에 게속 넣는다.
        }

        //날짜 단위의 사진 이동
        //이전 날짜의 사진들을 tempEattle 폴더에 넣는다.
        for (int i = 0; i < pictureInfos.size(); i++) {
            //사진을 임시 폴더(tempEattle)로 복사한다.
            Log.d("MainActivitiy", pictureInfos.get(i).getPicture() + "!!!" + folderName + Long.toString(pictureInfos.get(i).get_ID()) + ".jpg");
            FolderManage.copyFile(pictureInfos.get(i).getPicture(), folderName + Long.toString(pictureInfos.get(i).get_ID()) + ".jpg");
        }
        pictureInfos.clear();
        File new_name = null;

        if (!startFolderID.equals(endFolderID)) {
            new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 일상");
        } else
            new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 특별한 날");
        FolderManage.reNameFile(dir, new_name);

        Toast.makeText(getBaseContext(),"사진 정리가 완료되었습니다",Toast.LENGTH_LONG).show();
    }*/
