package com.eattle.phoket;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Media;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelectOptionFragment extends Fragment {

    private List<CardData> cards;
    private DatabaseHelper db;


    // TODO: Rename and change types and number of parameters
    public static SelectOptionFragment newInstance(List<CardData> cards) {
        SelectOptionFragment fragment = new SelectOptionFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("cards", new ArrayList<Parcelable>(cards));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cards = getArguments().getParcelableArrayList("cards");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_select_option_frgment, container, false);

        db = DatabaseHelper.getInstance(getActivity());

        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    removeFragment();
                }
                return true;
            }
        });

        root.findViewById(R.id.fabExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFragment();
            }
        });

        root.findViewById(R.id.fabShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Uri> imageUris = new ArrayList<Uri>();
                int cs = cards.size();
                for(int i = 0; i< cs; i++) {
                    List<Media> medias = db.getAllMediaByFolder(cards.get(i).getData());
                    if (cards.get(i).getType() == CONSTANT.FOLDER) {
                        int ms = medias.size();
                        for (int j = 0; j < ms; j++) {
                            imageUris.add(Uri.parse("file://"+medias.get(j).getPath()));
                        }
                    } else {
                        imageUris.add(Uri.parse("file://"+medias.get(cards.get(i).getId()).getPath()));
                    }
                }
                if(imageUris.size() == 1) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris.get(0));
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "공유하기"));
                }else {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "Share images to.."));
                }
            }
        });

        return root;

    }

    void removeFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();

    }

}
