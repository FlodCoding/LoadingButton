package com.flod.loadingbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * SimpleDes:
 * Creator: Flood
 * Date: 2019-06-13
 * UseDes:
 * 1、Gravity() = center 文字居中但图片没有居中 √
 * 2、设置Drawable的大小 √
 * 3、文字居中，图片靠边居中 √
 * 4、多次setCompoundDrawablesRelative,图片会发生偏移
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class DrawableTextView extends AppCompatTextView {

    @IntDef({POSITION.START, POSITION.TOP, POSITION.END, POSITION.BOTTOM})
    @interface POSITION {
        int START = 0;
        int TOP = 1;
        int END = 2;
        int BOTTOM = 3;
    }

    private Drawable[] mDrawables;
    private Rect[] mDrawablesBounds = new Rect[4];
    //private Boolean[] isDrawablesOffset = new Boolean[]{false, false, false, false};

    private float mTextWidth;
    private float mTextHeight;
    private Rect mTextBounds = new Rect();

    private boolean isCenter;                   //Gravity是否是居中
    private boolean enableCenterDrawables;      //drawable跟随文本居中
    private boolean enableTextInCenter;         //默认情况下文字与图片共同居中，开启后文字在最中间，图片紧挨

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
        enableCenterDrawables = array.getBoolean(R.styleable.DrawableTextView_enableCenterDrawables, true);
        enableTextInCenter = array.getBoolean(R.styleable.DrawableTextView_enableTextInCenter, false);
        if (mDrawables[POSITION.START] != null) {
            Rect startBounds = mDrawables[POSITION.START].getBounds();
            startBounds.right = (int) array.getDimension(R.styleable.DrawableTextView_drawableStartWidth, mDrawables[POSITION.START].getIntrinsicWidth());
            startBounds.bottom = (int) array.getDimension(R.styleable.DrawableTextView_drawableStartHeight, mDrawables[POSITION.START].getIntrinsicHeight());
        }

        if (mDrawables[POSITION.TOP] != null) {
            Rect topBounds = mDrawables[POSITION.TOP].getBounds();
            topBounds.right = (int) array.getDimension(R.styleable.DrawableTextView_drawableTopWidth, mDrawables[POSITION.TOP].getIntrinsicWidth());
            topBounds.bottom = (int) array.getDimension(R.styleable.DrawableTextView_drawableTopHeight, mDrawables[POSITION.TOP].getIntrinsicHeight());
        }

        if (mDrawables[POSITION.END] != null) {
            Rect endBounds = mDrawables[POSITION.END].getBounds();
            endBounds.right = (int) array.getDimension(R.styleable.DrawableTextView_drawableEndWidth, mDrawables[POSITION.END].getIntrinsicWidth());
            endBounds.bottom = (int) array.getDimension(R.styleable.DrawableTextView_drawableEndHeight, mDrawables[POSITION.END].getIntrinsicHeight());
        }

        if (mDrawables[POSITION.BOTTOM] != null) {
            Rect bottomBounds = mDrawables[POSITION.BOTTOM].getBounds();
            bottomBounds.right = (int) array.getDimension(R.styleable.DrawableTextView_drawableBottomWidth, mDrawables[POSITION.BOTTOM].getIntrinsicWidth());
            bottomBounds.bottom = (int) array.getDimension(R.styleable.DrawableTextView_drawableBottomHeight, mDrawables[POSITION.BOTTOM].getIntrinsicHeight());
        }
        array.recycle();
        resetCompoundDrawablesRelative();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (enableCenterDrawables) {
            isCenter = getLayoutParams().width != ViewGroup.LayoutParams.WRAP_CONTENT
                    && ((getGravity() & Gravity.CENTER) == Gravity.CENTER
                    || (getGravity() & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL
                    || (getGravity() & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL);


            mTextWidth = measureTextWidth();
            mTextHeight = measureTextHeight();
        }
        super.onLayout(changed, left, top, right, bottom);

    }

    /**
     * 在绘制前获取图片的Bounds，改变绘制的位置
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (enableCenterDrawables && isCenter) {

            //画布的偏移量
            int tranX = 0, tranY = 0;

            if (mDrawables[POSITION.START] != null) {
                Rect bounds = mDrawablesBounds[POSITION.START];
                int offset = (int) calcOffset(POSITION.START);
                mDrawables[POSITION.START].setBounds(bounds.left + offset, bounds.top,
                        bounds.right + offset, bounds.bottom);
                tranX -= (mDrawablesBounds[POSITION.START].width() + getCompoundDrawablePadding()) >> 1;
            }

            if (mDrawables[POSITION.TOP] != null) {
                Rect bounds = mDrawablesBounds[POSITION.TOP];
                int offset = (int) calcOffset(POSITION.TOP);

                mDrawables[POSITION.TOP].setBounds(bounds.left, bounds.top + offset,
                        bounds.right, bounds.bottom + offset);

                tranY -= (mDrawablesBounds[POSITION.TOP].height() + getCompoundDrawablePadding()) >> 1;
            }

            if (mDrawables[POSITION.END] != null) {
                Rect bounds = mDrawablesBounds[POSITION.END];
                int offset = -(int) calcOffset(POSITION.END);
                mDrawables[POSITION.END].setBounds(bounds.left + offset, bounds.top,
                        bounds.right + offset, bounds.bottom);

                tranX += (mDrawablesBounds[POSITION.END].width() + getCompoundDrawablePadding()) >> 1;
            }

            if (mDrawables[POSITION.BOTTOM] != null) {
                Rect bounds = mDrawablesBounds[POSITION.BOTTOM];
                int offset = -(int) calcOffset(POSITION.BOTTOM);
                mDrawables[POSITION.BOTTOM].setBounds(bounds.left, bounds.top + offset,
                        bounds.right, bounds.bottom + offset);

                tranY += (mDrawablesBounds[POSITION.BOTTOM].height() + getCompoundDrawablePadding()) >> 1;
            }

            if (enableTextInCenter) {
                canvas.translate(tranX, tranY);
            }
        }
        super.onDraw(canvas);


    }

    /**
     * 计算drawable居中还需距离
     * 如果左右两边都有图片，左图片居中则需要加上右侧图片占用的空间{@link #getCompoundPaddingEnd()},其他同理
     *
     * @return 偏移量
     */
    private float calcOffset(@POSITION int position) {

        switch (position) {
            case POSITION.START:
            case POSITION.END:
                return (getWidth() - (getCompoundPaddingStart() + getCompoundPaddingEnd() + mTextWidth)) / 2;

            case POSITION.TOP:
            case POSITION.BOTTOM:
                return (getHeight() - (getCompoundPaddingTop() + getCompoundPaddingBottom() + mTextHeight)) / 2;

            default:
                return 0;

        }
    }

    /**
     * 测量文字的宽度，通过Paint测量所有文字的长度，
     * 但是这个数据不一定准确，文本还有可能换行，还需要通过{@link #getLineBounds}来获取文本的最大宽度
     */
    protected float measureTextWidth() {
        getLineBounds(0, mTextBounds);
        float width = getPaint().measureText(getText().toString());
        float maxWidth = mTextBounds.right - mTextBounds.left;
        return width <= maxWidth ? width : maxWidth;
    }

    /**
     * 获取文本的高度，通过{@link #getLineHeight}乘文本的行数
     */
    protected float measureTextHeight() {
        return getLineHeight() * getLineCount();
    }

    /**
     * 设置Drawable，并设置宽高
     *
     * @param position {@link POSITION}
     * @param drawable Drawable
     * @param width    DX
     * @param height   DX
     */
    public void setDrawable(int position, Drawable drawable, @Px int width, @Px int height) {
        mDrawables[position] = drawable;
        if (drawable != null) {
            Rect bounds = new Rect();
            if (width == -1 && height == -1) {
                bounds.right = bounds.left + drawable.getIntrinsicWidth();
                bounds.bottom = bounds.top + drawable.getIntrinsicHeight();
            } else {
                bounds.right = bounds.left + width;
                bounds.bottom = bounds.top + height;
            }
            mDrawables[position].setBounds(bounds);
        }
        resetCompoundDrawablesRelative();
    }

    private void resetCompoundDrawablesRelative() {
        setCompoundDrawablesRelative(mDrawables[POSITION.START], mDrawables[POSITION.TOP], mDrawables[POSITION.END], mDrawables[POSITION.BOTTOM]);
    }

    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        storeDrawables(left, top, right, bottom);
    }

    @Override
    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        storeDrawables(start, top, end, bottom);
    }


    private void storeDrawables(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        if (mDrawables != null) {
            if (start != null && start != mDrawables[POSITION.START]) {
                mDrawablesBounds[POSITION.START] = start.copyBounds();
            }
            mDrawables[POSITION.START] = start;


            if (top != null && top != mDrawables[POSITION.TOP]) {
                mDrawablesBounds[POSITION.TOP] = top.copyBounds();
            }
            mDrawables[POSITION.TOP] = top;
            if (end != null && end != mDrawables[POSITION.END]) {
                mDrawablesBounds[POSITION.END] = end.copyBounds();
            }
            mDrawables[POSITION.END] = end;

            if (bottom != null && bottom != mDrawables[POSITION.BOTTOM]) {
                mDrawablesBounds[POSITION.BOTTOM] = bottom.copyBounds();
            }
            mDrawables[POSITION.BOTTOM] = bottom;
        }

    }


    public DrawableTextView setDrawableStart(Drawable drawableStart,
                                             @Dimension(unit = Dimension.DP) int width,
                                             @Dimension(unit = Dimension.DP) int height) {
        setDrawable(POSITION.START, drawableStart, dp2px(width), dp2px(height));
        return this;
    }

    public DrawableTextView setDrawableStart(Drawable drawableStart) {
        setDrawableStart(drawableStart, -1, -1);
        return this;
    }

    public DrawableTextView setDrawableTop(Drawable drawableTop,
                                           @Dimension(unit = Dimension.DP) int width,
                                           @Dimension(unit = Dimension.DP) int height) {
        setDrawable(POSITION.TOP, drawableTop, dp2px(width), dp2px(height));
        return this;
    }

    public DrawableTextView setDrawableTop(Drawable drawableTop) {
        setDrawableTop(drawableTop, -1, -1);
        return this;
    }

    public DrawableTextView setDrawableEnd(Drawable drawableEnd,
                                           @Dimension(unit = Dimension.DP) int width,
                                           @Dimension(unit = Dimension.DP) int height) {
        setDrawable(POSITION.END, drawableEnd, dp2px(width), dp2px(height));
        return this;
    }

    public DrawableTextView setDrawableEnd(Drawable drawableEnd) {
        setDrawableEnd(drawableEnd, -1, -1);
        return this;
    }


    public DrawableTextView setDrawableBottom(Drawable drawableBottom,
                                              @Dimension(unit = Dimension.DP) int width,
                                              @Dimension(unit = Dimension.DP) int height) {
        setDrawable(POSITION.BOTTOM, drawableBottom, dp2px(width), dp2px(height));
        return this;
    }

    public DrawableTextView setDrawableBottom(Drawable drawableBottom) {
        setDrawableBottom(drawableBottom, -1, -1);
        return this;
    }

    public void setEnableCenterDrawables(boolean enableCenterDrawables) {
        this.enableCenterDrawables = enableCenterDrawables;
    }

    public void setEnableTextInCenter(boolean enableTextInCenter) {
        this.enableTextInCenter = enableTextInCenter;
    }

    public Drawable[] getDrawables() {
        return mDrawables;
    }


}
