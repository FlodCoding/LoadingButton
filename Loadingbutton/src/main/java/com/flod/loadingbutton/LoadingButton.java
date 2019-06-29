package com.flod.loadingbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

/**
 * 1、改变loading的大小
 * 2、收缩动画后不居中
 * 3、收缩后的大小随loading的大小决定
 * 4、设置loading 可以设置为上下左右
 * 5、重复start的动画处理
 * 6、恢复动画还没结束时，点击收缩会变成恢复的状态
 * 7、正在显示EndDrawable时，再次点击start会变成恢复状态的加载
 *    先执行 beginShrinkAnim(true) 后执行 beginShrinkAnim(false);
 */
@SuppressWarnings("UnusedReturnValue,SameParameterValue")
public class LoadingButton extends DrawableTextView {
    private CircularProgressDrawable mLoadingDrawable;
    private ValueAnimator mShrinkAnimator;
    private boolean isAnimRunning;

    private Drawable[] mDrawablesSaved;
    private int mDrawablePaddingSaved;
    private CharSequence mTextSaved;
    private boolean mEnableTextInCenterSaved;
    private int[] mRootViewSizeSaved = new int[]{0, 0};

    private EndDrawable mEndDrawable;

    private boolean enableShrink;   //是否开启收缩动画
    private int mShrinkDuration;

    private boolean isShrinkAnimReverse;


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

