package com.eattle.phoket;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Media;


public class PopupPictureActivity extends AppCompatActivity {

    DatabaseHelper db;
//    int mediaId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CONSTANT.actList.add(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_picture);

        db = DatabaseHelper.getInstance(getApplicationContext());

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        int mediaId = intent.getIntExtra("id", -1);//folderId가 될수도 있고 TagId가 될 수도 있다

        Media m = db.getMediaById(mediaId);
        ImageView picture = (ImageView)findViewById(R.id.picture);


        Glide.with(this)
                .load(m.getPath())
                .into(picture);

        setTabToTag(db.getMediaById(mediaId));

    }

    void setTabToTag(Media m) {
        FragmentTransaction tr = getFragmentManager().beginTransaction();
        TagsOverAlbum ttt = TagsOverAlbum.newInstance(m,1);
        tr.replace(R.id.tagLayout, ttt, "TabToTag");
        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        tr.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                int actSize = CONSTANT.actList.size();
                for (int i = 0; i < actSize; i++) {
                    CONSTANT.actList.get(i).finish();
                    finish();
                }
                return true;
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
