package com.example.choi.eattle_prototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    public SpotPage(Context context,int spotNum) {
        super(context);
        init(context);
        this.spotNum = spotNum;
    }

    public SpotPage(Context context, AttributeSet attrs,int spotNum) {
        super(context, attrs);

        init(context);
        this.spotNum = spotNum;
    }

    private void init(final Context context) {
        Log.d("MainActivity", "SpotPage init함수 호출");
        mContext = context;

        // inflate XML layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_spot_page, this, true);

        tourSpotPicture = (ImageView) view.findViewById(R.id.tourSpotPicture);
        nameText = (TextView) view.findViewById(R.id.nameText);
        tourSpotPicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.getId() == R.id.tourSpotPicture) {
                    Log.d("MainActivity", " 클릭 성공");
                    //선택된 관광지에 대한 추가 액티비티를 띄운다.
                    Intent intent = new Intent(context, DetailedInfoActivity.class);

                    //DB 쿼리로 변경될 부분, intent와 함께 넘겨줄 데이터를 정의하는 부분
                    ArrayList<TouristSpotInfo> spot = new ArrayList<TouristSpotInfo>();
                    if(spotNum == 0){
                        //names = new String[]{"상세보기 1", "상세보기 2", "상세보기 3", "상세보기 4", "상세보기 5"};
                        //resIds = new int[]{R.drawable.detailed1_1, R.drawable.detailed1_2, R.drawable.detailed1_3, R.drawable.detailed1_4, R.drawable.detailed1_5};
                        spot.add(new TouristSpotInfo("상세보기1",R.drawable.detailed1_1,1,1));
                        spot.add(new TouristSpotInfo("상세보기2",R.drawable.detailed1_2,1,1));
                        spot.add(new TouristSpotInfo("상세보기3",R.drawable.detailed1_3,1,1));
                        spot.add(new TouristSpotInfo("상세보기4",R.drawable.detailed1_4,1,1));
                        spot.add(new TouristSpotInfo("상세보기5",R.drawable.detailed1_5,1,1));
                    }
                    else if(spotNum == 1){
                        spot.add(new TouristSpotInfo("상세보기1",R.drawable.detailed2_1,1,1));
                        spot.add(new TouristSpotInfo("상세보기2",R.drawable.detailed2_2,1,1));
                        spot.add(new TouristSpotInfo("상세보기3",R.drawable.detailed2_3,1,1));
                        spot.add(new TouristSpotInfo("상세보기4",R.drawable.detailed2_4,1,1));
                        spot.add(new TouristSpotInfo("상세보기5",R.drawable.detailed2_5,1,1));
                    }
                    else if(spotNum == 2){
                        spot.add(new TouristSpotInfo("상세보기1",R.drawable.detailed3_1,1,1));
                        spot.add(new TouristSpotInfo("상세보기2",R.drawable.detailed3_2,1,1));
                        spot.add(new TouristSpotInfo("상세보기3",R.drawable.detailed3_3,1,1));
                        spot.add(new TouristSpotInfo("상세보기4",R.drawable.detailed3_4,1,1));
                        spot.add(new TouristSpotInfo("상세보기5",R.drawable.detailed3_5,1,1));
                    }
                    else if(spotNum == 3){
                        spot.add(new TouristSpotInfo("상세보기1",R.drawable.detailed4_1,1,1));
                        spot.add(new TouristSpotInfo("상세보기2",R.drawable.detailed4_2,1,1));
                        spot.add(new TouristSpotInfo("상세보기3",R.drawable.detailed4_3,1,1));
                        spot.add(new TouristSpotInfo("상세보기4",R.drawable.detailed4_4,1,1));
                        spot.add(new TouristSpotInfo("상세보기5",R.drawable.detailed4_5,1,1));
                    }
                    else if(spotNum == 4){
                        spot.add(new TouristSpotInfo("상세보기1",R.drawable.detailed5_1,1,1));
                        spot.add(new TouristSpotInfo("상세보기2",R.drawable.detailed5_2,1,1));
                        spot.add(new TouristSpotInfo("상세보기3",R.drawable.detailed5_3,1,1));
                        spot.add(new TouristSpotInfo("상세보기4",R.drawable.detailed5_4,1,1));
                        spot.add(new TouristSpotInfo("상세보기5",R.drawable.detailed5_5,1,1));
                    }
                    //객체배열을 ArrayList로 넘겨준다.
                    intent.putParcelableArrayListExtra("spots",spot);
                    context.startActivity(intent);
                }
            }
        });

    }

    public void setImage(int resId) {
        tourSpotPicture.setImageResource(resId);
    }

    public String getNameText() {
        return nameText.getText().toString();
    }

    public void setNameText(String nameStr) {
        nameText.setText(nameStr);
    }
}
