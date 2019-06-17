package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class HardLoadingButton extends AppCompatTextView {
    private CircularProgressDrawable mProgressDrawable;
    private ValueAnimator mAnimator;
    private boolean isFinished;
    private int originalWidth = -1;
    private int originalHeight = -1;


    public HardLoadingButton(Context context) {
        this(context, null);
    }

    public HardLoadingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HardLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mProgressDrawable = new CircularProgressDrawable(context);
        setUpAnimator();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void setUpAnimator() {
        final ValueAnimator animator = new ValueAnimator();
        animator.setDuration(1300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().width = (int) animation.getAnimatedValue();
                getLayoutParams().height = getOriginalHeight();
                requestLayout();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                setText("");
                startProgressDrawable();
            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                mProgressDrawable.start();
                isFinished = !isReverse;
            }
        });

        mAnimator = animator;
    }

    private void startProgressDrawable() {
        final int defaultSize = getProgressDrawableSize();
        mProgressDrawable.setStrokeWidth(defaultSize * 0.1f);
        mProgressDrawable.setColorSchemeColors(getTextColors().getDefaultColor());
        mProgressDrawable.setBounds(0, 0, defaultSize, defaultSize);
        setCompoundDrawables(mProgressDrawable, null, null, null);
    }

    public CircularProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public void start() {
        mAnimator.cancel();
        if (mAnimator.getValues() == null) {
            mAnimator.setIntValues(getOriginalWidth(), getShrinkWidth());
        }


        if (!isFinished) {
            mAnimator.start();
        } else
            mAnimator.reverse();


    }


    public void stop() {

    }


    private int getShrinkWidth() {
        return getProgressDrawableSize() + getPaddingEnd() + getPaddingStart();
    }

    private int getProgressDrawableSize() {
        int outSize = Math.min(getOriginalWidth(), getOriginalHeight());
        return (int) (outSize * 0.8);
    }

    public int getOriginalWidth() {
        if (originalWidth == -1)
            originalWidth = getWidth();
        return originalWidth;
    }

    public int getOriginalHeight() {
        if (originalHeight == -1)
            originalHeight = getHeight();
        return originalHeight;
    }
}
