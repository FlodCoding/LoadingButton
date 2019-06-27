package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
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
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import java.util.Arrays;

/**
 * 1、控制drawable的大小 √
 * 2、文字居中时，drawable与textView一起居中 √
 * 3、getCompoundDrawablesRelative和getCompoundDrawables不一样
 * 4、设置loading 位置
 */
@SuppressWarnings("UnusedReturnValue")
public class LoadingButton extends DrawableTextView {
    private static final int DEFAULT_SHRINK_DURATION = 600;
    private CircularProgressDrawable mProgressDrawable;
    private ValueAnimator mShrinkAnimator;
    private boolean isAnimRunning;

    private int originalDrawablePadding = 0;

    private Drawable[] savedDrawables;
    private CharSequence savedText;
    private boolean savedEnableTextInCenter;
    private int[] savedRootViewSize = new int[]{0, 0};

    private EndDrawable mEndDrawable;

    private boolean enableShrinkAnim = true;   //是否开启收缩动画

    private boolean isShrink = true;

    private OnLoadingListener mOnLoadingListener;

    private int mLoadingSize;

    private int mLoadingPosition;

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

        setUpShrinkAnimator();

        mEndDrawable = new EndDrawable(R.drawable.ic_launcher);
        setEnableTextInCenter(true);
        setLoadingPosition(POSITION.START);
        setDrawable(POSITION.START, mProgressDrawable, (int) getTextSize(), (int) getTextSize());

    }

    private void setUpShrinkAnimator() {
        mShrinkAnimator = ValueAnimator.ofFloat(0, 1f);
        mShrinkAnimator.setDuration(DEFAULT_SHRINK_DURATION);
        mShrinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                    //x = (y - b)/k
                    //y = kx + b

                    // b = getHeight()
                    // k = getHeight() - getLoadingSize
                    // getLayoutParams().width  = (getHeight() - getLoadingSize())*animation.getAnimatedValue() + getHeight()
                    //getLayoutParams().width = (int) animation.getAnimatedValue();
                    getLayoutParams().width = (int) ((getLoadingDrawableSize()+60 - savedRootViewSize[0]) * (float) animation.getAnimatedValue() + savedRootViewSize[0]);
                    getLayoutParams().height = (int) ((getLoadingDrawableSize()+60 - savedRootViewSize[1]) * (float) animation.getAnimatedValue() + savedRootViewSize[1]);
                    requestLayout();



            }
        });

        mShrinkAnimator.addListener(new AnimatorListenerAdapter() {
            //onAnimationStart(Animator animation, boolean isReverse) 在7.0测试没有调用fuck

            @Override
            public void onAnimationStart(Animator animation) {
                if (isShrink) {
                    savedText = getText();
                    savedDrawables = Arrays.copyOf(getDrawables(), 4);
                    savedEnableTextInCenter = isEnableTextInCenter();

                    setText("");
                    setCompoundDrawablesRelative(mProgressDrawable, null, null, null);
                    setEnableTextInCenter(false);

                } else {
                    stopLoading();
                }

                if (mOnLoadingListener != null) {
                    mOnLoadingListener.onShrinkStart(isShrink);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isShrink) {
                    startLoading();
                } else {
                    setText(savedText);
                    setCompoundDrawablesRelative(savedDrawables[POSITION.START], savedDrawables[POSITION.TOP], savedDrawables[POSITION.END], savedDrawables[POSITION.BOTTOM]);
                    setEnableTextInCenter(savedEnableTextInCenter);
                }

                if (mOnLoadingListener != null) {
                    mOnLoadingListener.onShrinkEnd(isShrink);
                }

                isShrink = !isShrink;
            }

        });

    }



    private void startLoading() {
        mProgressDrawable.setStrokeWidth(getLoadingDrawableSize() * 0.12f);
        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingStart();
        }

        mProgressDrawable.start();
    }

    private void stopLoading() {
        mProgressDrawable.stop();
    }


    private void beginShrinkAnim(boolean isShrink) {
        if (enableShrinkAnim) {
            mShrinkAnimator.cancel();
            if (isShrink){

                mShrinkAnimator.start();
            }
            else
                mShrinkAnimator.reverse();
        }
    }

    public void start() {
        if (enableShrinkAnim)
            beginShrinkAnim(true);
        else {
            startLoading();
        }
    }

    public void stop() {
        stopLoading();
        mEndDrawable.show();
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


    public void setLoadingPosition(@POSITION int position) {
        mLoadingPosition = position;
    }

    public int getLoadingDrawableSize() {
        if (mLoadingSize == 0) {
            mLoadingSize = (int) (getTextSize());
        }
        return mLoadingSize;
    }

    public void setLoadingDrawableSize(@Px int size) {
        mLoadingSize = size;
    }


    @Override
    public void setCompoundDrawablePadding(int pad) {
        super.setCompoundDrawablePadding(pad);
    }

    @Override
    protected void onFirstLayout(int left, int top, int right, int bottom) {
        super.onFirstLayout(left, top, right, bottom);
        savedRootViewSize[0] = getWidth();
        savedRootViewSize[1] = getHeight();
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
        private Rect mBounds = new Rect();
        private Path mCirclePath;   //圆形裁剪路径
        private ObjectAnimator mAppearAnimator;
        private long duration = DEFAULT_END_DRAWABLE_DURATION;
        private float animValue;


        private EndDrawable(@DrawableRes int id) {
            setBitmap(id);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
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
                        }
                    }, duration);
                }
            });

        }

        private void show() {
            mAppearAnimator.cancel();
            mAppearAnimator.start();
        }

        private void hide() {
            mAppearAnimator.cancel();
            mAppearAnimator.reverse();
        }


        private int[] calcOffset(Canvas canvas, Rect bounds, @POSITION int pos) {
            int[] offset = new int[]{0, 0};
            offset[0] = canvas.getWidth() / 2;
            offset[1] = canvas.getHeight() / 2;
            switch (pos) {
                case POSITION.START: {
                    offset[0] -= (int) getTextWidth() / 2 + bounds.width() + getCompoundDrawablePadding();
                    if (enableShrinkAnim) {
                        offset[0] += bounds.width() / 2;
                    }
                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.TOP: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] -= (int) getTextHeight() / 2 + bounds.height() + getCompoundDrawablePadding();
                    if (enableShrinkAnim) {
                        offset[1] += bounds.height() / 2;
                    }
                    break;
                }
                case POSITION.END: {
                    offset[0] += (int) getTextWidth() / 2 + getCompoundDrawablePadding();
                    if (enableShrinkAnim) {
                        offset[0] -= bounds.width() / 2;
                    }
                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.BOTTOM: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] += (int) getTextHeight() / 2 + getCompoundDrawablePadding();
                    if (enableShrinkAnim) {
                        offset[1] += bounds.height() / 2;
                    }
                    break;
                }
            }
            return offset;
        }


        private void draw(Canvas canvas) {
            if (getAnimValue() > 0 && mProgressDrawable != null && mBitmap != null) {
                final Rect bounds = mProgressDrawable.getBounds();
                mBounds.right = bounds.width();
                mBounds.bottom = bounds.height();

                final int[] offsets = calcOffset(canvas, mBounds, mLoadingPosition);
                canvas.save();
                canvas.translate(offsets[0], offsets[1]);
                mCirclePath.reset();
                mCirclePath.addCircle(mBounds.centerX(), mBounds.centerY(),
                        ((getLoadingDrawableSize() >> 1) * 1.3f) * animValue, Path.Direction.CW);
                canvas.clipPath(mCirclePath);
                canvas.drawBitmap(mBitmap, null, mBounds, mPaint);
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
            mBitmap = getBitmap(id);
        }

        private void setDuration(long duration) {
            this.duration = duration;
        }
    }


    @Nullable
    private Bitmap getBitmap(int resId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }


    public interface OnLoadingListener {
        void onLoadingStart();

        void onLoadingEnd();

        void onShrinkStart(boolean isShrink);

        void onShrinkEnd(boolean isShrink);
    }

    public static class OnLoadingListenerAdapter implements OnLoadingListener {

        @Override
        public void onLoadingStart() {

        }

        @Override
        public void onLoadingEnd() {

        }

        @Override
        public void onShrinkStart(boolean isShrink) {

        }

        @Override
        public void onShrinkEnd(boolean isShrink) {

        }
    }


    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        mOnLoadingListener = onLoadingListener;
    }
}
