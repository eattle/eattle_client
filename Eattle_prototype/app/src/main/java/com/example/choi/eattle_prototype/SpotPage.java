package com.example.choi.eattle_prototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by choi  on 2015-01-13.
 */
public class SpotPage extends LinearLayout {
    Context mContext;

    TextView nameText;
    ImageView tourSpotPicture;

    int spotNum;//어떤 관광지?

    public static final int CALL_NUMBER = 1001;

    public SpotPage(final Context context, int spotNum) {
        super(context);
        init(context);
        this.spotNum = spotNum;
        //depth1에 대해 클릭 리스너를 등록한다. depth2에 대해서는 클릭 리스너를 등록하지 않는다.
        if(spotNum != -1) {
            tourSpotPicture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (v.getId() == R.id.tourSpotPicture) {
                        Log.d("MainActivity", " 클릭 성공");
                        //선택된 관광지에 대한 추가 액티비티를 띄운다.
                        createDetailedInfoActivity(context);
                    }
                }
            });
        }

    }

    public SpotPage(Context context, AttributeSet attrs, int spotNum) {
        super(context, attrs);

        init(context);
        this.spotNum = spotNum;
    }

    private void init(Context context) {
        Log.d("MainActivity", "SpotPage init함수 호출");
        mContext = context;

        // inflate XML layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_spot_page, this, true);

        tourSpotPicture = (ImageView) view.findViewById(R.id.tourSpotPicture);
        nameText = (TextView) view.findViewById(R.id.nameText);
    }

    public void createDetailedInfoActivity(Context context){
        Intent intent = new Intent(context, DetailedInfoActivity.class);

        //DB 쿼리로 변경될 부분, intent와 함께 넘겨줄 데이터를 정의하는 부분
        ArrayList<TouristSpotInfo> spot = new ArrayList<TouristSpotInfo>();

        String[] args = GLOBAL.spot[getSpotNum()].getDetailedInfo();//특정 관광지의 상세정보 ID를 얻어온다.

        for (int i = 0; i < args.length; i++) {
            String SQL = "SELECT info,picName FROM spotInfo WHERE _id = " + args[i];
            Cursor c = NearSpotService.db.rawQuery(SQL, null);
            c.moveToNext();
            String spotInfo = c.getString(0);
            String _picName = c.getString(1);
            //R.drawable을 동적으로 가져온다.
            int picName = getResources().getIdentifier(_picName, "drawable", CONSTANT.PACKAGE_NAME);
            spot.add(new TouristSpotInfo(spotInfo, picName, 1, 1));
        }

        //객체배열을 ArrayList로 넘겨준다.
        intent.putParcelableArrayListExtra("spots", spot);
        context.startActivity(intent);
    }

    //get, set
    public void setImage(int resId) {
        tourSpotPicture.setImageResource(resId);
    }

    public String getNameText() {
        return nameText.getText().toString();
    }

    public void setNameText(String nameStr) {
        nameText.setText(nameStr);
    }
    public int getSpotNum(){
        return this.spotNum;
    }
}
