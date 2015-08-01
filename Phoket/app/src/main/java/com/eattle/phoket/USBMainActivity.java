package com.eattle.phoket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Tag;

import java.util.ArrayList;
import java.util.List;


public class USBMainActivity extends ActionBarActivity {

    GridView mGrid;
    USBImageAdapter Adapter;
    List<Folder> folderList;
    List<Tag> tagList;
    int mode=0;//0이면 story별, 1이면 tag별
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbmain);

        //USB에서 사진 목록들을 읽어온다
        //TODO USB실제파일 목록과 USB DB 내용을 비교해봐야함
        DatabaseHelper db = DatabaseHelper.getInstance(USBMainActivity.this);
        //Folder DB를 읽는다 -> 그리드뷰
        folderList = db.getAllFolders();
        Toast.makeText(this,""+folderList.size(),Toast.LENGTH_SHORT).show();
        //그리드 뷰 등록
        mGrid = (GridView) findViewById(R.id.usbgrid);

        Adapter = new USBImageAdapter(this);
        mGrid.setAdapter(Adapter);

        mGrid.setOnItemClickListener(mItemClickListener);



    }

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //TODO 폴더 각각을 눌렀을 때
        }
    };

    public void onUSBButtonClick(View v){
        switch(v.getId()){
            case R.id.byStory:
                showUSBByStory();
                break;
            case R.id.byTag:
                showUSBByTag();
                break;
        }
    }
    private void showUSBByStory(){
        mode = 0;
        //1. Folder DB를 읽는다 -> 그리드뷰
        DatabaseHelper db = DatabaseHelper.getInstance(USBMainActivity.this);
        folderList = db.getAllFolders();
        //2. 그리드뷰를 refresh한다
        //Adapter.notifyDataSetChanged();
        mGrid.invalidateViews();
    }
    private void showUSBByTag(){
        mode = 1;
        //1. Folder DB를 읽는다 -> 그리드뷰
        DatabaseHelper db = DatabaseHelper.getInstance(USBMainActivity.this);
        tagList = db.getAllTags();
        Log.d("showUSBByTag","taglist.size() : "+tagList.size());
        //2. 그리드뷰를 refresh한다
        //Adapter.notifyDataSetChanged();
        mGrid.invalidateViews();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_usbmain, menu);
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

    class USBImageAdapter extends BaseAdapter {
        private Context mContext;

        public USBImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            if(mode == 0)//스토리별
                return folderList.size();
            else if(mode == 1)//태그별
                return tagList.size();
            else //에러
                return -1;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout r = (RelativeLayout) View.inflate(mContext, R.layout.each_folder_in_usb, null);
            ImageView folderImage = (ImageView)r.findViewById(R.id.folderImage);
            TextView folderName = (TextView)r.findViewById(R.id.folderName);

            //폴더 대표 이미지
            ImageView representativePicture = (ImageView)r.findViewById(R.id.representativeImage);
            if(mode == 0) {//스토리별
                String path = folderList.get(position).getThumbNail_path();
                representativePicture.setImageURI(Uri.parse(path));

                folderName.setText(folderList.get(position).getName());
            }
            else if(mode == 1){//태그별
                //TODO 태그별 대표 이미지 가져오기
                folderName.setText(tagList.get(position).getName());
            }
            return r;
        }
    }
}
