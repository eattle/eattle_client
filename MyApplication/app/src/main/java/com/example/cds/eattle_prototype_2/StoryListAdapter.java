package com.example.cds.eattle_prototype_2;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cds.eattle_prototype_2.device.BlockDevice;
import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by CDS on 15. 3. 31..
 */

//메인화면의 스토리 리스트를 출력하기 위한 Adapter
public class StoryListAdapter extends BaseAdapter{
    ArrayList<StoryListItem> items;//ListView에서 add하는 아이템들은 items에 담기게 된다
    Context mContext;
    LayoutInflater Inflare;
    int defaultLayout;//기본 레이아웃
    int secondLayout;//스토리에 속한 사진의 개수가 CONSTANT.BOUNDARY보다 적을때 사용할 레이아웃
    DatabaseHelper db;

    FileSystem fileSystem;
    Bitmap tempFromUSB = null;

    public StoryListAdapter(Context context){
        mContext = context;
        Inflare = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = new ArrayList<StoryListItem>();
        defaultLayout = R.layout.story_list;
        secondLayout = R.layout.story_list_daily;//스토리에 속한 사진의 개수가 CONSTANT.BOUNDARY보다 적을때 사용할 레이아웃
        //데이터베이스 OPEN
        db = DatabaseHelper.getInstance(mContext);
        fileSystem = FileSystem.getInstance();
    }

    @Override
    //현재 리스트뷰에 등록된 아이템의 개수를 반환
    public int getCount(){
        return items.size();
    }

    @Override
    //현재 아이템의 오브젝트를 반환
    public Object getItem(int position){
        return items.get(position);
    }

    public ArrayList<StoryListItem> getAllItems(){
        return items;
    }
    @Override
    public long getItemId(int position){
        return position;
    }

    //아이템을 출력한다
    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        final int pos = position;
        if(items.get(position).getFolderID() != -1) { // USB에서 온 사진이 아닐떄
            int pictureNumInStory = items.get(position).getPictureNumInStory();


            if (convertView == null)
                convertView = Inflare.inflate(defaultLayout, parent, false);
            TextView storyName = (TextView) convertView.findViewById(R.id.storyName);
            String temp = items.get(position).getName();
            String name = "";
            if (temp.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
                String[] bigSplit = temp.split("~");
                String[] tempName = bigSplit[0].split("_");
                name += tempName[0] + "년 " + tempName[1] + "월 " + tempName[2] + "일 ~ ";
                tempName = bigSplit[1].split("_");
                name += tempName[1] + "월 " + tempName[2].replace("의", "일의");
            } else {//단일 날짜의 스토리일 경우
                String[] tempName = temp.split("_");
                name = tempName[0] + "년 " + tempName[1] + "월 " + tempName[2].replace("의", "일의");
            }
            if (pictureNumInStory <= CONSTANT.BOUNDARY)
                name = name.replace("스토리", "일상");
            storyName.setText(name);

            //스토리 이미지를 설정한다
            ImageView storyImage = (ImageView) convertView.findViewById(R.id.storyImage);
            storyImage.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + items.get(position).getImgID() + ".jpg"));

            //특정 아이템에 해당하는 폴더 아이디를 가져온다
