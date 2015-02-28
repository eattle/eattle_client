package com.example.choi.eattle_prototype;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumLayoutFragment2 extends Fragment implements AlbumLayout{

    public final static String TAG = "AlbumLayout2";

    final static int MAX_IMAGE_NUM = 4;
    ImageView[] mImages = new ImageView[MAX_IMAGE_NUM];
    boolean movedOnImage;
    int mImageNum;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mImageNum = 0;

        return inflater.inflate(R.layout.fragment_album_layout2, container, false);
    }


    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mImages[0] = (ImageView)getView().findViewById(R.id.imageView6);
        mImages[1] = (ImageView)getView().findViewById(R.id.imageView7);
        mImages[2] = (ImageView)getView().findViewById(R.id.imageView8);
        mImages[3] = (ImageView)getView().findViewById(R.id.imageView9);

        for(int i = 0; i < MAX_IMAGE_NUM; i++) {
            if(AlbumMainActivity.ImageSetter.setImage(mImages[i])) mImageNum++;
            mImages[i].setOnClickListener(mOnClickListener);
        }

    }


    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.e("Click", "" + v.getId());

        }
        /*
               public boolean onTouch(View v, MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.e("touch", "ACTION_DOWN : "+event.getAction());
                movedOnImage = false;
                return true;
            }
            if(event.getAction() == MotionEvent.ACTION_MOVE && !movedOnImage) {
                Log.e("touch", "ACTION_MOVE : "+event.getAction());
                movedOnImage = true;

                return false;
            }
            if(event.getAction() == MotionEvent.ACTION_UP && !movedOnImage) {
                Log.e("touch", "ACTION_UP : "+event.getAction());

                return false;
            }


            Log.e("touch", ""+event.getAction());
            return false;
        }
         */
    };

    @Override
    public int getMaxImageNum() {
        return MAX_IMAGE_NUM;
    }

    @Override
    public int getImageNum() {
        return mImageNum;
    }

}
