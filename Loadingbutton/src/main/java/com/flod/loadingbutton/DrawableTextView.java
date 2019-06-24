package com.flod.loadingbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-06-13
 * UseDes:
 * 1、Gravity() = center 文字居中但图片没有居中 √
 * 2、设置Drawable的大小 √
 */
public class DrawableTextView extends AppCompatTextView {

    @IntDef({INDEX.START, INDEX.TOP, INDEX.END, INDEX.BOTTOM})
    @interface INDEX {
        int START = 0;
        int TOP = 1;
        int END = 2;
        int BOTTOM = 3;

        int W = 0;
        int H = 1;
    }


    protected Drawable[] mDrawables;
    private float[][] mDrawableSize = new float[4][2];

    private float mTextWidth;
    private float mTextHeight;
    private Rect mTextBounds = new Rect();

    private boolean isCenter;  //是否是居中
    private boolean enableDrawablesCenter;

    public DrawableTextView(Context context) {
        super(context);
        init(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        mDrawables = getCompoundDrawablesRelative();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        enableDrawablesCenter = array.getBoolean(R.styleable.DrawableTextView_enableDrawableCenter, true);
        if (mDrawables[INDEX.START] != null) {
            mDrawableSize[INDEX.START] = new float[]{
                    array.getDimension(R.styleable.DrawableTextView_drawableStartWidth, mDrawables[INDEX.START].getIntrinsicWidth()),
                    array.getDimension(R.styleable.DrawableTextView_drawableStartWidth, mDrawables[INDEX.START].getIntrinsicHeight())};
        }

        if (mDrawables[INDEX.TOP] != null) {
            mDrawableSize[INDEX.TOP] = new float[]{
                    array.getDimension(R.styleable.DrawableTextView_drawableTopWidth, mDrawables[INDEX.TOP].getIntrinsicWidth()),
                    array.getDimension(R.styleable.DrawableTextView_drawableTopHeight, mDrawables[INDEX.TOP].getIntrinsicHeight())};
        }

        if (mDrawables[INDEX.END] != null) {
            mDrawableSize[INDEX.END] = new float[]{
                    array.getDimension(R.styleable.DrawableTextView_drawableRightWidth, mDrawables[INDEX.END].getIntrinsicWidth()),
                    array.getDimension(R.styleable.DrawableTextView_drawableRightHeight, mDrawables[INDEX.END].getIntrinsicHeight())};
        }

        if (mDrawables[INDEX.BOTTOM] != null) {
            mDrawableSize[INDEX.BOTTOM] = new float[]{
                    array.getDimension(R.styleable.DrawableTextView_drawableBottomWidth, mDrawables[INDEX.BOTTOM].getIntrinsicWidth()),
                    array.getDimension(R.styleable.DrawableTextView_drawableBottomHeight, mDrawables[INDEX.BOTTOM].getIntrinsicHeight())};
        }

        array.recycle();

        //resetCompoundDrawables
        resetDrawablesSize();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (enableDrawablesCenter) {
            isCenter = getLayoutParams().width != ViewGroup.LayoutParams.WRAP_CONTENT
                    && ((getGravity() & Gravity.CENTER) == Gravity.CENTER
                    || (getGravity() & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL
                    || (getGravity() & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL);


            mTextWidth = measureTextWidth();
            mTextHeight = measureTextHeight();
        }
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (enableDrawablesCenter && isCenter) {
            if (mDrawables[INDEX.START] != null) {
                mDrawables[INDEX.START].getBounds().offset((int) calcOffset(INDEX.START), 0);
            }

            if (mDrawables[INDEX.TOP] != null) {
                mDrawables[INDEX.TOP].getBounds().offset(0, (int) calcOffset(INDEX.TOP));
            }

            if (mDrawables[INDEX.END] != null) {
                mDrawables[INDEX.END].getBounds().offset(-(int) calcOffset(INDEX.END), 0);
            }
            if (mDrawables[INDEX.BOTTOM] != null) {
                mDrawables[INDEX.BOTTOM].getBounds().offset(0, -(int) calcOffset(INDEX.BOTTOM));
            }
        }
        super.onDraw(canvas);
    }

    private float calcOffset(@INDEX int position) {
        int extraOffset = 0;
        switch (position) {
            case INDEX.START:
                if (mDrawables[INDEX.END] != null) {
                    extraOffset = getCompoundPaddingEnd();
                }
                return (getWidth() - (mDrawableSize[INDEX.START][INDEX.W] + mTextWidth + extraOffset)) / 2;

            case INDEX.TOP:
                if (mDrawables[INDEX.BOTTOM] != null) {
                    extraOffset = getCompoundPaddingBottom();
                }
                return (getHeight() - (mDrawableSize[INDEX.TOP][INDEX.H] + mTextHeight + extraOffset)) / 2;

            case INDEX.END:
                if (mDrawables[INDEX.START] != null) {
                    extraOffset = getCompoundPaddingStart();
                }
                return (getWidth() - (mDrawableSize[INDEX.END][INDEX.W] + mTextWidth + extraOffset)) / 2;

            case INDEX.BOTTOM:
                if (mDrawables[INDEX.TOP] != null) {
                    extraOffset = getCompoundPaddingTop();
                }
                return (getHeight() - (mDrawableSize[INDEX.BOTTOM][INDEX.H] + mTextHeight + extraOffset)) / 2;
            default:
                return 0;

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

    private void resetDrawablesSize() {
        for (int i = 0; i < mDrawables.length; i++) {
            if (mDrawables[i] != null) {
                mDrawables[i].getBounds().right =
                        (int) (mDrawables[i].getBounds().left + mDrawableSize[i][INDEX.W]);
                mDrawables[i].getBounds().bottom =
                        (int) (mDrawables[i].getBounds().top + mDrawableSize[i][INDEX.H]);
            }
        }
        setCompoundDrawablesRelative(mDrawables[0], mDrawables[1], mDrawables[2], mDrawables[3]);
    }


    private float measureTextWidth() {
        getLineBounds(0, mTextBounds);
        float width = getPaint().measureText(getText().toString());
        float maxWidth = mTextBounds.right - mTextBounds.left;
        return width <= maxWidth ? width : maxWidth;
    }

    private float measureTextHeight() {
        return getLineHeight() * getLineCount();
    }


}
