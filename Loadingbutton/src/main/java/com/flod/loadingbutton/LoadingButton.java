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
import android.view.View;

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
 * 先执行 beginShrinkAnim(true) 后执行 beginShrinkAnim(false);
 * <p>
 * 8、多次start和end 会出错
 * 9、设置完Drawable大小后，start后再次设置rootView大小失控,是因为原来是wrap_content
 * 10、start和compete同时按Loading没有关
 */
@SuppressWarnings({"UnusedReturnValue,SameParameterValue", "unused"})
public class LoadingButton extends DrawableTextView {
    private int curStatus = STATE.IDE;

    interface STATE {
        int IDE = 0;
        int SHRINKING = 1;
        int LOADING = 2;
        int END_DRAWABLE_SHOWING = 3;
        int RESTORING = 4;
    }

    private Drawable[] mDrawablesSaved;
    private int mDrawablePaddingSaved;
    private CharSequence mTextSaved;
    private boolean mEnableTextInCenterSaved;
    private boolean mViewEnableSaved;
    private int[] mRootViewSizeSaved = new int[]{0, 0};

    private boolean disableClickOnLoading;   //动画中禁用点击
    private boolean enableShrink;            //是否开启收缩动画
    private ValueAnimator mShrinkAnimator;
    private int mShrinkDuration;
    private CircularProgressDrawable mLoadingDrawable;
    private OnLoadingListener mOnLoadingListener;
    private EndDrawable mEndDrawable;
    private int mLoadingSize;
    private int mLoadingPosition;


    private boolean nextShrinkReverse;
    private boolean isCancel;
    private boolean isFail;


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
        disableClickOnLoading = array.getBoolean(R.styleable.LoadingButton_disableClickOnLoading, true);
        mShrinkDuration = array.getInt(R.styleable.LoadingButton_shrinkDuration, 500);
        int loadingDrawableSize = array.getDimensionPixelSize(R.styleable.LoadingButton_loadingDrawableSize, (int) (enableShrink ? getTextSize() * 2 : getTextSize()));
        int loadingDrawableColor = array.getColor(R.styleable.LoadingButton_loadingDrawableColor, getTextColors().getDefaultColor());
        int loadingDrawablePosition = array.getInt(R.styleable.LoadingButton_loadingDrawablePosition, POSITION.START);
        int endCompleteDrawableResId = array.getResourceId(R.styleable.LoadingButton_endCompleteDrawable, -1);
        int endFailDrawableResId = array.getResourceId(R.styleable.LoadingButton_endFailDrawable, -1);
        int endDrawableAppearTime = array.getInt(R.styleable.LoadingButton_endDrawableAppearTime, EndDrawable.DEFAULT_APPEAR_DURATION);
        int endDrawableDuration = array.getInt(R.styleable.LoadingButton_endDrawableDuration, 1500);
        array.recycle();

        //initLoadingDrawable
        mLoadingDrawable = new CircularProgressDrawable(context);
        mLoadingDrawable.setColorSchemeColors(loadingDrawableColor);
        mLoadingDrawable.setStrokeWidth(loadingDrawableSize * 0.14f);

        mLoadingSize = loadingDrawableSize;
        mLoadingPosition = loadingDrawablePosition;
        setDrawable(mLoadingPosition, mLoadingDrawable, loadingDrawableSize, loadingDrawableSize);

        //initLoadingDrawable
        if (endCompleteDrawableResId != -1 || endFailDrawableResId != -1) {
            mEndDrawable = new EndDrawable(endCompleteDrawableResId, endFailDrawableResId);
            mEndDrawable.mAppearAnimator.setDuration(endDrawableAppearTime);
            mEndDrawable.setDuration(endDrawableDuration);
        }

        //initShrinkAnimator
        setUpShrinkAnimator();

        //Start|End -> true  Top|Bottom ->false
        setEnableTextInCenter(mLoadingPosition % 2 == 0);

