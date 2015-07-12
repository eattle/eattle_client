package com.eattle.phoket.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by GA_SOMA on 15. 6. 5..
 */
public class ExEditText extends EditText{

    private OnBackPressListener _listener;


    public ExEditText(Context context)
    {
        super(context);
    }


    public ExEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }


    public ExEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && _listener != null)
        {
            _listener.onBackPress();
        }

        return super.onKeyPreIme(keyCode, event);
    }


    public void setOnBackPressListener(OnBackPressListener $listener)
    {
        _listener = $listener;
    }

    public interface OnBackPressListener
    {
        public void onBackPress();
    }
}
