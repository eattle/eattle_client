package com.eattle.phoket;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eattle.phoket.device.BlockDevice;
import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//스토리 그리드뷰에서 특정 사진을 클릭했을 때, 뷰페이저를 만들어주는 부분
public class AlbumFullActivity extends ActionBarActivity {

    DatabaseHelper db;
    List<Media> mMediaList;
    int initialMediaPosition;
    //USB에서 사진을 불러오기 위한 변수
    FileSystem fileSystem;
    int totalPictureNum;

    //'스토리 시작'을 통해 들어왔을 경우
    String titleName;
    String titleImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = DatabaseHelper.getInstance(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_full);

        Intent intent = getIntent();
        mMediaList = intent.getParcelableArrayListExtra("mediaList");
//        int folderId = intent.getIntExtra("folderId", 0);
        initialMediaPosition = intent.getIntExtra("position", 0);
        totalPictureNum = intent.getIntExtra("totalPictureNum",0);
        if(initialMediaPosition == -1) {//'스토리 시작'버튼으로 들어왔을 경우
            titleName = intent.getStringExtra("titleName");
            titleImagePath = intent.getStringExtra("titleImagePath");
            Log.d("testestestest",titleName+"!!"+titleImagePath);
        }
        fileSystem = FileSystem.getInstance();

        //뷰페이저 생성
        ExtendedViewPager mViewPager = (ExtendedViewPager) findViewById(R.id.albumFull);
        mViewPager.setAdapter(new TouchImageAdapter());//뷰페이저 어댑터 설정
        if (initialMediaPosition != -1)//-1이면 스토리 처음부터 시작(제목화면부터)
            mViewPager.setCurrentItem(initialMediaPosition);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (initialMediaPosition == -1) {  //스토리 제목부터 시작해야 하는 경우
                    position--;//첫화면에 제목화면을 넣기 위해.
                    if (position == -1)
                        return;
                }
                setTabToTag(mMediaList.get(position));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (initialMediaPosition == -1)  //스토리 제목부터 시작해야 하는 경우
                    position--;//첫화면에 제목화면을 넣기 위해
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    class TouchImageAdapter extends PagerAdapter {

        //하나의 이미지에 하나 이상의 태그가 있기 때문에 ArrayList를 선언한다
        ArrayList<TagsOverAlbum> tagArrayList = new ArrayList<TagsOverAlbum>();

        @Override
        public int getCount() {
            if (initialMediaPosition != -1)//default
                return mMediaList.size();
            else//-1이면 스토리 처음부터 시작(제목화면부터)
                return mMediaList.size() + 1;//하나가 더 추가됨
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {

            if (initialMediaPosition == -1) { //스토리 제목부터 시작해야 하는 경우
                position--;//첫화면에 제목화면을 넣기 위해.

                if (position == -1) { //제목화면
                    FrameLayout r = (FrameLayout) View.inflate(getBaseContext(), R.layout.story_start, null);
                    //대표 이미지
                    try {
                        ImageView storyStartImage = (ImageView) r.findViewById(R.id.storyStartImage);
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 1;
                        Bitmap bm = BitmapFactory.decodeFile(titleImagePath, opt);
                        storyStartImage.setImageBitmap(bm);
                    }
                    catch (OutOfMemoryError e) {
                        Log.e("warning", "이미지가 너무 큽니다");
                    }

                    //날짜
                    TextView storyStartDate = (TextView)r.findViewById(R.id.storyStartDate);
                    storyStartDate.setText("");

                    //제목
                    TextView storyStartTitle = (TextView)r.findViewById(R.id.storystartTitle);
                    storyStartTitle.setText(titleName);

                    //종료버튼(x)
                    ImageView storyStartExit = (ImageView)r.findViewById(R.id.storyStartExit);
                    storyStartExit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                    container.addView(r);
                    return r;
                }
            }

            TouchImageView img = new TouchImageView(container.getContext());

            final Media m = mMediaList.get(position);

            String path = m.getPath();//사진의 경로를 가져온다
            //TODO 사진 경로에 사진이 없을 경우를 체크한다
            //사진은 USB에서 읽어오는 것을 표준으로 한다
            try {
                /*
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = 4;
                Bitmap bm = BitmapFactory.decodeFile(path, opt);
                img.setImageBitmap(bm);*/
                if(CONSTANT.ISUSBCONNECTED == 1) {//USB가 연결되어 있을 때
                    Bitmap bm = fileoutimage(m.getName() + ".jpg", CONSTANT.BLOCKDEVICE);
                    img.setImageBitmap(bm);
                }
                else{
                    File isExist = new File(path);
                    if(!isExist.exists()){
                        //사진 파일이 로컬에 존재하지 않고 USB에만 있다고 판단될 때
                        Toast.makeText(getApplicationContext(),"사진이 존재하지 않습니다. USB를 연결하세요",Toast.LENGTH_SHORT).show();
                        return null;
                    }

                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 4;
                    Bitmap bm = BitmapFactory.decodeFile(path, opt);
                    img.setImageBitmap(bm);
                }

            } catch (OutOfMemoryError e) {
                Log.e("warning", "이미지가 너무 큽니다");
            }

            container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


            //태그를 불러오기 위한 클릭 리스너
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pushTabToTag(m);
                    setPlacePopup(m);
                }
            });

