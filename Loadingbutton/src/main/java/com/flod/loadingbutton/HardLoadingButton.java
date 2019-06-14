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
    private int originalWidth;
    private int originalHeight;


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


    private void setUpAnimator() {
        final ValueAnimator animator = new ValueAnimator();
        animator.setDuration(1300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getLayoutParams().width = (int) animation.getAnimatedValue();
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
        final int outSize = Math.min(originalWidth, originalHeight);
        final int defaultSize = (int) (outSize * 0.8);
        final int left = Math.abs(outSize - defaultSize) / 2 + getScrollX();
        final int top = getScrollY();
        final int right = left + defaultSize;
        final int bottom = top + defaultSize;

        mProgressDrawable.setStrokeWidth(defaultSize * 0.1f);
        mProgressDrawable.setColorSchemeColors(getTextColors().getDefaultColor());
        mProgressDrawable.setBounds(left, top, right, bottom);
        setCompoundDrawables(mProgressDrawable, null, null, null);
    }

    private void storeViewSize() {
        originalWidth = getWidth();
        originalHeight = getHeight();
    }

    public CircularProgressDrawable getProgressDrawable() {
        return mProgressDrawable;
    }


    public void start() {
        mAnimator.cancel();
        if (mAnimator.getValues() == null) {
            storeViewSize();
            mAnimator.setIntValues(originalWidth, originalHeight);
        }

        if (!isFinished) {
            mAnimator.start();
        } else
            mAnimator.reverse();
    }


    public void stop() {

    }

}
