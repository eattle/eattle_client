package com.example.cds.eattle_prototype_2;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabToTag extends Fragment {

    public static TabToTag newInstance(long id){
        TabToTag ttt = new TabToTag();

        Bundle args = new Bundle();
        args.putLong("id", id);
        ttt.setArguments(args);

        return ttt;
    }

    public TabToTag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_tab_to_tag, container, false);

        long id = 0;

        Bundle args = getArguments();
        if(args != null){
            id = args.getLong("id");
        }

        final Button btn = (Button)root.findViewById(R.id.button);

        btn.setText(""+id);

        //태그를 클릭했을 때
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn.setText("clicked");
            }
        });


        return root;
    }


}
