package com.eattle.phoket;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ProgressBar;

import com.dexafree.materialList.view.MaterialListView;
import com.eattle.phoket.helper.DatabaseHelper;
import com.eattle.phoket.model.Folder;

import java.util.List;

/**
 * Created by GA on 2015. 7. 15..
 */
public abstract class Section extends Fragment {
    private final static int STATE_LOADING = 0;
    private final static int STATE_RUNNING = 1;
    private final static int STATE_SELECT = 2;

    private Context mContext;
    private DatabaseHelper db;

    private MaterialListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;

    private int state;

    private void setupMaterialListView(){

    }


    public void initialize(){
        state = STATE_LOADING;
    }

    public void setLoading(){
        state = STATE_LOADING;
        mSwipeRefreshLayout.setRefreshing(true);
        mListView.clear();
    }

    public void setRunning(){
        state = STATE_RUNNING;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void setSelectMode(){
        state = STATE_SELECT;

    }
}