        if (getRootView().isInEditMode()) {
            mLoadingDrawable.setStartEndTrim(0, 0.8f);
        }

    }

    private void setUpShrinkAnimator() {
        mShrinkAnimator = ValueAnimator.ofFloat(0, 1f);
        mShrinkAnimator.setDuration(mShrinkDuration);
        mShrinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
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
                if (!nextShrinkReverse) {
                    //开始收缩
                    curStatus = STATE.SHRINKING;
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onShrinking();
                    }

                    saveStatus();
                    setText("");
                    setCompoundDrawablePadding(0);
                    setCompoundDrawablesRelative(mLoadingDrawable, null, null, null);
                    setEnableTextInCenter(false);

                } else {
                    //开始恢复
                    curStatus = STATE.RESTORING;
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onRestoring();
                        mOnLoadingListener.onLoadingStop();
                    }
                    stopLoading();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!nextShrinkReverse) {
                    //收缩结束
                    curStatus = STATE.LOADING;
                    startLoading();
                    nextShrinkReverse = true;

                } else {
                    //恢复结束
                    curStatus = STATE.IDE;
                    restoreStatus();
                    endCallbackListener();
                    nextShrinkReverse = false;
                }
            }

        });

    }

    private void endCallbackListener() {
        if (mOnLoadingListener != null) {
            if (isCancel)
                mOnLoadingListener.onCanceled();
            else if (isFail)
                mOnLoadingListener.onFailed();
            else
                mOnLoadingListener.onCompleted();

            if (disableClickOnLoading) {
                super.setEnabled(mViewEnableSaved);
            }
            isCancel = false;
            isFail = false;
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

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                measureTextHeight();
                measureTextWidth();
                removeOnLayoutChangeListener(this);
            }
        });

    }


    private void beginShrinkAnim(boolean isReverse, boolean lastFrame) {
        if (enableShrink) {
            if (mShrinkAnimator.isRunning()) {
                //如果上一个动画还在执行，就结束到最后一帧
                mShrinkAnimator.end();
            }
            nextShrinkReverse = isReverse;
            if (!isReverse) {
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
        curStatus = STATE.LOADING;
        if (mOnLoadingListener != null) {
            mOnLoadingListener.onLoadingStart();
        }

        mLoadingDrawable.start();

    }

    private void stopLoading() {
        mLoadingDrawable.stop();
    }


    private void cancelAllRunning(boolean withAnim) {
        switch (curStatus) {
            case STATE.SHRINKING:
                beginShrinkAnim(true, !withAnim);
                break;
            case STATE.LOADING:
                if (enableShrink) {
                    beginShrinkAnim(true, !withAnim);
                } else {
                    stopLoading();
                    endCallbackListener();
                    restoreStatus();
                    curStatus = STATE.IDE;
                }
                break;
            case STATE.END_DRAWABLE_SHOWING:
                if (mEndDrawable != null) {
                    mEndDrawable.cancel(withAnim);
                } else {
                    beginShrinkAnim(true, !withAnim);
                }
                break;
            case STATE.RESTORING:
                if (!withAnim)
                    mShrinkAnimator.end();
                else {
                    beginShrinkAnim(true, true);
                }
                break;
        }

    }


    public void start() {
        //disable click
        if (disableClickOnLoading) {
            super.setEnabled(false);
        }

        //cancel last loading
        if (curStatus == STATE.SHRINKING || curStatus == STATE.LOADING)
            isCancel = true;
        cancelAllRunning(false);


        if (enableShrink) {
            beginShrinkAnim(false, false);
        } else {
            saveStatus();
            startLoading();
        }
    }

    public void complete() {
        if (mEndDrawable != null) {
            mEndDrawable.show(true);
        } else {
            //TODO 测试
            if (curStatus == STATE.LOADING)
                beginShrinkAnim(true, false);
            else
                beginShrinkAnim(true, true);
        }
    }

    public void fail() {
        isFail = true;
        if (mEndDrawable != null) {
            mEndDrawable.show(false);
        } else {
            beginShrinkAnim(true, false);
        }
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean withAnim) {
        if (curStatus != STATE.IDE) {
            isCancel = true;
            cancelAllRunning(withAnim);
        }
    }

    public void setDisableClickOnLoading(boolean disable) {
        this.disableClickOnLoading = disable;
    }

    public void setEnableShrink(boolean enable) {
        this.enableShrink = enable;
    }


    public int getShrinkSize() {
        return Math.max(Math.min(mRootViewSizeSaved[0], mRootViewSizeSaved[1]), getLoadingDrawableSize());
    }

    public ValueAnimator getShrinkAnimator() {
        return mShrinkAnimator;
    }

    public LoadingButton setShrinkDuration(long milliseconds) {
        mShrinkAnimator.setDuration(milliseconds);
        return this;
    }

    public int getShrinkDuration() {
        return mShrinkDuration;
    }

    public CircularProgressDrawable getLoadingDrawable() {
        return mLoadingDrawable;
    }

    public LoadingButton setLoadingColor(@NonNull int... colors) {
        mLoadingDrawable.setColorSchemeColors(colors);
        return this;
    }


    public void setLoadingPosition(@POSITION int position) {
        boolean enableTextInCenter = position % 2 == 0;
        setEnableTextInCenter(enableTextInCenter);
        mEnableTextInCenterSaved = enableTextInCenter;
        setDrawable(mLoadingPosition, null, 0, 0);
        mLoadingPosition = position;
        setDrawable(position, getLoadingDrawable(), getLoadingDrawableSize(), getLoadingDrawableSize());
    }

    public LoadingButton setLoadingEndDrawableSize(@Px int size) {
        mLoadingSize = size;
        setDrawable(mLoadingPosition, mLoadingDrawable, size, size);
        return this;
    }

    public int getLoadingDrawableSize() {
        return mLoadingSize;
    }


    public LoadingButton setCompleteEndDrawable(@DrawableRes int id) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(id, -1);
        else {
            mEndDrawable.setCompleteDrawable(id);
        }
        return this;
    }

    public LoadingButton setCompleteEndDrawable(Drawable drawable) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(drawable, null);
        else {
            mEndDrawable.setCompleteDrawable(drawable);
        }
        return this;
    }

    public LoadingButton setFailEndDrawable(@DrawableRes int id) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(-1, id);
        else {
            mEndDrawable.setFailDrawable(id);
        }
        return this;
    }

    public LoadingButton setFailEndDrawable(Drawable drawable) {
        if (mEndDrawable == null)
            mEndDrawable = new EndDrawable(null, drawable);
        else {
            mEndDrawable.setFailDrawable(drawable);
        }
        return this;
    }

    public LoadingButton setEndDrawableDuration(long milliseconds) {
        if (mEndDrawable != null)
            mEndDrawable.setDuration(milliseconds);
        return this;
    }

    public long getEndDrawableDuration() {
        if (mEndDrawable != null)
            return mEndDrawable.duration;
        return EndDrawable.DEFAULT_APPEAR_DURATION;
    }

    public LoadingButton setEndDrawableAppearTime(long milliseconds) {
        if (mEndDrawable != null)
            mEndDrawable.getAppearAnimator().setDuration(milliseconds);
        return this;
    }

    @Nullable
    public ObjectAnimator getEndDrawableAnimator() {
        if (mEndDrawable != null) {
            return mEndDrawable.getAppearAnimator();
        }
        return null;
    }


    @Override
    public void setCompoundDrawablePadding(int pad) {
        super.setCompoundDrawablePadding(pad);
        if (curStatus == STATE.IDE)
            mDrawablePaddingSaved = pad;
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        //TODO 测试
        if (enableShrink && (curStatus != STATE.IDE)) {
            text = "";
        }
        super.setText(text, type);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mViewEnableSaved = enabled;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (curStatus == STATE.IDE) {
            mRootViewSizeSaved[0] = getWidth();
            mRootViewSizeSaved[1] = getHeight();
        }
    }

    @Override
    protected void onFirstLayout(int left, int top, int right, int bottom) {
        super.onFirstLayout(left, top, right, bottom);
        saveStatus();
        mViewEnableSaved = isEnabled();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mEndDrawable != null)
            mEndDrawable.draw(canvas);
        super.onDraw(canvas);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public class EndDrawable {
        private static final int DEFAULT_APPEAR_DURATION = 300;
        private Bitmap mCompleteBitmap;
        private Bitmap mFailBitmap;
        private Paint mPaint;
        private Rect mBounds = new Rect();
        private Path mCirclePath;   //圆形裁剪路径
        private ObjectAnimator mAppearAnimator;
        private long duration;
        private float animValue;
        int[] offsetTemp = new int[]{0, 0};
        private boolean isShowing;
        private Runnable mRunnable;

        private EndDrawable(@Nullable Drawable completeDrawable, @Nullable Drawable failDrawable) {
            setCompleteDrawable(completeDrawable);
            setFailDrawable(failDrawable);
            init();
        }

        private EndDrawable(@DrawableRes int completeResId, @DrawableRes int failResId) {
            setCompleteDrawable(completeResId);
            setFailDrawable(failResId);
            init();
        }

        private void init() {
            setLayerType(LAYER_TYPE_HARDWARE, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            mCirclePath = new Path();
            mAppearAnimator = ObjectAnimator.ofFloat(this, "animValue", 1.0f);
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    setAnimValue(0);
                    if (enableShrink)
                        beginShrinkAnim(true, !nextShrinkReverse);
                    else {
                        curStatus = STATE.IDE;
                        restoreStatus();
                        endCallbackListener();
                    }
                    isShowing = false;
                }
            };
            mAppearAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onEndDrawableAppear(!isFail, mEndDrawable);
                    }
                    curStatus = STATE.END_DRAWABLE_SHOWING;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mOnLoadingListener != null) {
                        mOnLoadingListener.onLoadingStop();
                    }
                    if (isShowing) {
                        postDelayed(mRunnable, duration);
                    }
                }
            });
        }


        /**
         * 显示EndDrawable
         *
         * @param isSuccess true:CompleteDrawable fail:FailDrawable
         */
        private void show(boolean isSuccess) {
            //end running Shrinking
            if (mShrinkAnimator.isRunning()) {
                mShrinkAnimator.end();
            }

            //StopLoading
            if (mOnLoadingListener != null) {
                mOnLoadingListener.onLoadingStop();
            }
            stopLoading();

            //end showing endDrawable
            if (isShowing) {
                cancel(false);
            }
            mAppearAnimator.start();
            isShowing = true;
        }

        private void cancel(boolean withAnim) {
            isShowing = false;
            if (mAppearAnimator.isRunning()) {
                mAppearAnimator.end();
            }
            getHandler().removeCallbacks(mRunnable);
            if (enableShrink)
                beginShrinkAnim(true, !withAnim);
            else {
                endCallbackListener();
                restoreStatus();
                curStatus = STATE.IDE;
            }
            setAnimValue(0);
        }


        private void hide() {
            if (isShowing) {
                cancel(false);
            }
            mAppearAnimator.reverse();
            isShowing = true;
        }

        private int[] calcOffset(Canvas canvas, Rect bounds, @POSITION int pos) {
            final int[] offset = offsetTemp;
            offset[0] = canvas.getWidth() / 2;
            offset[1] = canvas.getHeight() / 2;

            switch (pos) {
                case POSITION.START: {
                    offset[0] -= (int) getTextWidth() / 2 + bounds.width() + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[0] += bounds.width() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[0] += (bounds.width() + getCompoundDrawablePadding()) / 2;
                    }

                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.TOP: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] -= (int) getTextHeight() / 2 + bounds.height() + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[1] += bounds.height() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[1] += (bounds.height() + getCompoundDrawablePadding()) / 2;
                    }
                    break;
                }
                case POSITION.END: {
                    offset[0] += (int) getTextWidth() / 2 + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[0] -= bounds.width() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[0] -= (bounds.width() + getCompoundDrawablePadding()) / 2;
                    }
                    offset[1] -= bounds.height() / 2;
                    break;
                }
                case POSITION.BOTTOM: {
                    offset[0] -= bounds.width() / 2;
                    offset[1] += (int) getTextHeight() / 2 + getCompoundDrawablePadding();
                    if (enableShrink && nextShrinkReverse) {
                        offset[1] -= bounds.height() / 2;
                    } else if (!isEnableTextInCenter()) {
                        offset[1] -= (bounds.height() + getCompoundDrawablePadding()) / 2;
                    }
                    break;
                }
            }
            return offset;
        }

        private void draw(Canvas canvas) {
            if (getAnimValue() > 0 && mLoadingDrawable != null) {
                final Bitmap targetBitMap = isFail ? mFailBitmap : mCompleteBitmap;
                if (targetBitMap != null) {
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
                    canvas.drawBitmap(targetBitMap, null, mBounds, mPaint);
                    canvas.restore();
                }
            }
        }

        private void setAnimValue(float animValue) {
            this.animValue = animValue;
            invalidate();
        }

        private float getAnimValue() {
            return animValue;
        }

        public ObjectAnimator getAppearAnimator() {
            return mAppearAnimator;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public void setCompleteDrawable(Drawable drawable) {
            mCompleteBitmap = getBitmap(drawable);
        }

        public void setCompleteDrawable(@DrawableRes int id) {
            if (id != -1) {
                Drawable drawable = ContextCompat.getDrawable(getContext(), id);
                mCompleteBitmap = getBitmap(drawable);
            }
        }

        public void setFailDrawable(@DrawableRes int id) {
            if (id != -1) {
                Drawable failDrawable = ContextCompat.getDrawable(getContext(), id);
                mFailBitmap = getBitmap(failDrawable);
            }
        }

        public void setFailDrawable(Drawable drawable) {
            mCompleteBitmap = getBitmap(drawable);
        }

    }

    @Nullable
    private Bitmap getBitmap(Drawable drawable) {
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

        void onLoadingStop();

        void onShrinking();

        void onRestoring();

        void onEndDrawableAppear(boolean isSuccess, EndDrawable endDrawable);

        void onCompleted();

        void onFailed();

        void onCanceled();
    }


    public static class OnLoadingListenerAdapter implements OnLoadingListener {

        @Override
        public void onShrinking() {

        }


        @Override
        public void onLoadingStart() {

        }

        @Override
        public void onLoadingStop() {


        }

        @Override
        public void onEndDrawableAppear(boolean isSuccess, EndDrawable endDrawable) {

        }

        @Override
        public void onRestoring() {

        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onCanceled() {

        }


    }

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        mOnLoadingListener = onLoadingListener;
    }


}