//          final int folderID = items.get(position).getFolderID();
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AlbumLayout.class);
                    intent.putExtra("kind", CONSTANT.FOLDER);
                    intent.putExtra("id", items.get(pos).getFolderID());
                    mContext.startActivity(intent);
                }
            });
        }
        else{//USB에서 읽어올 때
            //fileSystem.incaseSearchTable(items.get(position).getBlockDevice());//탐색테이블 만듬 초기화

            if (convertView == null)
                convertView = Inflare.inflate(defaultLayout, parent, false);
            //    private Bitmap fileoutimage(String outString, BlockDevice blockDevice){//USB->스마트폰 내보내기
            //스토리 이미지를 설정한다
            ImageView storyImage = (ImageView) convertView.findViewById(R.id.storyImage);
            //storyImage.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + items.get(position).getImgID() + ".jpg"));
            if(tempFromUSB == null){
                tempFromUSB = fileoutimage("India.png",items.get(position).getBlockDevice());
            }
            storyImage.setImageBitmap(tempFromUSB);

            TextView storyName = (TextView) convertView.findViewById(R.id.storyName);
            storyName.setText("USB에서 온 사진");
            final BlockDevice tempBlockDevice = items.get(position).getBlockDevice();
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AlbumLayout.class);
                    intent.putExtra("kind", -1);
                    intent.putExtra("id", -1);
                    mContext.startActivity(intent);
                }
            });
        }




        return convertView;

        /*
        if(pictureNumInStory <= CONSTANT.BOUNDARY){//스토리에 속한 사진의 개수가 CONSTANT.BOUNDARY보다 적을때 사용할 레이아웃
            //if(convertView == null)
                convertView = Inflare.inflate(secondLayout, parent, false);
            //해당 스토리에 속한 사진들을 가져온다
            List<Media> medias = db.getAllMediaByFolder(items.get(position).getFolderID());
            for(int i=0;i<medias.size();i++) {
                ImageView tempImage = null;
                if(i==0)
                    tempImage = (ImageView)convertView.findViewById(R.id.one);
                else if(i==1)
                    tempImage = (ImageView)convertView.findViewById(R.id.two);
                Log.d("asdf",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ "thumbnail" +"/"+medias.get(i).getName()+".jpg!~!");
                tempImage.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ "thumbnail" +"/"+medias.get(i).getName()+".jpg"));

            }

        }
        else if(pictureNumInStory > CONSTANT.BOUNDARY) {//default
            //if(convertView == null)
                convertView = Inflare.inflate(defaultLayout, parent, false);
            TextView storyName = (TextView)convertView.findViewById(R.id.storyName);
            String temp = items.get(position).getName();
            String name = "";
            if(temp.contains("~")) {//여러 날짜를 포함하는 스토리일 경우
                String[] bigSplit = temp.split("~");
                String[] tempName = bigSplit[0].split("_");
                name += tempName[0] + "년 " + tempName[1] + "월 " + tempName[2] + "일 ~ ";
                tempName = bigSplit[1].split("_");
                name += tempName[1] + "월 " + tempName[2].replace("의", "일의");
            }
            else {//단일 날짜의 스토리일 경우
                String[] tempName = temp.split("_");
                name = tempName[0] + "년 " + tempName[1] + "월 " + tempName[2].replace("의", "일의");
            }
            storyName.setText(name);

            //스토리 이미지를 설정한다
            ImageView storyImage = (ImageView)convertView.findViewById(R.id.storyImage);
            storyImage.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ "thumbnail" +"/"+items.get(position).getImgID()+".jpg"));

            //특정 아이템에 해당하는 폴더 아이디를 가져온다
//          final int folderID = items.get(position).getFolderID();
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AlbumLayout.class);
                    intent.putExtra("kind", CONSTANT.FOLDER);
                    intent.putExtra("id", items.get(pos).getFolderID());
                    mContext.startActivity(intent);
                }
            });
        }*/
    }

    //아이템 추가시
    public void add(StoryListItem item){
        items.add(item);
    }
    //아이템 삭제시
    public void remove(int _position){
        items.remove(_position);
    }

    //아이템 삭제시
    public void clear(){
        items.clear();
    }


    private Bitmap fileoutimage(String outString, BlockDevice blockDevice){//내보내기
        //D  S   X
        //1220879 1870864 2133464

        int result[] = fileSystem.stringSearch(outString);
        byte[] dummyBuffer = new byte[(int) fileSystem.CLUSTERSPACESIZE];
        //1866136
        //result[0] = 4096;
        //result[0] = 6505;
        Log.d("xxxxxx","result[0] " + result[0]);
        if(result[0] == -1) {
            Log.d("AlbumImageSetter","값이 잘못 들어왔습니다");
            return null;
        }
        else{

            byte resultbyte[] = new byte[result[4]];
            //int resultstringaddress = 6085;
            int resultstringaddress = result[0];
            //int resultaddress = readIntToBinary(result[0],result[1]+80,LOCATIONSIZE);

            int limit =0;
            int bytecnt =0;


            blockDevice.readBlock(resultstringaddress, dummyBuffer);

            while(resultstringaddress != 0){

                int originalbyteAddress =  fileSystem.readIntToBinary(resultstringaddress, limit, fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);

                blockDevice.readBlock(originalbyteAddress, fileSystem.buffer);
                for(int i=0; i<fileSystem.CLUSTERSPACESIZE; i++){
                    if(bytecnt < result[4]) {
                        resultbyte[bytecnt++] = fileSystem.buffer[i];
                    }
                    else
                        break;
                }
                if(bytecnt >= result[4])
                    break;

                limit += fileSystem.LOCATIONSIZE;

                if(limit >= fileSystem.SPACELOCATION){
                    resultstringaddress =  fileSystem.readIntToBinary(resultstringaddress, fileSystem.NEXTLOCATION, fileSystem.LOCATIONSIZE, dummyBuffer, blockDevice);
                    blockDevice.readBlock(resultstringaddress, dummyBuffer);
                    limit =0;
                }

            }


            Log.d("xxxxxx","xxxxxxxxxxxx " + resultbyte);
            Log.d("xxxxxx","xxxxxxxxxxxxxxxxxxx " + resultbyte.length);

            /*
            Toast.makeText(this, "1 " + resultbyte, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "1 " + resultbyte.length, Toast.LENGTH_SHORT).show();*/

            Bitmap byteimage = BitmapFactory.decodeByteArray( resultbyte , 0 , resultbyte.length);
            //imageView.setImageBitmap(byteimage);

            //imageView.setImageBitmap(resizeBitmapImageFn(byteimage,540));

            //Bitmap bitmap1 = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera/1.jpg");
            //imageView.setImageBitmap(resizeBitmapImageFn(bitmap1,540));
            return byteimage;
        }

    }


}
