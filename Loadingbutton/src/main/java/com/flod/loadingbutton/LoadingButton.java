package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

/**
 * 1、控制drawable的大小
 * 2、文字居中时，drawable与textView一起居中
 * 3、getCompoundDrawablesRelative和getCompoundDrawables不一样
 */
@SuppressWarnings("UnusedReturnValue")
public class LoadingButton extends DrawableTextView {
    private static final int DEFAULT_SHRINK_DURATION = 600;
    private CircularProgressDrawable mProgressDrawable;
    private ValueAnimator mShrinkAnimator;
    private int originalWidth;
    private int originalHeight;
    private int[] originalPadding = new int[4];
    private Drawable[] originalDrawables;
    private CharSequence mText;

    private EndDrawable mEndDrawable;

    private boolean enableShrinkAnim = true;   //是否开启收缩动画

    private boolean isShrinkAnimFinished;

    private OnLoadingListener mOnLoadingListener;

    private int mLoadingSize;

    public LoadingButton(Context context) {
        super(context);
        init(context, null);
    }

    public LoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        setLayerType(LAYER_TYPE_HARDWARE, null);
        mProgressDrawable = new CircularProgressDrawable(context);
        mProgressDrawable.setColorSchemeColors(getTextColors().getDefaultColor());

        originalDrawables = getCompoundDrawablesRelative();
        if (enableShrinkAnim) {
            setUpShrinkAnimator();
        }

