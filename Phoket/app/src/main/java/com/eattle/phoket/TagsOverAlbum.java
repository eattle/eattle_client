package com.eattle.phoket;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;
import com.eattle.phoket.model.Tag;
import com.eattle.phoket.view.ExEditText;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TagsOverAlbum extends Fragment {
    private String TAG = "TagsOverAlbum";

    DatabaseHelper db;
    private static int media_id;
    private static int position;
    private static int totalPictureNum;
    private static Media m;
    private static int type;

    int a = 0;

    //파일 시스템 관련 변수
    static FileSystem fileSystem;


    //pushTabToTag를 위해
    public static TagsOverAlbum newInstance(Media m, int position, int totalPictureNum) {
        setMedia(m);
        setMedia_id(m.getId());
        setPosition(position);
        setTotalPictureNum(totalPictureNum);
        setType(0);

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
        setType(0);

        fileSystem = FileSystem.getInstance();

        TagsOverAlbum ttt = new TagsOverAlbum();

        Bundle args = new Bundle();
        args.putInt("id", m.getId());
        ttt.setArguments(args);

        return ttt;
    }

    //setTabToTag를 위해
    public static TagsOverAlbum newInstance(Media m, int type) {
        setMedia(m);
        setMedia_id(m.getId());
        setType(type);

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
            position = args.getInt("position", 0);
        }

        final LinearLayout layout = (LinearLayout) root.findViewById(R.id.tagLayout);
        List<Tag> tags = db.getAllTagsByMediaId(media_id);


        if(type == 1){
            ExEditText inputTag = (ExEditText)root.findViewById(R.id.editText);
            inputTag.setOnBackPressListener(onBackPressListener);
        }

        int s = tags.size();

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);//태그들 간에 margin을 지정하는 부분

        //사용자가 직접 추가한 태그
        for (int i = 0; i < s; i++) {
            //Button tagButton = new Button(getActivity());
            FrameLayout tagButton = (FrameLayout) inflater.inflate(R.layout.view_tag_button, null);
            ((TextView)tagButton.findViewById(R.id.tagName)).setText("" + tags.get(i).getName());
            layout.addView(tagButton);
            final int id = tags.get(i).getId();

            tagButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
                    Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                    intent.putExtra("kind", CONSTANT.TAG);
                    intent.putExtra("id", id);
                    intent.putExtra("mediaId", media_id);
                    startActivity(intent);

                }
            });
        }
        //기본적으로 추가하는 태그(날짜 등)
        //'년','월','일' 태그
        for (int i = 0; i < 3; i++) {
            FrameLayout defaultTagButton = (FrameLayout) inflater.inflate(R.layout.view_default_tag_button, null);

            String tagName="";
            if (i == 0)
                tagName = Integer.toString(m.getYear()) + "년";
            else if (i == 1)
                tagName = Integer.toString(m.getMonth()) + "월";
            else if (i == 2)
                tagName = Integer.toString(m.getDay()) + "일";

            ((TextView)defaultTagButton.findViewById(R.id.tagName)).setText("" + tagName);
            final String tagName_ = tagName;
            defaultTagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
                    Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                    intent.putExtra("kind", CONSTANT.DEFAULT_TAG);
                    intent.putExtra("tagName", tagName_);//기본 태그에서는 tagName을 넘겨준다
                    intent.putExtra("mediaId", m.getId());
                    startActivity(intent);
                }
            });
            layout.addView(defaultTagButton);
        }


        final EditText inputTag = (EditText) root.findViewById(R.id.editText);//태그 입력 창
        final TextView btn = (TextView) root.findViewById(R.id.button);//태그 추가 버튼

        inputTag.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    String input = inputTag.getText().toString().replaceAll("\\p{Space}", "");
                    if(input == ""){
                        Toast.makeText(getActivity(),"포켓을 입력해주세요",Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    int tag_id = db.createTag("" + input, media_id, db.getMediaById(media_id).getFolder_id());
                    if (tag_id != -1) {
                        Tag tag = db.getTagByTagId(tag_id);
                        FrameLayout tagButton = (FrameLayout) inflater.inflate(R.layout.view_tag_button, null);
                        ((TextView) tagButton.findViewById(R.id.tagName)).setText("" + tag.getName());
                        layout.addView(tagButton);
                        final int id = tag.getId();

                        tagButton.setOnClickListener(new Button.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
                                Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                                intent.putExtra("kind", CONSTANT.TAG);
                                intent.putExtra("id", id);
                                intent.putExtra("mediaId", media_id);
                                startActivity(intent);
                            }
                        });
                    }
                    inputTag.clearFocus();
                    inputTag.setText(null);
                    return true;

                }
                return false;


            }

        });
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputTag.getText().toString().replaceAll("\\p{Space}", "");
                if(input == ""){
                    Toast.makeText(getActivity(),"포켓을 입력해주세요",Toast.LENGTH_SHORT).show();
                    return;
                }

                int tag_id = db.createTag("" + input, media_id, db.getMediaById(media_id).getFolder_id());

                if (tag_id != -1) {

                    Tag tag = db.getTagByTagId(tag_id);

                    FrameLayout tagButton = (FrameLayout) inflater.inflate(R.layout.view_tag_button, null);
                    ((TextView) tagButton.findViewById(R.id.tagName)).setText("" + tag.getName());
                    layout.addView(tagButton);
                    final int id = tag.getId();

                    tagButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().finish();//현재 띄워져 있던 albumFullActivity 종료(메모리 확보를 위해)
                            Intent intent = new Intent(getActivity(), AlbumGridActivity.class);
                            intent.putExtra("kind", CONSTANT.TAG);
                            intent.putExtra("id", id);
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
        TextView storyContentOrderText = (TextView) root.findViewById(R.id.storyContentOrderText);
        storyContentOrderText.setText((position + 1) + "/" + totalPictureNum);

        //휴지통(사진 삭제)
        ImageView storyContentDelete = (ImageView) root.findViewById(R.id.storyContentDelete);
        int folderID = db.getMediaById(media_id).getFolder_id();
        if( ((db.getAllMediaByFolder(folderID)).size()) <= CONSTANT.BOUNDARY && (db.getFolder(folderID).getIsFixed() == 0))//일상이면서, 고정스토리가 아닐경우
            storyContentDelete.setVisibility(View.GONE);//휴지통이 보이지 않는다
        else//일상이 아니거나 고정스토리일 경우
            storyContentDelete.setVisibility(View.VISIBLE);//휴지통이 보인다

        storyContentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wantPictureDeleted();
            }
        });

        if(type == 1){//'이 사진을 포켓에 담아주세요'에서 들어왔을 경우
            RelativeLayout storyContentOrder = (RelativeLayout)root.findViewById(R.id.storyContentOrder);
            storyContentOrder.setVisibility(View.GONE);
        }
        return root;
    }

    public void wantPictureDeleted() {//사진을 삭제할지
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        d.setTitle("사진을 스토리에서 제외하시겠습니까? ");
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deletePicture();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        d.setPositiveButton("Yes", l);
        d.setNegativeButton("No", l);
        d.show();
    }
    public void deletePicture() {
        //데이터베이스 OPEN
        if(db == null)
            db = DatabaseHelper.getInstance(getActivity());

        int folderId = m.getFolder_id();
        List<Media> allMediaByFolder = db.getAllMediaByFolder(folderId);
        Folder folder = db.getFolder(folderId);
        int totalPictureNum = allMediaByFolder.size();

        List<Tag> tags = db.getAllTagsByMediaId(m.getId());
        for(int i = 0; i < tags.size();i++){
            db.deleteFolderTag(tags.get(i).getId(), folderId);
        }

        //해당 사진의 folderID를 변경한다.
        m.setFolder_id(-1);//휴지통의 folderID는 -1(유령폴더)
        db.updateMedia(m);//DB에 업데이트

        //사진에 '휴지통'태그를 등록한다.
        db.createTag("휴지통", m.getId());


        //해당 사진이 지워짐으로서 폴더에 사진이 하나도 안남게 되었을 때
        if ((totalPictureNum-1) == 0) {
            db.deleteFolder(folderId, true);//폴더(스토리) 자체를 지운다
            this.getActivity().finish();//뷰페이저(AlbumFullActivity) 종료
            return;
        }


        //TODO 삭제하려는 사진이 folder의 대표 사진인지 확인한다 -> 대표사진을 지울 경우에는 다른 사진을 대표로 대체
        if(media_id == AlbumFullActivity.titleImageId) {
            for (int i = 0; i < allMediaByFolder.size(); i++) {
                if (allMediaByFolder.get(i).getId() == media_id) {
                    //TODO 일단 그 다음 혹은 이전 사진을 대표사진으로 변경한다 -> 대표 사진 선정 방식 고민
                    if (i != allMediaByFolder.size() - 1) {
                        String path = allMediaByFolder.get(i + 1).getPath();
                        folder.setImage(path);
                        AlbumFullActivity.titleImagePath = path;

                        int newId = allMediaByFolder.get(i + 1).getId();
                        folder.setTitleImageID(newId);
                        folder.setThumbNail_path(allMediaByFolder.get(i + 1).getThumbnail_path());
                        AlbumFullActivity.titleImageId = newId;
                    } else {
                        String path = allMediaByFolder.get(i - 1).getPath();

                        folder.setImage(path);
                        AlbumFullActivity.titleImagePath = path;

                        int newId = allMediaByFolder.get(i - 1).getId();
                        folder.setTitleImageID(newId);
                        folder.setThumbNail_path(allMediaByFolder.get(i - 1).getThumbnail_path());
                        AlbumFullActivity.titleImageId = newId;
                    }
                }
            }
        }
        folder.setPicture_num(--totalPictureNum);//폴더에 속한 사진의 개수를 감소시킨다
        db.updateFolder(folder);//DB에 업데이트

        Log.d(TAG, "폴더 삭제 완료");

        Toast.makeText(getActivity(),"사진이 스토리에서 제외되었습니다",Toast.LENGTH_SHORT).show();

        Log.d(TAG,"AlbumFullActivity.touchImageAdapter == null?" + (AlbumFullActivity.touchImageAdapter==null));
        AlbumFullActivity.touchImageAdapter.removeView(position);//뷰페이저 업데이트
    }


    private ExEditText.OnBackPressListener onBackPressListener = new ExEditText.OnBackPressListener()
    {
        @Override
        public void onBackPress()
        {
            didBackPressOnEditText();
        }
    };

    private void didBackPressOnEditText()
    {
        getActivity().finish();
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

    public static void setPosition(int position) {
        TagsOverAlbum.position = position;
    }

    public static int getPosition() {
        return TagsOverAlbum.position;
    }

    public static void setTotalPictureNum(int totalPictureNum) {
        TagsOverAlbum.totalPictureNum = totalPictureNum;
    }

    public static int getTotalPictureNum() {
        return TagsOverAlbum.totalPictureNum;
    }

    public static int getType() {
        return type;
    }

    public static void setType(int type) {
        TagsOverAlbum.type = type;
    }
}
