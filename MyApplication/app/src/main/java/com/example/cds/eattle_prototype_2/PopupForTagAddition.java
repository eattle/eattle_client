package com.example.cds.eattle_prototype_2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.cds.eattle_prototype_2.helper.DatabaseHelper;
import com.example.cds.eattle_prototype_2.model.Media;

import java.util.List;


public class PopupForTagAddition extends Activity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_for_tag_addition);

        View popup = (View)findViewById(R.id.popup);
        db = DatabaseHelper.getInstance(getApplicationContext());

        Intent intent = getIntent();
        final DataForTagAddition data= intent.getParcelableExtra("dataForTagAddition");

        TextView notifyText = (TextView)findViewById(R.id.notifyText);
        notifyText.setText("["+data.getTagName()+"] 태그를 추가하실래요?");
        Button yesButton = (Button)findViewById(R.id.yesButton);
        //폴더에 태그를 등록
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //해당 사진이 속한 폴더의 모든 사진에 태그를 등록한다
                List<Media> mediaList = db.getAllMediaByFolder(data.getFolderId());
                for (int i = 0; i < mediaList.size(); i++) {
                    db.createTag(data.getTagName(), mediaList.get(i).getId());
                }
                finish();
            }
        });
        //취소
        Button noButton = (Button)findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다음에 다시 묻지 않는다
                //TODO
                finish();
            }
        });

    }
}
