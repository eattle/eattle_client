package com.example.cds.eattle_prototype_2;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Tag;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabToTag extends Fragment {
    DatabaseHelper db;
    int media_id;

    int a = 0;

    public static TabToTag newInstance(int id){
        TabToTag ttt = new TabToTag();

        Bundle args = new Bundle();
        args.putInt("id", id);
        ttt.setArguments(args);

        return ttt;
    }

    public TabToTag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        db = DatabaseHelper.getInstance(getActivity());

        View root = inflater.inflate(R.layout.fragment_tab_to_tag, container, false);

        Bundle args = getArguments();
        if(args != null){
            media_id = args.getInt("id");
        }

        final LinearLayout layout = (LinearLayout)root.findViewById(R.id.tagLayout);
        List<Tag> tags= db.getAllTagsByMediaId(media_id);


        int s = tags.size();

        for(int i = 0; i < s; i++){
            Button tagButton = new Button(getActivity());
            tagButton.setText(""+tags.get(i).getName());
            tagButton.setId(tags.get(i).getId());
            tagButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(tagButton);
            tagButton.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AlbumLayout.class);
                    intent.putExtra("kind", CONSTANT.TAG);
                    intent.putExtra("id", (((Button)v).getId()));
                    intent.putExtra("mediaId", media_id);
                    startActivity(intent);
                }
            });
        }


        final EditText inputTag = (EditText)root.findViewById(R.id.editText);
        final Button btn = (Button)root.findViewById(R.id.button);
        btn.setText("태그 추가");

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag_id = db.createTag(""+inputTag.getText().toString(), media_id);

                if(tag_id != -1){
                    Tag tag = db.getTagByTagId(tag_id);
                    Button tagButton = new Button(getActivity());
                    tagButton.setText(""+tag.getName());
                    tagButton.setId(tag.getId());
                    tagButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    layout.addView(tagButton);
                    tagButton.setOnClickListener(new Button.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), AlbumLayout.class);
                            intent.putExtra("kind", CONSTANT.TAG);
                            intent.putExtra("id", (((Button)v).getId()));
                            intent.putExtra("mediaId", media_id);
                            startActivity(intent);
                        }
                    });
                }
            }
        });


        return root;
    }


}
