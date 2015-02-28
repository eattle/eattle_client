package com.example.choi.eattle_prototype;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.choi.eattle_prototype.model1.Spot;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumTitleFragment extends Fragment implements AlbumLayout{

    public AlbumTitleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_album_title, container, false);

        View root = inflater.inflate(R.layout.fragment_album_title, container, false);

        final TextView spotName = (TextView)root.findViewById(R.id.spotName);
        final ImageView spotImage = (ImageView)root.findViewById(R.id.spotImage);

        Spot spot;
        int picNum;
        Bundle args = getArguments();
        if(args != null){
            spot = args.getParcelable("spot");
            picNum = args.getInt("picNum");
            spotName.setText(spot.getName());
            spotImage.setImageResource(picNum);
        }
        return root;


    }

//    @Override
//    public int getMaxImageNum() {
//        return MAX_IMAGE_NUM;
//    }

//    public int getImageNum(){
//        return imageNum;
//    }

    public static AlbumTitleFragment newInstance(Spot spot, int picNum){
        AlbumTitleFragment tf = new AlbumTitleFragment();

        Bundle args = new Bundle();
        args.putParcelable("spot", spot);
        args.putInt("picNum", picNum);
        tf.setArguments(args);

        return tf;
    }

    @Override
    public int getMaxImageNum() {
        return 0;
    }

    @Override
    public int getImageNum() {
        return 0;
    }
}


interface AlbumLayout{
    int getMaxImageNum();
    int getImageNum();
}