package com.example.cds.eattle_prototype_2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.List;


public class AlbumLayout extends ActionBarActivity {



    GridView mGrid;
    List<Media> mMediaList;
    DatabaseHelper db;
    int mFolderId;
    String mFolderName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_layout);

        db = DatabaseHelper.getInstance(getApplicationContext());

        Intent intent=new Intent(this.getIntent());
        mFolderId = intent.getIntExtra("folderId", 0);
        mFolderName = db.getFolder(mFolderId).getName();


        mGrid = (GridView) findViewById(R.id.imagegrid);

//        ContentResolver cr = getContentResolver();

//        mCursor = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, null, null, null);
        mMediaList = db.getAllMediaByFolder(mFolderId);

        ImageAdapter Adapter = new ImageAdapter(this);
        mGrid.setAdapter(Adapter);

        mGrid.setOnItemClickListener(mItemClickListener);

    }

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            /*
            mCursor.moveToPosition(position);
            String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            Intent intent = new Intent(getApplicationContext(), ImageFull.class);
            intent.putExtra("path", path);
            startActivity(intent);
            */
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

        public ImageAdapter(Context c){
            mContext = c;
        }

        public int getCount(){
            return mMediaList.size();
        }

        public Object getItem(int position){
            return position;
        }

        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            ImageView imageView;
            if(convertView == null){
                imageView = new ImageView(mContext);
            }
            else{
                imageView = (ImageView)convertView;
            }

//            mCursor.moveToPosition(position);

//            Uri uri = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Thumbnails._ID)));

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ mFolderName +"/"+mMediaList.get(position).getName()+".jpg";

            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), CONSTANT.THUMBSIZE, CONSTANT.THUMBSIZE);

            imageView.setImageBitmap(ThumbImage);
//            imageView.setImageURI(Uri.parse(path));
 //           imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 400));

            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            return imageView;
        }
    }
}
