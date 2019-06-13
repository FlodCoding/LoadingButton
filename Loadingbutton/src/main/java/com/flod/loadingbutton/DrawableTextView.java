package com.flod.loadingbutton;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-06-13
 * UseDes:
 */
public class DrawableTextView extends AppCompatTextView {
    private Drawable[] drawables = new Drawable[4];
    private int[][] drawableSize = new int[4][4];



    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }
}
