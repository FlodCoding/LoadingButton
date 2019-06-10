package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;


public class CircularLoadingDrawable extends Drawable implements Animatable {

    private static final float CENTER_RADIUS = 7.5f;     //中心圆半径
    private static final float STROKE_WIDTH = 2.5f;      //圆环宽度

    private static final int ANIMATION_DURATION = 1332;  //圆环旋转完的动画时间

    private final Ring mRing;
    private Resources mResources;
    private Animator mAnimator;
    long mRotationCount;            //旋转次数
    private float mRotation;        //旋转的角度


    public CircularLoadingDrawable(@NonNull Context context) {
        mResources = context.getResources();
        mRing = new Ring();
        setStrokeWidth(STROKE_WIDTH);
        initAnimators();
    }

    private void initAnimators() {
        final Ring ring = mRing;
        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                //更新变化(ring)
                applyTransformation(animatedValue, ring, false);
                invalidateSelf();
            }
        });

        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());//线性匀速插值器
        animator.setDuration(ANIMATION_DURATION);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRotationCount = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                applyTransformation(1f, ring, true);
                ring.storeOriginals();

                //取消当前动画
                //animation.cancel();
                //animation.start();

                mRotationCount++;
            }
        });

        mAnimator = animator;
    }

    private void setStrokeWidth(float strokeWidth) {
        mRing.setStrokeWidth(strokeWidth);
        invalidateSelf();//重绘
    }


    /**
     * 更新视图变化
     *
     * @param animatedValue 动画值
     * @param ring          环
     * @param lastFrame     是否是最后一帧
     */
    private void applyTransformation(float animatedValue, Ring ring, boolean lastFrame) {
        final float startingRotation = 0;
        float startTrim, endTrim;

        if(animatedValue < 0.5f){
            //动画前半部分
            final float scaledTime = animatedValue / 0.5f;
            startTrim = 0;

        }

    }


    private static class Ring {
        final RectF mTempBounds = new RectF();
        final Paint mCirclePaint = new Paint();
        final Paint mPaint = new Paint();
        float mStrokeWidth = 5f;

        float mStartTrim = 0f;   //弧的开始位置系数,用来和360相乘
        float mEndTrim = 0f;     //弧的结束位置系数,用来和360相乘
        float mRotation = 0f;    //旋转的次数
        float mRingCenterRadius; //圆环中心圆半径

        int mCurrentColor = Color.BLACK;
        int mAlpha = 255;

        Ring() {
            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);

            mCirclePaint.setColor(Color.TRANSPARENT);
        }

        void setStrokeWidth(float strokeWidth) {
            mStrokeWidth = strokeWidth;
            mPaint.setStrokeWidth(strokeWidth);
        }


        //保存当前的状态
        void storeOriginals() {

        }


        void draw(Canvas c, Rect bounds) {
            final RectF arcBounds = mTempBounds;
            //弧半径
            float arcRadius = mRingCenterRadius + mStrokeWidth / 2f;
            if (mRingCenterRadius <= 0) {
                //如果mRingCenterRadius没有被设置过，那么就默认填充整个RectBounds

                //弧的半径
                arcRadius = Math.min(bounds.width(), bounds.height()) / 2f - mStrokeWidth / 2f;
            }
            //设置弧的边框
            arcBounds.set(bounds.centerX() - arcRadius,
                    bounds.centerY() - arcRadius,
                    bounds.centerX() + arcRadius,
                    bounds.centerY() - arcRadius);

            final float startAngle = (mStartTrim + mRotation) * 360;
            final float endAngle = (mEndTrim + mRotation) * 360;
            //扫过的角度
            float sweepAngle = endAngle - startAngle;

            mPaint.setColor(mCurrentColor);
            mPaint.setAlpha(mAlpha);

            //开始绘制了

            //绘制内圆
            float inset = mStrokeWidth / 2f;    //缩进的大小
            arcBounds.inset(inset, inset);      //弧的边框向内缩进,得到这个弧的内圆
            c.drawCircle(arcBounds.centerX(), arcBounds.centerY(),
                    arcBounds.width() / 2f, mCirclePaint);
            arcBounds.inset(-inset, -inset);    //复位

            //绘制弧
            c.drawArc(arcBounds, startAngle, sweepAngle, false, mPaint);
        }


        //---------------RingCenterRadius------------------//
        void setCenterRadius(float centerRadius) {
            mRingCenterRadius = centerRadius;
        }

        float getCenterRadius() {
            return mRingCenterRadius;
        }


        public float getStartTrim() {
            return mStartTrim;
        }

        public void setStartTrim(float startTrim) {
            mStartTrim = startTrim;
        }

        public float getEndTrim() {
            return mEndTrim;
        }

        public void setEndTrim(float endTrim) {
            mEndTrim = endTrim;
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Rect bounds = getBounds();
        canvas.save();
        canvas.rotate(mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        mRing.draw(canvas, bounds);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    @Override
    public void start() {
        mAnimator.cancel();
        mRing.storeOriginals();
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return mAnimator.isRunning();
    }
}
