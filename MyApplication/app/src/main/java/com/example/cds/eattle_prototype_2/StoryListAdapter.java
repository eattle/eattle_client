package com.example.cds.eattle_prototype_2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by CDS on 15. 3. 31..
 */
//메인화면의 스토리 리스트를 출력하기 위한 Adapter
public class StoryListAdapter extends BaseAdapter{
    private ArrayList<StoryListItem> items;//ListView에서 add하는 아이템들은 items에 담기게 된다
    private Context mContext;
    public StoryListAdapter(Context context){
        items = new ArrayList<StoryListItem>();
        mContext = context;
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
    @Override
    public long getItemId(int position){
        return position;
    }
    //아이템을 출력한다
    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        if(convertView == null){
            //story_list.xml을 가져온다
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.story_list,parent,false);

            //스토리 이름을 등록한다
            TextView storyName = (TextView)convertView.findViewById(R.id.storyName);
            String[] tempName = items.get(position).getName().split("_");
            String name = tempName[0]+"년 "+tempName[1]+"월 "+tempName[2].replace("의","일의");
            storyName.setText(name);

            //스토리 이미지를 설정한다
            ImageView storyImage = (ImageView)convertView.findViewById(R.id.storyImage);
            storyImage.setImageURI(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+ "thumbnail" +"/"+items.get(position).getImgID()+".jpg"));

            //특정 아이템에 해당하는 폴더 아이디를 가져온다
            final int folderID = items.get(position).getFolderID();
            //리스트 클릭 리스너
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AlbumLayout.class);
                    //객체배열을 ArrayList로 넘겨준다.
                    intent.putExtra("folderId", folderID);
                    mContext.startActivity(intent);
                }
            });
        }

        return convertView;
    }

    //아이템 추가시
    public void add(StoryListItem item){
        items.add(item);
    }
    //아이템 삭제시
    public void remove(int _position){
        items.remove(_position);
    }
}