        //getConfig
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton);
        enableShrink = array.getBoolean(R.styleable.LoadingButton_enableShrink, false);
        mShrinkDuration = array.getInt(R.styleable.LoadingButton_shrinkDuration, 600);
        int loadingDrawableSize = array.getDimensionPixelSize(R.styleable.LoadingButton_loadingDrawableSize, (int) getTextSize());
        int loadingDrawablePosition = array.getInt(R.styleable.LoadingButton_loadingDrawablePosition, POSITION.START);
        int endDrawableResId = array.getResourceId(R.styleable.LoadingButton_endDrawable, -1);
        int endDrawableDuration = array.getInt(R.styleable.LoadingButton_endDrawableDuration, 1500);
        array.recycle();

        //initLoadingDrawable
        mLoadingDrawable = new CircularProgressDrawable(context);
        mLoadingDrawable.setColorSchemeColors(getTextColors().getDefaultColor());
        mLoadingDrawable.setStrokeWidth(loadingDrawableSize * 0.14f);
        setLoadingPosition(loadingDrawablePosition);
        setLoadingSize(loadingDrawableSize);
        setDrawable(POSITION.START, mLoadingDrawable, loadingDrawableSize, loadingDrawableSize);

        //initLoadingDrawable
        if (endDrawableResId != -1) {
            mEndDrawable = new EndDrawable(endDrawableResId);
            mEndDrawable.setDuration(endDrawableDuration);
        }

        //initShrinkAnimator
        setUpShrinkAnimator();
        setEnableTextInCenter(true);
    }

    private void setUpShrinkAnimator() {
        mShrinkAnimator = ValueAnimator.ofFloat(0, 1f);
        mShrinkAnimator.setDuration(mShrinkDuration);
        mShrinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //Log.d("Animation", "onAnimationUpdate: " + animation.getAnimatedFraction());
                // y = kx + b
                // b = getRootViewSize()
                // k = getRootViewSize() - getLoadingSize
                getLayoutParams().width = (int) ((getShrinkSize() - mRootViewSizeSaved[0]) * (float) animation.getAnimatedValue() + mRootViewSizeSaved[0]);
                getLayoutParams().height = (int) ((getShrinkSize() - mRootViewSizeSaved[1]) * (float) animation.getAnimatedValue() + mRootViewSizeSaved[1]);
                requestLayout();
            }
        });

        mShrinkAnimator.addListener(new AnimatorListenerAdapter() {

            //onAnimationStart(Animator animation, boolean isReverse) 在7.0测试没有调用fuck
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimRunning = true;
                //Log.d("Animation", "onAnimationStart");
                if (mOnLoadingListener != null) {
                    mOnLoadingListener.onShrinkStart(isShrinkAnimReverse);
                }

                if (!isShrinkAnimReverse) {
                    //开始收缩
                    saveStatus();
                    setText("");
                    setCompoundDrawablePadding(0);
                    setCompoundDrawablesRelative(mLoadingDrawable, null, null, null);
                    setEnableTextInCenter(false);

                } else {
                    //开始恢复
                    stopLoading();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //Log.d("Animation", "onAnimationEnd");
                if (mOnLoadingListener != null) {
                    mOnLoadingListener.onShrinkEnd(isShrinkAnimReverse);
                }
                if (!isShrinkAnimReverse) {
                    //收缩结束
                    startLoading();
                } else {
                    //恢复结束
                    stopLoading();
                    restoreStatus();
                    setEnabled(true);
                    isAnimRunning = false;
                }
                isShrinkAnimReverse = !isShrinkAnimReverse;
            }

        });


    }

    public void start() {
        Log.d("Animation", "start");
        if (enableShrink) {

            //TODO 如果mEndDrawable mEndDrawable.isShowing 且准备执行 beginShrinkAnim(false); 就会冲突
            if (mEndDrawable != null && mEndDrawable.isShowing) {
                //想办法先让上一个动画先结束
                mEndDrawable.cancel();
            }
            beginShrinkAnim(true, false);
        } else {
            startLoading();
        }

    }

    public void end() {
        if (mEndDrawable == null) {
            if (mOnLoadingListener != null) {
                mOnLoadingListener.onLoadingEnd();
            }
            beginShrinkAnim(false, false);
        } else {
            if (mShrinkAnimator.isRunning()) {
                //结束正在收缩的动画
                mShrinkAnimator.end();
            }
            stopLoading();
            Log.d("Animation", "end");
            mEndDrawable.show();
        }

    }

    /**
     * 保存收缩前的状态
     */
    private void saveStatus() {
        mTextSaved = getText();
        mDrawablesSaved = copyDrawables(true);
        mDrawablePaddingSaved = getCompoundDrawablePadding();
        mEnableTextInCenterSaved = isEnableTextInCenter();
    }

    /**
     * 恢复收缩后的状态
     */
    private void restoreStatus() {
        setText(mTextSaved);
        setCompoundDrawablePadding(mDrawablePaddingSaved);
        setCompoundDrawablesRelative(mDrawablesSaved[POSITION.START], mDrawablesSaved[POSITION.TOP], mDrawablesSaved[POSITION.END], mDrawablesSaved[POSITION.BOTTOM]);
        setEnableTextInCenter(mEnableTextInCenterSaved);

        getLayoutParams().width = mRootViewSizeSaved[0];
        getLayoutParams().height = mRootViewSizeSaved[1];
        requestLayout();
    }


    private void beginShrinkAnim(boolean isShrink, boolean lastFrame) {
        Log.d("Animation", "isShrink" + isShrink);
        if (enableShrink) {
            if (mShrinkAnimator.isRunning()) {
                //如果上一个动画还在执行，就结束到最后一帧
                mShrinkAnimator.end();
            }
            if (isShrink) {
                mShrinkAnimator.start();
            } else {
                mShrinkAnimator.reverse();
            }

            if (lastFrame) {
                mShrinkAnimator.end();
            }

        }
    }


    private void startLoading() {
        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingStart();
        }
        mLoadingDrawable.start();
    }

    private void stopLoading() {
        mLoadingDrawable.stop();
    }

    public void setEnableShrink(boolean enable) {
        this.enableShrink = enable;
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
        mLoadingDrawable.setColorSchemeColors(colors);
        return this;
    }

    public CircularProgressDrawable getLoadingDrawable() {
        return mLoadingDrawable;
    }


    public void setLoadingPosition(@POSITION int position) {
        mLoadingPosition = position;
    }

    public int getLoadingDrawableSize() {
        return mLoadingSize;
    }

    public void setLoadingDrawableSize(@Px int size) {
        mLoadingSize = size;
    }

    public int getShrinkSize() {
        return Math.max(Math.min(mRootViewSizeSaved[0], mRootViewSizeSaved[1]), getLoadingDrawableSize());
    }


    @Override
    public void setCompoundDrawablePadding(int pad) {
        super.setCompoundDrawablePadding(pad);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isAnimRunning) {
            mRootViewSizeSaved[0] = getWidth();
            mRootViewSizeSaved[1] = getHeight();
        }
    }

    @Override
    protected void onFirstLayout(int left, int top, int right, int bottom) {
        super.onFirstLayout(left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mEndDrawable != null)
            mEndDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    private class EndDrawable {
        private static final int DEFAULT_APPEAR_DURATION = 300;
        private Bitmap mBitmap;
        private Paint mPaint;
        private Rect mBounds = new Rect();
        private Path mCirclePath;   //圆形裁剪路径
        private ObjectAnimator mAppearAnimator;
        private long duration;
        private float animValue;
        private boolean isShowing;
        private Runnable mRunnable;


        private EndDrawable(@DrawableRes int id) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
            setBitmap(id);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            mCirclePath = new Path();
            mAppearAnimator = ObjectAnimator.ofFloat(this, "animValue", 1.0f);
            mAppearAnimator.setDuration(DEFAULT_APPEAR_DURATION);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("Animation", "onEndDrawableAnimationEnd");
                    setAnimValue(0);
                    if (enableShrink && isAnimRunning) {
                        beginShrinkAnim(false, false);
                    } else {
                        setEnabled(true);
                        isAnimRunning = false;
                    }
                    isShowing = false;
                }
            };
            mAppearAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onLoadingEnd();
                    }
                    if (isShowing) {
                        postDelayed(mRunnable, duration);
                    }
                }
            });

        }

        private void show() {
            //如果仍在显示中，结束动画
            if (isShowing) {
                cancel();
            }
            mAppearAnimator.start();
            isShowing = true;
        }

        private void cancel() {
            isShowing = false;
            if (mAppearAnimator.isRunning()) {
                //出现中
                mAppearAnimator.end();
            }
            getHandler().removeCallbacks(mRunnable);
            beginShrinkAnim(false, true);
            setAnimValue(0);
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
                    if (enableShrink) {
                        offset[0] += bounds.width() / 2;
                    }
                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.TOP: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] -= (int) getTextHeight() / 2 + bounds.height() + getCompoundDrawablePadding();
                    if (enableShrink) {
                        offset[1] += bounds.height() / 2;
                    }
                    break;
                }
                case POSITION.END: {
                    offset[0] += (int) getTextWidth() / 2 + getCompoundDrawablePadding();
                    if (enableShrink) {
                        offset[0] -= bounds.width() / 2;
                    }
                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.BOTTOM: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] += (int) getTextHeight() / 2 + getCompoundDrawablePadding();
                    if (enableShrink) {
                        offset[1] += bounds.height() / 2;
                    }
                    break;
                }
            }
            return offset;
        }

        private void draw(Canvas canvas) {
            if (getAnimValue() > 0 && mLoadingDrawable != null && mBitmap != null) {
                final Rect bounds = mLoadingDrawable.getBounds();
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

        public ObjectAnimator getAppearAnimator() {
            return mAppearAnimator;
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
        public void onShrinkStart(boolean isReverse) {

        }

        @Override
        public void onShrinkEnd(boolean isReverse) {

        }
    }

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        mOnLoadingListener = onLoadingListener;
    }


    private void cancel() {

    }
}
