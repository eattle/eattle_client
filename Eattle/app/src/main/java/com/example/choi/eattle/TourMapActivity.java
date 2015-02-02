package com.example.choi.eattle;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/*
    지도 액티비티
    * 현재 나의 위치를 표시
    * 내가 이동해온 경로를 표시
    * 특정 지점에 가까이 갔을 때 푸시
 */
public class TourMapActivity extends ActionBarActivity {

    private static GoogleMap map;
    public static PolylineOptions rectOptions = new PolylineOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_map);

        // 지도 객체 참조
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        //경로 그리기


    }

    @Override
    public void onResume() {
        super.onResume();
        // 내 위치 자동 표시 enable
        //지도에 현재 나의 위치를 점으로 찍어 표시해주는 코드
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 내 위치 자동 표시 disable
        map.setMyLocationEnabled(false);
    }

    /**
     * 현재 위치의 지도를 보여주기 위해 정의한 메소드
     *
     * @param latitude
     * @param longitude
     */
    public static void showCurrentLocation(Double latitude, Double longitude) {
        // 현재 위치를 이용해 LatLon 객체 생성
        LatLng curPoint = new LatLng(latitude, longitude);

        //지도를 어느정도 확대해서 보여줄 것인지
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));

        // 지도 유형 설정. 지형도인 경우에는 GoogleMap.MAP_TYPE_TERRAIN, 위성 지도인 경우에는 GoogleMap.MAP_TYPE_SATELLITE
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        Polyline polyline = map.addPolyline(rectOptions);
        // 특정 위치에 관광지를 표시하기 위해 정의한 메소드(여기에 관광지들 등록하면 됨)
        showSpotPosition(37.4871636, 126.979744, "우리집", "벽적골 태영아파트 933동");
        showSpotPosition(37.2501539, 127.0641907, "아딸", "벽적골 태영아파트 입구");
        showSpotPosition(37.5043299, 127.0447994, "소프트웨어 마에스트로", "강남구 역삼동");

        showSpotPosition(37.5042846, 127.0414756, "성산일출봉", "강남구 역삼동");
        showSpotPosition(37.502757, 127.043621, "제주월드컵경기장", "강남구 역삼동");
        showSpotPosition(37.5009083, 127.045714, "백록담", "강남구 역삼동");
        showSpotPosition(37.5032836, 127.0446134, "돌하르방공원", "강남구 역삼동");
        showSpotPosition(37.5005613, 127.035285, "한라수목원", "강남구 역삼동");
    }

    /**
     * 관광지를 표시하기 위해 정의한 메소드
     */
    private static void showSpotPosition(Double latitude, Double longitude, String title, String snippet) {
        MarkerOptions marker = new MarkerOptions();
        //우리집
        marker.position(new LatLng(latitude, longitude));//관광지의 위치를 지정한다.
        marker.title("● 관광지명 : " + title + "\n");
        marker.snippet("● 주소 : \n " + snippet);
        marker.draggable(true);
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.spot));

        map.addMarker(marker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
