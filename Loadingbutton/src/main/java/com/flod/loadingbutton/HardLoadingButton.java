package com.flod.loadingbutton;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class HardLoadingButton extends AppCompatTextView {
    private CircularProgressDrawable mProgressDrawable;


    public HardLoadingButton(Context context) {
        this(context, null);
    }

    public HardLoadingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HardLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpProgressDrawable(context);
    }

    private void setUpProgressDrawable(Context context) {
        mProgressDrawable = new CircularProgressDrawable(context);
        final int defaultSize = (int) (getTextSize() + 10);
        mProgressDrawable.setStrokeWidth(getTextSize() * 0.1f);
        mProgressDrawable.setColorSchemeColors(getTextColors().getDefaultColor());
        mProgressDrawable.setBounds(0, 0, defaultSize, defaultSize);
        setCompoundDrawables(mProgressDrawable, null, null, null);
        mProgressDrawable.start();

    }


    public CircularProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }


    public void start(boolean shrinkBtn) {

    }


    public void stop() {

    }

}
