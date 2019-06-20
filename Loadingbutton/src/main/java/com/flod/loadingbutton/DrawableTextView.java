package com.flod.loadingbutton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-06-13
 * UseDes:
 * TODO 设置Drawable的大小
 */
public class DrawableTextView extends AppCompatTextView {
    protected Drawable[] mDrawables;
    private float mTextWidth;
    private float mTextHeight;

    private boolean isCenter;  //是否是居中

    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //测量文字的长度
        mTextWidth = measureTextWidth(getText().toString());
        mDrawables = getCompoundDrawablesRelative();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //宽度并不是固定的,且是居中
        //TODO 不只有center
        if (getLayoutParams().width != ViewGroup.LayoutParams.WRAP_CONTENT && getGravity() == Gravity.CENTER) {
            isCenter = true;
        } else {
            isCenter = false;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {


        //重新绘制
        if (isCenter) {
            //Horizontal adjust
            if (mDrawables[0] != null) {
                final int offset = (int) calcOffset(mDrawables[0], true);
                mDrawables[0].getBounds().offset(offset, 0);

            }

            if (mDrawables[2] != null) {
                final int offset = (int) calcOffset(mDrawables[2], true);
                mDrawables[2].getBounds().offset(-offset, 0);
            }

        }
        super.onDraw(canvas);
    }

    private float calcOffset(Drawable drawable, boolean isHorizontal) {
        if (isHorizontal) {
            return (getWidth() - (drawable.getIntrinsicWidth() + getCompoundDrawablePadding() + mTextWidth)) / 2;
        } else {
            return (getHeight() - (drawable.getIntrinsicHeight() + getCompoundDrawablePadding() + mTextHeight)) / 2;
        }
    }


    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        if (mDrawables != null) {
            mDrawables[0] = left;
            mDrawables[1] = top;
            mDrawables[2] = right;
            mDrawables[3] = bottom;
        }
    }

    @Override
    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        if (mDrawables != null) {
            mDrawables[0] = start;
            mDrawables[1] = top;
            mDrawables[2] = end;
            mDrawables[3] = bottom;
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        //测量文字的长度
        mTextWidth = measureTextWidth(text.toString());
        mTextHeight = measureTextHeight(text.toString());
    }

    private float measureTextWidth(String text) {
        return text != null ? getPaint().measureText(text) : 0;
    }

    private float measureTextHeight(String text) {
        if (text == null)
            return 0;
        Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
        return (fontMetrics.descent - fontMetrics.ascent) / 2;
    }

}
