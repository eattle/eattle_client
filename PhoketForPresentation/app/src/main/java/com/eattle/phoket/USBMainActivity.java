package com.eattle.phoket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Tag;

import java.util.List;


public class USBMainActivity extends ActionBarActivity {

    GridView mGrid;
    USBImageAdapter Adapter;
    List<Folder> folderList;
    List<Tag> tagList;
    int mode=0;//0이면 story별, 1이면 tag별
    //데이터베이스 관련
    DatabaseHelper db;
    Button byPhoket;
    Button byStory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbmain);


        byStory = (Button)findViewById(R.id.byStory);
        byPhoket = (Button)findViewById(R.id.byPhoket);
        //USB에서 사진 목록들을 읽어온다
        //TODO USB실제파일 목록과 USB DB 내용을 비교해봐야함
        db = DatabaseHelper.getInstance(this);
        //Folder DB를 읽는다 -> 그리드뷰
        folderList = db.getAllFolders();
        //Toast.makeText(this,""+folderList.size(),Toast.LENGTH_SHORT).show();
        //그리드 뷰 등록
        mGrid = (GridView) findViewById(R.id.usbgrid);

        Adapter = new USBImageAdapter(this);
        mGrid.setAdapter(Adapter);

        mGrid.setOnItemClickListener(mItemClickListener);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        //actionBar.setDisplayShowHomeEnabled(false);
        //actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setDisplayShowHomeEnabled(false);
        //actionBar.setDisplayShowTitleEnabled(false);
        LinearLayout actionBarLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.actionbar_usbmain, null);

        ImageView drawerImageView = (ImageView)actionBarLayout.findViewById(R.id.home_icon);

        //홈버튼
        drawerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView drawerImageViewCheck = (ImageView)actionBarLayout.findViewById(R.id.search_icon);

        drawerImageViewCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(actionBarLayout);
        //actionbar가 너비 전체를 차지하지 않는 문제를 해결하기 위해
        Toolbar parent = (Toolbar) actionBarLayout.getParent();
        parent.setContentInsetsAbsolute(0, 0);
    }

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //TODO 폴더 각각을 눌렀을 때
            if(mode == 0) {//스토리 단위로 보고있을 때
                Intent intent = new Intent(getApplicationContext(), AlbumGridActivity.class);
                intent.putExtra("kind", CONSTANT.FOLDER);
                intent.putExtra("id", folderList.get(position).getId());
                startActivity(intent);
            }
            else if(mode == 1){//포켓 단위로 보고있을 때
                Intent intent = new Intent(getApplicationContext(), AlbumGridActivity.class);
                intent.putExtra("kind", CONSTANT.TAG);
                intent.putExtra("id", tagList.get(position).getId());
                startActivity(intent);
            }
        }
    };

    public void onUSBButtonClick(View v){
        switch(v.getId()){
            case R.id.byStory:
                byStory.setBackground(getResources().getDrawable(R.mipmap.storybuttonpressed));
                byPhoket.setBackground(getResources().getDrawable(R.mipmap.phoketbutton));
                showUSBByStory();
                break;
            case R.id.byPhoket:
                byStory.setBackground(getResources().getDrawable(R.mipmap.storybutton));
                byPhoket.setBackground(getResources().getDrawable(R.mipmap.phoketbuttonpressed));
                showUSBByTag();
                break;
        }
    }
    private void showUSBByStory(){
        mode = 0;
        //1. Folder DB를 읽는다 -> 그리드뷰
        folderList = db.getAllFolders();
        //2. 그리드뷰를 refresh한다
        //Adapter.notifyDataSetChanged();
        mGrid.invalidateViews();
    }
    private void showUSBByTag(){
        mode = 1;
        //1. Folder DB를 읽는다 -> 그리드뷰
        tagList = db.getAllTags();
        Log.d("showUSBByTag","taglist.size() : "+tagList.size());
        //2. 그리드뷰를 refresh한다
        //Adapter.notifyDataSetChanged();
        mGrid.invalidateViews();
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_usbmain, menu);
        return true;
    }*/
/*
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
    }*/

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
            if(mode == 0){//스토리별
                folderName.setText(CONSTANT.convertFolderNameToDate(folderList.get(position).getName()));
            }
            else if(mode == 1){//태그별
                folderName.setText(tagList.get(position).getName());
            }
/*
            //폴더 대표 이미지
            ImageView representativePicture = (ImageView)r.findViewById(R.id.representativeImage);
            if(mode == 0) {//스토리별
                /String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + folderList.get(position).getThumbNail_name() + ".jpg";
                representativePicture.setImageURI(Uri.parse(path));

                folderName.setText(folderList.get(position).getName());
            }
            else if(mode == 1){//태그별

                //TODO 태그별 대표 이미지 가져오기
                folderName.setText(tagList.get(position).getName());
            }*/
            return r;
        }
    }
}
