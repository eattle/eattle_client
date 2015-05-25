package com.eattle.phoket;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 8;
        Bitmap bm = BitmapFactory.decodeFile(m.getPath(), opt);
        picture.setImageBitmap(bm);

        setTabToTag(db.getMediaById(mediaId));

    }

    void setTabToTag(Media m) {
        FragmentTransaction tr = getFragmentManager().beginTransaction();
        TagsOverAlbum ttt = TagsOverAlbum.newInstance(m);
        tr.replace(R.id.tagLayout, ttt, "TabToTag");
        tr.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        tr.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_popup_picture, menu);
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
