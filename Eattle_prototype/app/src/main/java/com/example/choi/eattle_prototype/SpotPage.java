package com.example.choi.eattle_prototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

    /*
    //제스처를 인식하기 위한 변수들-----------------
    // 드래그시 좌표 저장
    int posX1 = 0, posX2 = 0, posY1 = 0, posY2 = 0;
    // 핀치시 두좌표간의 거리 저장
    float oldDist = 1f;
    float newDist = 1f;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;*/

    public SpotPage(final Context context, int spotNum) {
        super(context);
        init(context);
        this.spotNum = spotNum;
        //depth1에 대해 클릭 리스너를 등록한다. depth2에 대해서는 클릭 리스너를 등록하지 않는다.
        if (spotNum != -1) {
            tourSpotPicture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (v.getId() == R.id.tourSpotPicture) {
                        Log.d("MainActivity", " 클릭 성공");
                        //선택된 관광지에 대한 추가 액티비티를 띄운다.
                        createDetailedInfoActivity(context);
                    }
                }
            });
            /*
            tourSpotPicture.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    String strMsg = "";
                    Toast.makeText(getContext(), "onTouch", Toast.LENGTH_SHORT);

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:    //첫번째 손가락 터치(드래그 용도)
                            Toast.makeText(getContext(), "action_down", Toast.LENGTH_SHORT);
                            posX1 = (int) event.getX();
                            posY1 = (int) event.getY();

                            Log.d("zoom", "mode=DRAG");
                            mode = DRAG;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mode == DRAG) {  // 드래그 중
                                Toast.makeText(getContext(), "action_move DRAG", Toast.LENGTH_SHORT);

                                posX2 = (int) event.getX();
                                posY2 = (int) event.getY();

                                if (Math.abs(posX2 - posX1) > 20 || Math.abs(posY2 - posY1) > 20) {
                                    posX1 = posX2;
                                    posY1 = posY2;
                                    strMsg = "drag";
                                    Toast toast = Toast.makeText(getContext(), strMsg, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } else if (mode == ZOOM) {    // 핀치 중
                                Toast.makeText(getContext(), "action_move ZOOM", Toast.LENGTH_SHORT);

                                newDist = spacing(event);

                                Log.d("zoom", "newDist=" + newDist);
                                Log.d("zoom", "oldDist=" + oldDist);

                                if (newDist - oldDist > 40) { // zoom in
                                    oldDist = newDist;

                                    strMsg = "zoom in";//확대
                                    Toast toast = Toast.makeText(getContext(), strMsg, Toast.LENGTH_SHORT);
                                    toast.show();
                                } else if (oldDist - newDist > 40) { // zoom out
                                    oldDist = newDist;

                                    strMsg = "zoom out";//축소
                                    Toast toast = Toast.makeText(getContext(), strMsg, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:    // 첫번째 손가락을 떼었을 경우
                        case MotionEvent.ACTION_POINTER_UP:  // 두번째 손가락을 떼었을 경우
                            Toast.makeText(getContext(), "action_UP,POINTER_UP", Toast.LENGTH_SHORT);

                            mode = NONE;
                            break;
                        case MotionEvent.ACTION_POINTER_DOWN:
                            Toast.makeText(getContext(), "action_POINTER_DOWN", Toast.LENGTH_SHORT);

                            //두번째 손가락 터치(손가락 2개를 인식하였기 때문에 핀치 줌으로 판별)
                            mode = ZOOM;

                            newDist = spacing(event);
                            oldDist = spacing(event);

                            Log.d("zoom", "newDist=" + newDist);
                            Log.d("zoom", "oldDist=" + oldDist);
                            Log.d("zoom", "mode=ZOOM");
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        default:
                            break;
                    }
                    return false;
                }

            });
*/

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

    public void createDetailedInfoActivity(Context context) {
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

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
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

    public int getSpotNum() {
        return this.spotNum;
    }
}
