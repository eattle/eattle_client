package com.example.cds.eattle_prototype_2;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;
import com.example.cds.eattle_prototype_2.model.Tag;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabToTag extends Fragment {
    DatabaseHelper db;
    private static int media_id;
    private static Media m;
    int a = 0;

    public static TabToTag newInstance(Media m) {
        setMedia(m);
        setMedia_id(m.getId());

        TabToTag ttt = new TabToTag();

        Bundle args = new Bundle();
        args.putInt("id", m.getId());
        ttt.setArguments(args);

        return ttt;
    }

    public TabToTag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        db = DatabaseHelper.getInstance(getActivity());

        View root = inflater.inflate(R.layout.fragment_tab_to_tag, container, false);

        Bundle args = getArguments();
        if (args != null) {
            media_id = args.getInt("id");
        }

        final LinearLayout layout = (LinearLayout) root.findViewById(R.id.tagLayout);
        List<Tag> tags = db.getAllTagsByMediaId(media_id);


        int s = tags.size();

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(4,4,4,4);

        //사용자가 직접 추가한 태그
        for (int i = 0; i < s; i++) {
            //Button tagButton = new Button(getActivity());
            Button tagButton = (Button) inflater.inflate(R.layout.tag, null);
            tagButton.setText("" + tags.get(i).getName());
            tagButton.setId(tags.get(i).getId());
            tagButton.setLayoutParams(params);
            layout.addView(tagButton);

            tagButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AlbumLayout.class);
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
            Button defaultTagButton = (Button) inflater.inflate(R.layout.default_tag, null);
            defaultTagButton.setLayoutParams(params);

            if(i==0) {
                final String tagName = Integer.toString(m.getYear()) + "년";
                defaultTagButton.setText(tagName);
                defaultTagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AlbumLayout.class);
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
                        Intent intent = new Intent(getActivity(), AlbumLayout.class);
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
                        Intent intent = new Intent(getActivity(), AlbumLayout.class);
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
        btn.setText("모멘트 추가");

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                int tag_id = db.createTag("" + inputTag.getText().toString(), media_id);

                if (tag_id != -1) {
                    Tag tag = db.getTagByTagId(tag_id);
                    Button tagButton = (Button) inflater.inflate(R.layout.tag, null);
                    tagButton.setText("" + tag.getName());
                    tagButton.setId(tag.getId());
                    tagButton.setLayoutParams(params);
                    layout.addView(tagButton);
                    tagButton.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), AlbumLayout.class);
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


        return root;
    }

    public int getMedia_id() {
        return media_id;
    }

    public static void setMedia_id(int media_id) {
        TabToTag.media_id = media_id;
    }

    public Media getMedia() {
        return m;
    }

    public static void setMedia(Media m) {
        TabToTag.m = m;
    }

}
