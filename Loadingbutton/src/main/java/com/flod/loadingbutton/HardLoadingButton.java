package com.flod.loadingbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class HardLoadingButton extends AppCompatImageButton {
    public HardLoadingButton(Context context) {
        this(context,null);
    }

    public HardLoadingButton(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public HardLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
