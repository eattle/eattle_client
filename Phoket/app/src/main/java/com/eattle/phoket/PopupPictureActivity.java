package com.eattle.phoket;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Media;


public class PopupPictureActivity extends ActionBarActivity {

    DatabaseHelper db;
//    int mediaId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_picture);

        db = DatabaseHelper.getInstance(getApplicationContext());

        Intent intent = getIntent();
        int mediaId = intent.getIntExtra("id", -1);//folderId가 될수도 있고 TagId가 될 수도 있다

        Media m = db.getMediaById(mediaId);
        ImageView picture = (ImageView)findViewById(R.id.picture);


        String thumbPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + "thumbnail" + "/" + mediaId + ".jpg";
        picture.setImageBitmap(BitmapFactory.decodeFile(thumbPath));

        setTabToTag(db.getMediaById(mediaId));

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LinearLayout actionBarLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.actionbar_tophoket, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.LEFT);


        ImageView exitIcon = (ImageView)actionBarLayout.findViewById(R.id.exitIcon);

        exitIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ImageView alarm = (ImageView)actionBarLayout.findViewById(R.id.alarmIcon);


        alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageView search = (ImageView)actionBarLayout.findViewById(R.id.searchIcon);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        actionBar.setCustomView(actionBarLayout, params);
        actionBar.setDisplayHomeAsUpEnabled(false);


    }

    void setTabToTag(Media m) {
        FragmentTransaction tr = getFragmentManager().beginTransaction();
        TagsOverAlbum ttt = TagsOverAlbum.newInstance(m,1);
        tr.replace(R.id.tagLayout, ttt, "TabToTag");
        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        tr.commit();

    }


}