            return img;
            /*
            //종료 버튼(x)
            ImageView storyContentExit = new ImageView(getApplicationContext());
            storyContentExit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            storyContentExit.setBackgroundResource(R.mipmap.close);
            storyContentExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            container.addView(storyContentExit);*/
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (initialMediaPosition == -1)  //스토리 제목부터 시작해야 하는 경우
                position--;//첫화면에 제목화면을 넣기 위해.
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    Fragment isThereTabToTagHere() {
        return getFragmentManager().findFragmentById(R.id.tagLayout);
    }

    void setTabToTag(Media m){
        if(isThereTabToTagHere() != null){
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m);
            tr.replace(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }

    void pushTabToTag(Media m){
        Fragment f;
        if((f = isThereTabToTagHere()) != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            //tagArrayList에 있는 모든 태그들을 삭제한다
            tr.remove(f);
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
        else{
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m);
            tr.add(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }

    /*
    void pushTabToTag(Media m,int position) {
        Fragment f;
        if ((f = isThereTabToTagHere()) != null) {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            //tagArrayList에 있는 모든 태그들을 삭제한다
            tr.remove(f);
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        } else {
            FragmentTransaction tr = getFragmentManager().beginTransaction();
            TagsOverAlbum ttt = TagsOverAlbum.newInstance(m,position,totalPictureNum);
            tr.add(R.id.tagLayout, ttt, "TabToTag");
            tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tr.commit();
        }
    }*/

    void setPlacePopup(Media m) {
        //장소명이 존재하면 태그로 추가할지 묻는다
        if (!m.getPlaceName().equals("")) {
            //일단 m.getPlaceName()이 태그 목록에 있는지 확인한다
            int tagId = db.getTagIdByTagName(m.getPlaceName());
            //1. 해당 장소명으로 태그가 아예 존재하지 않을 때 -> 묻는다
            if (tagId == 0) {
                Intent intent = new Intent(getApplicationContext(), PopupForTagAddition.class);
                DataForTagAddition tempData = new DataForTagAddition(tagId, m.getId(), m.getFolder_id(), m.getPlaceName());
                intent.putExtra("dataForTagAddition", tempData);
                startActivity(intent);
            }
            //2. 해당 장소명으로 태그가 존재하는데, 해당 폴더에 등록되어 있지 않을 때 -> 묻는다
            else if (db.getMediaTagByIds(tagId, m.getId()) == 0) {
                Intent intent = new Intent(getApplicationContext(), PopupForTagAddition.class);
                DataForTagAddition tempData = new DataForTagAddition(tagId, m.getId(), m.getFolder_id(), m.getPlaceName());
                intent.putExtra("dataForTagAddition", tempData);
                startActivity(intent);
            }
            //(한번 물어봤는데 아니요로 대답할 시에 다음부터 묻지 않는다 -> 추후구현(아니오라고 대답했을 때 태그-미디어 DB에 등록하고 추가적인 flag를 다는 방식으로)
        }

    }

    private Bitmap fileoutimage(String outString, CachedBlockDevice blockDevice) {//USB -> 스마트폰
        //D  S   X
        //1220879 1870864 2133464

        int result[] = fileSystem.stringSearch(outString);
        byte[] dummyBuffer = new byte[(int) fileSystem.CLUSTERSPACESIZE];
        //1866136
        //result[0] = 4096;
        //result[0] = 6505;
        Log.d("xxxxxx", "result[0] " + result[0]);
        if (result[0] == -1) {
            Toast.makeText(this, "값이 잘못들어왔습니다", Toast.LENGTH_SHORT).show();
            return null;
        } else {

            byte resultbyte[] = new byte[result[4]];
            //int resultstringaddress = 6085;
            int resultstringaddress = result[0];
            //int resultaddress = readIntToBinary(result[0],result[1]+80,LOCATIONSIZE);

            int limit = 0;
            int bytecnt = 0;


            blockDevice.readBlock(resultstringaddress, dummyBuffer);

            while (resultstringaddress != 0) {

                int originalbyteAddress = fileSystem.readIntToBinary(resultstringaddress, limit, fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);

                blockDevice.readBlock(originalbyteAddress, fileSystem.buffer);
                for (int i = 0; i < fileSystem.CLUSTERSPACESIZE; i++) {
                    if (bytecnt < result[4]) {
                        resultbyte[bytecnt++] = fileSystem.buffer[i];
                    } else
                        break;
                }
                if (bytecnt >= result[4])
                    break;

                limit += fileSystem.LOCATIONSIZE;

                if (limit >= fileSystem.SPACELOCATION) {
                    resultstringaddress = fileSystem.readIntToBinary(resultstringaddress, fileSystem.NEXTLOCATION, fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);
                    blockDevice.readBlock(resultstringaddress, dummyBuffer);
                    limit = 0;
                }

            }


            Log.d("xxxxxx", "xxxxxxxxxxxx " + resultbyte);
            Log.d("xxxxxx", "xxxxxxxxxxxxxxxxxxx " + resultbyte.length);

            Toast.makeText(this, "1 " + resultbyte, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "1 " + resultbyte.length, Toast.LENGTH_SHORT).show();

            Bitmap byteimage = BitmapFactory.decodeByteArray(resultbyte, 0, resultbyte.length);
            //imageView.setImageBitmap(byteimage);

            //imageView.setImageBitmap(resizeBitmapImageFn(byteimage,540));

            //Bitmap bitmap1 = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera/1.jpg");
            //imageView.setImageBitmap(resizeBitmapImageFn(bitmap1,540));
            return byteimage;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_full, menu);
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
