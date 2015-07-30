package com.eattle.phoket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.view.ExEditText;


public class SearchActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LinearLayout actionBarLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.actionbar_search, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.LEFT);

        ImageView exitIcon = (ImageView)actionBarLayout.findViewById(R.id.exit_icon);

        exitIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        actionBar.setCustomView(actionBarLayout, params);
        actionBar.setDisplayHomeAsUpEnabled(false);

        final ExEditText inputTag = (ExEditText) findViewById(R.id.editText);//태그 입력 창
        inputTag.setOnBackPressListener(onBackPressListener);
        final TextView btn = (TextView) findViewById(R.id.searchButton);//태그 검색 버튼

        inputTag.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    DatabaseHelper db = DatabaseHelper.getInstance(SearchActivity.this);
                    int tag_id = db.getTagIdByTagName(inputTag.getText().toString());
                    if (tag_id == 0) {
                        Toast.makeText(getApplicationContext(), "입력하신 포켓은 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    Intent intent = new Intent(getApplicationContext(), AlbumGridActivity.class);
                    intent.putExtra("kind", CONSTANT.TAG);
                    intent.putExtra("id", tag_id);
                    startActivity(intent);
                    finish();
                    return true;

                }
                return false;
            }
        });

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper db = DatabaseHelper.getInstance(SearchActivity.this);
                int tag_id = db.getTagIdByTagName(inputTag.getText().toString());
                if (tag_id == 0) {
                    Toast.makeText(getApplicationContext(), "입력하신 포켓은 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), AlbumGridActivity.class);
                intent.putExtra("kind", CONSTANT.TAG);
                intent.putExtra("id", tag_id);
                startActivity(intent);
                finish();
            }
        });

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
        finish();
    }


}
