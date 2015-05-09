package com.example.cds.eattle_prototype_2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cds.eattle_prototype_2.device.BlockDevice;
import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Folder;
import com.example.cds.eattle_prototype_2.model.Media;
import com.example.cds.eattle_prototype_2.model.Media_Tag;
import com.example.cds.eattle_prototype_2.model.Tag;

import java.util.ArrayList;
import java.util.List;


public class AlbumLayout extends ActionBarActivity {

    DatabaseHelper db;

    TextView titleText;
    ImageView titleImage;

    GridView mGrid;
    List<Media> mMediaList;

    int id;
    int kind;
    String titleName;
    String titleImagePath;
//    String mFolderName;



    private BlockDevice blockDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_layout);

        // fileSystem.incaseSearchTable(blockDevice);//탐색테이블 만듬 초기화

        db = DatabaseHelper.getInstance(getApplicationContext());

        titleText = (TextView) findViewById(R.id.titleText);
        titleImage = (ImageView) findViewById(R.id.titleImage);


        //인텐트로부터 사진 검색을 위한 (folderId) 초기화
        Intent intent = new Intent(this.getIntent());
        id = intent.getIntExtra("id", -1);
        kind = intent.getIntExtra("kind", -1);

        if (kind == CONSTANT.FOLDER) {
            Folder f = db.getFolder(id);
            mMediaList = db.getAllMediaByFolder(id);

            String[] tempName = f.getName().split("_");
            titleName = tempName[0] + "년 " + tempName[1] + "월 " + tempName[2].replace("의", "일의");
            //titleImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+f.getName()+"/"+ f.getImage()+".jpg";
            titleImagePath = f.getImage();//대표 이미지의 경로를 얻는다
        } else if (intent.getIntExtra("kind", -1) == CONSTANT.DEFAULT_TAG) {
            Media m = db.getMediaById(intent.getIntExtra("mediaId", -1));
            String tagName = intent.getStringExtra("tagName");
            if (tagName.contains("년")) {
                Log.d("asdf", "년");
                mMediaList = db.getAllMediaByYear(m.getYear());
            } else if (tagName.contains("월")) {
                Log.d("asdf", "월");

                mMediaList = db.getAllMediaByMonth(m.getMonth());
            } else if (tagName.contains("일")) {
                Log.d("asdf", "일");

                mMediaList = db.getAllMediaByDay(m.getDay());
            }
            titleName = tagName + "의 추억";
            titleImagePath = mMediaList.get(0).getPath();
        } else {
            Tag t = db.getTagByTagId(id);
            Media m = db.getMediaById(intent.getIntExtra("mediaId", -1));
            Folder f = db.getFolder(m.getFolder_id());
            List<Media_Tag> temp = db.getAllMediaTag();
            mMediaList = db.getAllMediaByTagId(id);

            titleName = t.getName();
            //titleImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+f.getName()+"/"+ m.getName()+".jpg";
            titleImagePath = f.getImage();//대표 이미지의 경로를 얻는다
        }


        //폴더(스토리)의 제목 등록
        titleText.setText(titleName);
        //폴더(스토리)의 대표사진 등록
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 4;//기존 해상도의 1/4로 줄인다
        Bitmap bitmap = BitmapFactory.decodeFile(titleImagePath, opt);
        titleImage.setImageBitmap(bitmap);
        titleImage.setAlpha(0.4f);

        //그리드 뷰 등록
        mGrid = (GridView) findViewById(R.id.imagegrid);

        ImageAdapter Adapter = new ImageAdapter(this);
        mGrid.setAdapter(Adapter);

        mGrid.setOnItemClickListener(mItemClickListener);

        Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        LinearLayout albumLayout = (LinearLayout) findViewById(R.id.albumLayout);
        albumLayout.startAnimation(animationFadeIn);

    }

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), FullPicture.class);
            intent.putParcelableArrayListExtra("mediaList", new ArrayList<Parcelable>(mMediaList));
            intent.putExtra("position", position);
            startActivity(intent);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_layout, menu);
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

    class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mMediaList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + mMediaList.get(position).getName() + ".jpg";

            imageView.setImageURI(Uri.parse(path));
            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, GridView.AUTO_FIT);
            GridView.LayoutParams params = new GridView.LayoutParams(GridView.AUTO_FIT, 400);
            imageView.setLayoutParams(params);

            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            return imageView;
        }
    }
}
