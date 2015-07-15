package com.eattle.phoket;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eattle.phoket.Card.manager.CardData;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;
import com.eattle.phoket.model.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelectOptionFragment extends Fragment {
    private String TAG = "SelectOptionFragment";

    private List<CardData> cards;
    private DatabaseHelper db;
    private Context context;

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
        context = getActivity();//꼭 여기서 해줘야함
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

        root.findViewById(R.id.fabLocal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<cards.size();i++) {
                    exportStoryPopupDialog(cards.get(i).getData());
                }
                removeFragment();

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



    public void exportStoryPopupDialog(final int folderID){
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        if(db == null)
            db = DatabaseHelper.getInstance(getActivity());
        String storyName = CONSTANT.convertFolderNameToStoryName(db.getFolder(folderID).getName());
        d.setMessage("스마트폰에 \n'" + storyName + "'\n폴더를 생성하시겠습니까?");

        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //makeLocalFolder(folderID);
                        Log.d(TAG, " context : "+context);
                        new exportStoryToFolder(context).execute(folderID);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;

                }
            }
        };
        d.setPositiveButton("Yes", l);
        d.setNegativeButton("No", l);
        d.show();
    }

    //스토리 내보내기를 위한 AsyncTask
    public class exportStoryToFolder extends AsyncTask<Integer, String, Integer> {

        private ProgressDialog mDlg;
        private Context mContext;

        public exportStoryToFolder(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mDlg = new ProgressDialog(mContext);
            mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDlg.setMessage("스토리를 폴더로 생성하는 중입니다..");
            mDlg.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {

            final int folderID = params[0];

            if(db == null)
                db = DatabaseHelper.getInstance(mContext);

            Folder folder = db.getFolder(folderID);
            String folderName = CONSTANT.convertFolderNameToStoryFolderName(folder.getName());
            folderName = Environment.getExternalStorageDirectory() + "/PhoKet/" + folderName;
            FolderManage.makeDirectory(folderName);

            List<Media> medias = db.getAllMediaByFolder(folder.getId());
            int totalNum = medias.size();
            publishProgress("max",Integer.toString(totalNum));//사진의 총 개수를 프로그레스 바에 설정한다

            for(int i=0;i<medias.size();i++){
                Media media = medias.get(i);
                File originalPicture = new File(media.getPath());
                String newPath = folderName+"/"+media.getName();
                FolderManage.copyFile(originalPicture,newPath);
                //기존의 사진에서 지우기
                FolderManage.deleteFile(originalPicture);
                //Media DB에 업데이트
                media.setPath(newPath);
                Log.d(TAG,"new Path : "+newPath);
                db.updateMedia(media);

                //Folder DB에 업데이트
                Log.d(TAG,"media.getId() : "+media.getId()+" folder.getTitleImageID()"+folder.getTitleImageID());
                if(media.getId() == folder.getTitleImageID()) {//폴더의 대표사진에 대해
                    folder.setImage(newPath);

                    db.updateFolder(folder);
                    //TODO 사진 경로 바뀌면 썸네일 경로도 달라지는지?
                }

                // 작업이 진행되면서 호출하며 화면의 업그레이드를 담당하게 된다
                publishProgress("progress", Integer.toString(i+1));
            }
            // 수행이 끝나고 리턴하는 값은 다음에 수행될 onProgressUpdate 의 파라미터가 된다
            return totalNum;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
                mDlg.setProgress(Integer.parseInt(progress[1]));
            } else if (progress[0].equals("max")) {
                mDlg.setMax(Integer.parseInt(progress[1]));//사진의 총 개수
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            mDlg.dismiss();
            Toast.makeText(mContext, "스토리 폴더 생성 완료", Toast.LENGTH_SHORT).show();
        }
    }
}
