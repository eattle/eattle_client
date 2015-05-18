package com.eattle.phoket;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eattle.phoket.device.CachedBlockDevice;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TagsOverAlbum extends Fragment {
    DatabaseHelper db;
    private static int media_id;
    private static int position;
    private static int totalPictureNum;
    private static Media m;
    int a = 0;

    //파일 시스템 관련 변수
    static FileSystem fileSystem;

    //pushTabToTag를 위해
    public static TagsOverAlbum newInstance(Media m,int position,int totalPictureNum) {
        setMedia(m);
        setMedia_id(m.getId());
        setPosition(position);
        setTotalPictureNum(totalPictureNum);

        fileSystem = FileSystem.getInstance();

        TagsOverAlbum ttt = new TagsOverAlbum();

        Bundle args = new Bundle();
        args.putInt("id", m.getId());
        args.putInt("position", position);
        args.putInt("totalPictureNum", totalPictureNum);
        ttt.setArguments(args);

        return ttt;
    }
    //setTabToTag를 위해
    public static TagsOverAlbum newInstance(Media m) {
        setMedia(m);
        setMedia_id(m.getId());

        fileSystem = FileSystem.getInstance();

        TagsOverAlbum ttt = new TagsOverAlbum();

        Bundle args = new Bundle();
        args.putInt("id", m.getId());
        ttt.setArguments(args);

        return ttt;
    }

    public TagsOverAlbum() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        db = DatabaseHelper.getInstance(getActivity());

        View root = inflater.inflate(R.layout.fragment_tags_over_album, container, false);

        Bundle args = getArguments();
        if (args != null) {
            media_id = args.getInt("id");
            position = args.getInt("position",0);
        }

        final LinearLayout layout = (LinearLayout) root.findViewById(R.id.tagLayout);
        List<Tag> tags = db.getAllTagsByMediaId(media_id);


        int s = tags.size();

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4,4,4,4);

        //사용자가 직접 추가한 태그
        for (int i = 0; i < s; i++) {
            //Button tagButton = new Button(getActivity());
            Button tagButton = (Button) inflater.inflate(R.layout.view_tag_button, null);
            tagButton.setText("" + tags.get(i).getName());
            tagButton.setId(tags.get(i).getId());
            tagButton.setLayoutParams(params);
            layout.addView(tagButton);

            tagButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                    intent.putExtra("kind", CONSTANT.TAG);
                    intent.putExtra("id", (((Button) v).getId()));
                    intent.putExtra("mediaId", media_id);
                    startActivity(intent);
                }
            });
        }
        //기본적으로 추가하는 태그(날짜 등)
        //'년','월','일' 태그
        for(int i=0;i<3;i++) {
            Button defaultTagButton = (Button) inflater.inflate(R.layout.view_default_tag_button, null);
            defaultTagButton.setLayoutParams(params);

            if(i==0) {
                final String tagName = Integer.toString(m.getYear()) + "년";
                defaultTagButton.setText(tagName);
                defaultTagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                        intent.putExtra("kind", CONSTANT.DEFAULT_TAG);
                        //intent.putExtra("id", -1);
                        intent.putExtra("tagName",tagName);//기본 태그에서는 tagName을 넘겨준다
                        intent.putExtra("mediaId", m.getId());
                        startActivity(intent);
                    }
                });
            }
            else if(i==1) {
                final String tagName = Integer.toString(m.getMonth()) + "월";
                defaultTagButton.setText(tagName);
                defaultTagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                        intent.putExtra("kind", CONSTANT.DEFAULT_TAG);
                        //intent.putExtra("id", -1);
                        intent.putExtra("tagName",tagName);//기본 태그에서는 tagName을 넘겨준다
                        intent.putExtra("mediaId", m.getId());
                        startActivity(intent);
                    }
                });
            }
            else if(i==2) {
                final String tagName = Integer.toString(m.getDay()) + "일";
                defaultTagButton.setText(tagName);
                defaultTagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                        intent.putExtra("kind", CONSTANT.DEFAULT_TAG);
                        //intent.putExtra("id", -1);
                        intent.putExtra("tagName",tagName);//기본 태그에서는 tagName을 넘겨준다
                        intent.putExtra("mediaId", m.getId());
                        startActivity(intent);
                    }
                });
            }
            layout.addView(defaultTagButton);
        }


        final EditText inputTag = (EditText) root.findViewById(R.id.editText);
        final Button btn = (Button) root.findViewById(R.id.button);
        //btn.setText("추가");

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                int tag_id = db.createTag("" + inputTag.getText().toString(), media_id);

                if (tag_id != -1) {
                    Tag tag = db.getTagByTagId(tag_id);
                    Button tagButton = (Button) inflater.inflate(R.layout.view_tag_button, null);
                    tagButton.setText("" + tag.getName());
                    tagButton.setId(tag.getId());
                    tagButton.setLayoutParams(params);
                    layout.addView(tagButton);
                    tagButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                            intent.putExtra("kind", CONSTANT.TAG);
                            intent.putExtra("id", (((Button) v).getId()));
                            intent.putExtra("mediaId", media_id);
                            startActivity(intent);
                        }
                    });
                }
                inputTag.clearFocus();
                inputTag.setText(null);
            }
        });



        //스토리의 몇번째 사진인지
        TextView storyContentOrderText = (TextView)root.findViewById(R.id.storyContentOrderText);
        storyContentOrderText.setText((position+1)+"/"+totalPictureNum);

        //휴지통(사진 삭제)
        ImageView storyContentDelete = (ImageView)root.findViewById(R.id.storyContentDelete);
        storyContentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*//TODO
                //해당 사진을 삭제한다
                //로컬(USB)에서 삭제
                if(CONSTANT.ISUSBCONNECTED == 0) {//USB에 연결되지 않았을 때
                    Log.d("asdfasdf", "USB 낫 연결 "+m.getPath());
                    File tempFile = new File(m.getPath());
                    FolderManage.deleteFile(tempFile);
                }
                else if(CONSTANT.ISUSBCONNECTED == 1){//USB에 연결되었을 때
                    Log.d("asdfasdf", "USB  연결");
                    fileSystem.delete(m.getId()+".jpg",CONSTANT.BLOCKDEVICE);
                }
                //DB에서 해당 사진 삭제
                db.deleteMedia(m.getId());
                Log.d("asdfasdf", "DB 삭제 완료");

                //해당 사진이 지워짐으로서 폴더에 사진이 하나도 안남게 되었을 때, 폴더(스토리) 자체를 지운다
                int folderId = m.getFolder_id();
                if(db.getAllMediaByFolder(folderId).size() == 0)
                    db.deleteFolder(folderId, true);
                Log.d("asdfasdf", "폴더 삭제 완료");
                Folder folder = db.getFolder(folderId);

                //TODO 뷰를 새로 그린다.
*/
            }
        });
        return root;
    }

    public int getMedia_id() {
        return media_id;
    }

    public static void setMedia_id(int media_id) {
        TagsOverAlbum.media_id = media_id;
    }

    public Media getMedia() {
        return m;
    }

    public static void setMedia(Media m) {
        TagsOverAlbum.m = m;
    }
    public static void setPosition(int position){
        TagsOverAlbum.position = position;
    }
    public static int getPosition(){
        return TagsOverAlbum.position;
    }
    public static void setTotalPictureNum(int totalPictureNum){
        TagsOverAlbum.totalPictureNum = totalPictureNum;
    }
    public static int getTotalPictureNum(){
        return TagsOverAlbum.totalPictureNum;
    }
}
