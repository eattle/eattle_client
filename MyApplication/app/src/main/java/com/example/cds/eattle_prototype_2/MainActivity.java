package com.example.cds.eattle_prototype_2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Folder;
import com.example.cds.eattle_prototype_2.model.Manager;
import com.example.cds.eattle_prototype_2.model.Media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;


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

    private ListView storyList;//메인화면의 스토리 목록들이 들어가는 리스트뷰
    private StoryListAdapter storyListAdapter;//리스트뷰를 위한 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(getApplicationContext());

        //Folder DB를 기반으로 메인화면을 구성한다.
        drawMainView();
    }

    public void drawMainView(){//폴더를 기반으로 스토리의 목록을 보여준다.
        //커스텀 어댑터 생성
        storyListAdapter = new StoryListAdapter(this);
        //activity_main.xml에 있는 storyList 리스트뷰에 연결
        storyList = (ListView)findViewById(R.id.storyList);
        //ListView에 어댑터 연결
        storyList.setAdapter(storyListAdapter);

        //리스트뷰에 아이템 추가---------------------------
        //모든 폴더 목록들을 불러온다
        List<Folder> folderList = db.getAllFolders();
        if(folderList.isEmpty()) {//폴더가 정리되어 있지 않으면
            TextView tempLayout = new TextView(this);
            tempLayout.setText("앨범이 비어있어요!");
            tempLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
            tempLayout.setTextSize(20);
            tempLayout.setGravity(Gravity.CENTER);
            storyList.addView(tempLayout);
        }
        else {
            for (int i = 0; i < folderList.size(); i++) {
                StoryListItem tempItem = new StoryListItem(folderList.get(i).getImage(),folderList.get(i).getName(),folderList.get(i).getId());
                storyListAdapter.add(tempItem);
            }
        }

        /*
        LinearLayout storyList = (LinearLayout)findViewById(R.id.storyList);
        storyList.removeAllViews();//일단 기존의 스토리 목록을 지운다.
        final List<Folder> folderList = db.getAllFolders();

        if(folderList.isEmpty()) {//폴더가 정리되어 있지 않으면
            TextView tempLayout = new TextView(this);
            tempLayout.setText("앨범이 비어있어요!");
            tempLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
            tempLayout.setTextSize(20);
            tempLayout.setGravity(Gravity.CENTER);
            storyList.addView(tempLayout);
        }
        else{
            LayoutInflater inflater = (LayoutInflater)getSystemService(this.LAYOUT_INFLATER_SERVICE);

            for(int i=0;i<folderList.size();i++) {

                FrameLayout frameLayout = new FrameLayout(this);
                frameLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200));

                // 해당 레이아웃의 파라미터 값을 호출
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) frameLayout.getLayoutParams();
                // 해당 margin값 변경
                lp.setMargins(15, 15, 15, 15);
                // 변경된 값 적용
                frameLayout.setLayoutParams(lp);

                ImageView imageView = new ImageView(this);
                //imageView.setImageResource(picName);
                imageView.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ "thumbnail" +"/"+folderList.get(i).getImage()+".jpg"));

                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // 레이아웃 크기에 이미지를 맞춘다
                //listImage.setLayoutParams(new ViewGroup.LayoutParams(120,120));
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setAlpha(0.4f);

                TextView textView = (TextView)inflater.inflate(R.layout.story_list,null,false);
                textView.setText(folderList.get(i).getName());

                final long id=folderList.get(i).getId();
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AlbumLayout.class);
                        //객체배열을 ArrayList로 넘겨준다.
                        intent.putExtra("folderId", id);
                        startActivity(intent);
                    }
                });
                frameLayout.addView(imageView);
                frameLayout.addView(textView);
                storyList.addView(frameLayout);
            }
        }*/

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.classification:
                Toast.makeText(this,"사진 정리 중",Toast.LENGTH_LONG).show();
                Button classification = (Button)findViewById(R.id.classification);
                classification.setEnabled(false); // 클릭 무효화
                pictureClassification();
                classification.setEnabled(true); // 클릭 유효화
                break;

            /*
            case R.id.intervalOk:
                EditText editText = (EditText)findViewById(R.id.intervalText);
                String text = editText.getText().toString();
                if(text!=null) {//시간간격을 입력했으면
                    CONSTANT.TIMEINTERVAL = Long.parseLong(text);
                    pictureClassification();
                }
                else
                    Toast.makeText(getBaseContext(),"시간 간격을 입력하세요",Toast.LENGTH_SHORT).show();
                editText.clearFocus();*/
        }
    }

    private void calculatePictureInterval() {//사진간 시간 간격을 계산하는 함수
        totalInterval=0;
        totalPictureNum=0;
        ImageSetter.setCursor(0,0);//커서의 위치를 처음으로 이동시킨다.
        long pictureTakenTime=0;
        while (ImageSetter.mCursor.moveToNext()) {
            String path = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            Log.d("MainActivity","!!"+path);
            //썸네일 사진들은 계산대상에서 제외한다
            if(path.contains("thumbnail") || path.contains("스토리")) {
                Log.d("pictureClassification","썸네일 및 기존 스토리는 계산대상에서 제외");
                continue;
            }
            //사진이 촬영된 날짜
            long _pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            _pictureTakenTime *= 1000; //second->millisecond
            if(pictureTakenTime == 0)
                pictureTakenTime = _pictureTakenTime;

            totalInterval += _pictureTakenTime-pictureTakenTime;
            pictureTakenTime = _pictureTakenTime;
            totalPictureNum++;
        }
    }
    private void pictureClassification() {//시간간격을 바탕으로 사진들을 분류하는 함수
        //DCIM 폴더의 Eattle이 만든 폴더를 다 삭제한다(추후 변경)
        String[] folderList = FolderManage.getList(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"));
        for(int i=0;i<folderList.length;i++){
            //Log.d("!!!!",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+folderList[i]+"/"+"~~~~~~~~~~~~");
            if(!folderList[i].equals("Camera") && !folderList[i].equals("thumbnail"))
                FolderManage.deleteFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+folderList[i]+"/"));
        }
        //---------------------------------------------
        ImageSetter = new AlbumImageSetter(this, 0, 0);
        calculatePictureInterval();//사진의 시간간격의 총합을 구한다.
        long averageInterval = totalInterval;
        if (totalPictureNum != 0)
            averageInterval /= totalPictureNum;
        CONSTANT.TIMEINTERVAL=averageInterval;
        //DB를 참조한다.
        Manager _m = new Manager(totalPictureNum, averageInterval, standardDerivation);
        db.createManager(_m);//Manager DB에 값들을 집어넣음

        db.deleteAllFolder();
        db.deleteAllMedia();
        ImageSetter.setCursor(0,0);//커서의 위치를 처음으로 이동시킨다.
        File picture=null;
        File dir=null;
        String startFolderID="";
        String endFolderID="";
        long folderIDForDB=0;//Folder DB에 들어가는 아이디
        long _pictureTakenTime=0;//현재 읽고 있는 사진 이전의 찍힌 시간
        String representativeImage="";//폴더에 들어가는 대표이미지의 이름(경로제외), 일단 폴더에 들어가는 첫번째 사진으로 한다.
        String folderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/tempEattle/";
        String folderThumbnailName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/thumbnail/";
        FolderManage.makeDirectory(folderThumbnailName);


        while(ImageSetter.mCursor.moveToNext()){
            String path = ImageSetter.mCursor.getString(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

            Log.d("사진 분류",path);
            //썸네일 사진들은 분류대상에서 제외한다
            if(path.contains("thumbnail") || path.contains("스토리")) {
                Log.d("pictureClassification","썸네일 및 기존 스토리는 분류 대상에서 제외");
                continue;
            }

            picture = new File(path);
            //사진 ID
            long pictureID = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.MediaColumns._ID));
            //사진이 촬영된 날짜
            long pictureTakenTime = ImageSetter.mCursor.getLong(ImageSetter.mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            pictureTakenTime *= 1000; //second->millisecond
            //millisecond -> Calendar
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(pictureTakenTime);
            String folderID=""+cal.get(Calendar.YEAR)+"_"+(cal.get(Calendar.MONTH)+1)+"_"+cal.get(Calendar.DATE);
            if(representativeImage.equals(""))
                representativeImage = Long.toString(pictureID);
            /*
            Cursor thumbnailCursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
                    getContentResolver(), pictureID,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null );
            Log.d("thumbnail",thumbnailCursor.getCount()+"!!!");
            if( thumbnailCursor != null && thumbnailCursor.getCount() > 0 ) {
            //if( thumbnailCursor != null){
                Log.e("thumbnail", ""+pictureID);
                File picture_thumbnail = new File(thumbnailCursor.getString( thumbnailCursor.getColumnIndex( MediaStore.Images.Thumbnails.DATA ) ));
                FolderManage.copyFile(picture_thumbnail , folderThumbnailName+Long.toString(pictureID)+".jpg");
            }
            thumbnailCursor.close();
            */

            //썸네일 이미지를 생성한다
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 16;//기존 해상도의 1/16로 줄인다
            Bitmap bitmap = BitmapFactory.decodeFile(path,opt);
            createThumbnail(bitmap, folderThumbnailName, Long.toString(pictureID)+".jpg");

            Log.d("MainActivity", "[pictureID] : " + Long.toString(pictureID) + " [pictureTakenTime] : " + Long.toString(pictureTakenTime));

            //이전에 읽었던 사진과 시간 차이가 CONSTANT.TIMEINTERVAL보다 크면 새로 폴더를 만든다.
            Log.d("MainActivity","pictureTakenTime-_pictureTakenTime = "+(pictureTakenTime-_pictureTakenTime));
            if(pictureTakenTime-_pictureTakenTime > CONSTANT.TIMEINTERVAL){
                //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
                if(!startFolderID.equals("")) {
                    File new_name = null;
                    if (!startFolderID.equals(endFolderID))
                        new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 스토리");
                    else
                        new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 스토리");

                    String _new_name=FolderManage.reNameFile(dir, new_name);
                    //Folder DB에 넣는다.
                    Folder f = new Folder(folderIDForDB,_new_name,representativeImage);
                    db.createFolder(f);
                    representativeImage="";
                    Log.d("MainActivity","tempEattle 폴더 이름 변경");
                }

                //방금 읽은 사진의 folderID가 시작날짜가 된다.
                startFolderID = folderID;
                //tempEattle이라는 이름으로 임시 폴더를 만든다.
                dir = FolderManage.makeDirectory(folderName);
                folderIDForDB++;
            }
            //사진을 새로운 폴더로 복사한다.
            FolderManage.copyFile(picture , folderName+Long.toString(pictureID)+".jpg");

            //DB에 사진 데이터를 넣는다.
            Media m = new Media(pictureID,folderIDForDB,""+pictureID,cal.get(Calendar.YEAR),(cal.get(Calendar.MONTH)+1),cal.get(Calendar.DATE),0,0);
            db.createMedia(m);
            _pictureTakenTime = pictureTakenTime;
            endFolderID = folderID;
        }

        //마지막 남은 폴더를 처리한다.
        //이전에 만들어진 폴더의 이름을 바꾼다(startFolderID ~ endFolderID)
        if(!startFolderID.equals("")) {
            File new_name = null;
            if (!startFolderID.equals(endFolderID)) {
                new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "~" + endFolderID + "의 스토리");
            } else
                new_name = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + startFolderID + "의 스토리");
            String _new_name = FolderManage.reNameFile(dir, new_name);
            //Folder DB에 넣는다.
            Folder f = new Folder(folderIDForDB,_new_name,representativeImage);
            db.createFolder(f);
            representativeImage="";
            Log.d("MainActivity","tempEattle 폴더 이름 변경");
        }
        //메인화면의 스토리 목록을 갱신한다.
        drawMainView();
        Toast.makeText(getBaseContext(),"사진 정리가 완료되었습니다",Toast.LENGTH_LONG).show();
        ImageSetter.mCursor.close();
    }

    // 외장 메모리 DCIM 전체 MediaScanning
    // 킷캣 이후 버전 이후로 앱단에서 스캐닝 불가
    public static void startExtMediaScan(Context mContext){
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))));
    }
    //썸네일 생성 함수
    public static void createThumbnail(Bitmap bitmap, String strFilePath, String filename) {

        File file = new File(strFilePath);

        if (!file.exists()) {
            file.mkdirs();
            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }
        File fileCacheItem = new File(strFilePath + filename);
        //strFilePath+filename이 이미 존재한다면, 썸네일을 만들 필요가 없다
        if(fileCacheItem.exists()){
            Log.d("createThumbnail","썸네일이 이미 존재합니다");
            return;
        }

        OutputStream out = null;




        try {
            int height=bitmap.getHeight();
            int width=bitmap.getWidth();

            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