        mEndDrawable = new EndDrawable(R.drawable.ic_launcher);
    }

    private void setUpShrinkAnimator() {
        mShrinkAnimator = new ValueAnimator();
        mShrinkAnimator.setDuration(DEFAULT_SHRINK_DURATION);
        mShrinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().width = (int) animation.getAnimatedValue();
                getLayoutParams().height = getOriginalHeight();
                requestLayout();
            }
        });

        mShrinkAnimator.addListener(new AnimatorListenerAdapter() {
            //onAnimationStart(Animator animation, boolean isReverse) 在7.0测试没有调用fuck
            @Override
            public void onAnimationStart(Animator animation) {
                if (!isShrinkAnimFinished) {
                    mText = getText();
                    setText("");
                } else {
                    mProgressDrawable.stop();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShrinkAnimFinished) {
                    startProgressDrawable();
                } else {
                    setCompoundDrawablesRelative(originalDrawables[0], originalDrawables[1], originalDrawables[2], originalDrawables[3]);
                    setPadding(originalPadding[0], originalPadding[1], originalPadding[2], originalPadding[3]);
                    setText(mText);
                }
                isShrinkAnimFinished = !isShrinkAnimFinished;
            }

        });

    }

    private void startProgressDrawable() {
        final int size = getLoadingSize();
        mProgressDrawable.setStrokeWidth(size * 0.15f);
        setDrawable(POSITION.START, mProgressDrawable, size, size);
        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingStart();
        }
        mProgressDrawable.start();
    }


    private void beginShrinkAnim(boolean isShrink) {
        if (enableShrinkAnim) {
            mShrinkAnimator.cancel();
            if (mShrinkAnimator.getValues() == null) {
                mShrinkAnimator.setIntValues(getOriginalWidth(), getShrinkSize());
            }
            if (isShrink)
                mShrinkAnimator.start();
            else
                mShrinkAnimator.reverse();
        } else {
            setCompoundDrawablesRelative(originalDrawables[0], originalDrawables[1], originalDrawables[2], originalDrawables[3]);
        }
    }

    public void start() {
        if (enableShrinkAnim)
            beginShrinkAnim(true);
        else {
            startProgressDrawable();
        }

//        mEndDrawable.show();
    }


    public void stop() {
        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingEnd();
        }
        mProgressDrawable.stop();
        mEndDrawable.show();
       // mEndDrawable.hide();
    }

    public void toggle() {

    }


    public LoadingButton setEndDrawable(@DrawableRes int id) {
        mEndDrawable.setBitmap(id);
        return this;
    }

    public LoadingButton setEndDrawableDuration(long milliseconds) {
        mEndDrawable.setDuration(milliseconds);
        return this;
    }

    public LoadingButton setShrinkDuration(long milliseconds) {
        mShrinkAnimator.setDuration(milliseconds);
        return this;
    }

    public LoadingButton setLoadingSize(@Px int size) {
        mLoadingSize = size;
        return this;
    }

    public LoadingButton setLoadingColor(@NonNull int... colors) {
        mProgressDrawable.setColorSchemeColors(colors);
        return this;
    }

    public CircularProgressDrawable getLoadingDrawable() {
        return mProgressDrawable;
    }

    public int getLoadingSize() {
        if (mLoadingSize == 0) {
            mLoadingSize = (int) ((getOriginalHeight() -
                    getPaddingTop() - getPaddingBottom()) * 0.8);
        }
        return mLoadingSize;
    }

    private int getShrinkSize() {
        return getOriginalHeight();
    }


    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        originalPadding[0] = getPaddingStart();
        originalPadding[1] = getPaddingTop();
        originalPadding[2] = getPaddingEnd();
        originalPadding[3] = getPaddingBottom();
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setCompoundDrawablesRelative(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (originalDrawables != null) {
            if (!(left instanceof CircularProgressDrawable))
                originalDrawables[0] = left;
            originalDrawables[1] = top;
            originalDrawables[2] = right;
            originalDrawables[3] = bottom;
        }
        super.setCompoundDrawablesRelative(left, top, right, bottom);
    }

    public int getOriginalWidth() {
        if (originalWidth == 0)
            originalWidth = getWidth();
        return originalWidth;
    }

    public int getOriginalHeight() {
        if (originalHeight == 0)
            originalHeight = getHeight();
        return originalHeight;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mEndDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    @SuppressWarnings("SameParameterValue")
    private class EndDrawable {
        private static final int DEFAULT_END_DRAWABLE_DURATION = 1500;
        private static final int DEFAULT_APPEAR_DURATION = 300;
        private Bitmap mBitmap;
        private Paint mPaint;
        private Path mCirclePath;   //圆形裁剪路径
        private ObjectAnimator mAppearAnimator;
        private long duration = DEFAULT_END_DRAWABLE_DURATION;
        private float animValue;
        private boolean isShowing;


        private EndDrawable(@DrawableRes int id) {
            setBitmap(id);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCirclePath = new Path();
            mAppearAnimator = ObjectAnimator.ofFloat(this, "animValue", 1.0f);
            mAppearAnimator.setDuration(DEFAULT_APPEAR_DURATION);
            mAppearAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onLoadingEnd();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setAnimValue(0);
                            beginShrinkAnim(false);

                            if (mOnLoadingListener != null) {
                                mOnLoadingListener.onEndDrawableEnd();
                            }
                        }
                    }, duration);
                }
            });


        }


        private void show() {
            mAppearAnimator.cancel();
            mAppearAnimator.start();

            //setDrawable(POSITION.START, mDrawable, 60, 60);
        }

        private void hide() {
            mAppearAnimator.cancel();
            mAppearAnimator.reverse();
        }

        private void toggle() {
            isShowing = !isShowing;
            if (isShowing)
                show();
            else
                hide();
        }

        private void draw(Canvas canvas) {
            if (getAnimValue() > 0 && mProgressDrawable != null) {

                final int compoundPaddingTop = getCompoundPaddingTop();
                final int compoundPaddingBottom = getCompoundPaddingBottom();
                final Rect bounds = mProgressDrawable.getBounds();
                final int vspace = canvas.getHeight() - compoundPaddingTop - compoundPaddingBottom;
                final int offsetX = getScrollX() + getPaddingStart();
                final int offsetY = getScrollY() + compoundPaddingTop + (vspace - bounds.height()) / 2;

                canvas.save();
                canvas.translate(offsetX, offsetY);
                mCirclePath.reset();
                mCirclePath.addCircle(bounds.centerX(), bounds.centerY(),
                        ((getLoadingSize() >> 1) * 1.3f) * animValue, Path.Direction.CW);
                canvas.clipPath(mCirclePath);
                canvas.drawBitmap(mBitmap, null, bounds, mPaint);
                canvas.restore();
            }
        }

        private void setAnimValue(float animValue) {
            this.animValue = animValue;
            invalidate();
        }

        private float getAnimValue() {
            return animValue;
        }

        private void setBitmap(int id) {
            mBitmap = BitmapFactory.decodeResource(getResources(), id);
        }

        private void setDuration(long duration) {
            this.duration = duration;
        }
    }


    public interface OnLoadingListener {
        void onLoadingStart();

        void onLoadingEnd();

        void onEndDrawableEnd();
    }


    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        mOnLoadingListener = onLoadingListener;
    }
}
