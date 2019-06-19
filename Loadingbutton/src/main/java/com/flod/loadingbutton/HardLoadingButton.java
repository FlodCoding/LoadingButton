package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class HardLoadingButton extends AppCompatTextView {
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

    public HardLoadingButton(Context context) {
        this(context, null);
    }

    public HardLoadingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HardLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mProgressDrawable = new CircularProgressDrawable(context);
        originalDrawables = getCompoundDrawables();
        if (enableShrinkAnim) {
            setUpShrinkAnimator();
        }

        mEndDrawable = new EndDrawable(R.mipmap.ic_launcher);
    }

    private void setUpShrinkAnimator() {
        mShrinkAnimator = new ValueAnimator();
        mShrinkAnimator.setDuration(600);
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
                    setCompoundDrawables(originalDrawables[0], originalDrawables[1], originalDrawables[2], originalDrawables[3]);
                    setPadding(originalPadding[0], originalPadding[1], originalPadding[2], originalPadding[3]);
                    setText(mText);
                }
                isShrinkAnimFinished = !isShrinkAnimFinished;
            }

        });

    }

    private void startProgressDrawable() {
        setPadding(0, 0, 0, 0);
        final int defaultSize = getProgressDrawableSize();
        mProgressDrawable.setStrokeWidth(defaultSize * 0.12f);
        mProgressDrawable.setColorSchemeColors(getTextColors().getDefaultColor());
        final int left = (getOriginalHeight() - defaultSize) / 2;
        mProgressDrawable.setBounds(left, 0, left + defaultSize, defaultSize);
        setCompoundDrawables(mProgressDrawable, null, null, null);
        mProgressDrawable.start();
    }

    public CircularProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    private void beginShrinkAnim(boolean isShrink) {
        if (enableShrinkAnim) {
            mShrinkAnimator.cancel();
            if (mShrinkAnimator.getValues() == null) {
                getProgressDrawableSize();
                mShrinkAnimator.setIntValues(getOriginalWidth(), getShrinkSize());
            }
            if (isShrink)
                mShrinkAnimator.start();
            else
                mShrinkAnimator.reverse();
        }
    }

    public void start() {
        beginShrinkAnim(true);
    }


    public void stop() {
        mProgressDrawable.stop();
        mEndDrawable.show();
    }

    public void toggle() {

    }


    private int getShrinkSize() {
        return getOriginalHeight();
    }

    private int getProgressDrawableSize() {
        int outSize = Math.min(getOriginalWidth(), getOriginalHeight());
        return (int) (outSize * 0.7);
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
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (originalDrawables != null) {
            if (!(left instanceof CircularProgressDrawable))
                originalDrawables[0] = left;
            originalDrawables[1] = top;
            originalDrawables[2] = right;
            originalDrawables[3] = bottom;
        }
        super.setCompoundDrawables(left, top, right, bottom);
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
        super.onDraw(canvas);
        mEndDrawable.draw(canvas);
    }

    class EndDrawable {
        private Bitmap mBitmap;
        private Paint mPaint;
        private Path mCirclePath;   //圆形裁剪路径
        private ObjectAnimator mObjectAnimator;
        private float animValue;
        private boolean isAnimFinished;


        private boolean isShowing;


        EndDrawable(@DrawableRes int id) {
            mBitmap = BitmapFactory.decodeResource(getResources(), id);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCirclePath = new Path();
            mObjectAnimator = ObjectAnimator.ofFloat(this, "animValue", 1.0f);
            mObjectAnimator.setDuration(1500);
            mObjectAnimator.setAutoCancel(true);
            mObjectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                            beginShrinkAnim(false);
                        }
                    }, 1500);


                }
            });
            if (isAnimFinished)
                animValue = 0;
            isAnimFinished = !isAnimFinished;
        }


        void show() {
            mObjectAnimator.cancel();
            mObjectAnimator.start();
        }

        void hide() {
            mObjectAnimator.cancel();
            mObjectAnimator.reverse();
        }

        void toggle() {
            isShowing = !isShowing;
            if (isShowing)
                show();
            else
                hide();
        }

        private void draw(Canvas canvas) {
            if (getAnimValue() > 0) {
                canvas.save();
                mCirclePath.reset();
                int r = getShrinkSize() >> 1;
                Log.d("onDraw", (r * animValue) + "");
                mCirclePath.addCircle(r, r, (r * 1.3f) * animValue, Path.Direction.CW);
                canvas.clipPath(mCirclePath);
                Matrix matrix = new Matrix();
                float scale = canvas.getWidth() / (mBitmap.getWidth() + 0f);
                matrix.setScale(scale, scale);
                canvas.drawBitmap(mBitmap, matrix, mPaint);
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
    }


}
